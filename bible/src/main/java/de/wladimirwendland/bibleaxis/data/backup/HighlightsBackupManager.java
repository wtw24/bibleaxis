/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.backup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.wladimirwendland.bibleaxis.BuildConfig;
import de.wladimirwendland.bibleaxis.dal.DbLibraryHelper;
import de.wladimirwendland.bibleaxis.domain.entity.Highlight;
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;
import de.wladimirwendland.bibleaxis.domain.threading.AppTaskRunner;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

@Singleton
public class HighlightsBackupManager {

    private static final int AUTO_BACKUP_DELAY_MS = 5_000;
    private static final String APP_DIR_NAME = "BibleAxis";
    private static final String BACKUPS_DIR_NAME = "backups";
    private static final String BACKUP_FILE_NAME = "highlights-latest.json";
    private static final int BACKUP_SCHEMA_VERSION = 1;

    @NonNull
    private final Context appContext;
    @NonNull
    private final PreferenceHelper preferenceHelper;
    @NonNull
    private final DbLibraryHelper dbLibraryHelper;
    @NonNull
    private final AppTaskRunner appTaskRunner;
    @NonNull
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @NonNull
    private final Runnable autoBackupRunnable;

    @Inject
    public HighlightsBackupManager(
            @NonNull Context context,
            @NonNull PreferenceHelper preferenceHelper,
            @NonNull DbLibraryHelper dbLibraryHelper,
            @NonNull AppTaskRunner appTaskRunner
    ) {
        this.appContext = context.getApplicationContext();
        this.preferenceHelper = preferenceHelper;
        this.dbLibraryHelper = dbLibraryHelper;
        this.appTaskRunner = appTaskRunner;
        this.autoBackupRunnable = () -> this.appTaskRunner.runOnIo(this::backupHighlightsSafely);
    }

    public void scheduleAutoBackup() {
        mainHandler.removeCallbacks(autoBackupRunnable);
        mainHandler.postDelayed(autoBackupRunnable, AUTO_BACKUP_DELAY_MS);
    }

    @NonNull
    public RestoreResult restoreFromUri(@NonNull Uri backupUri) {
        SQLiteDatabase db = dbLibraryHelper.getDatabase();
        String backupText;

        try {
            backupText = readTextFromUri(backupUri);
        } catch (Exception ex) {
            StaticLogger.error(this, "Read backup file failed", ex);
            return RestoreResult.error();
        }

        try {
            JSONObject root = new JSONObject(backupText);
            int schemaVersion = root.optInt("schemaVersion", BACKUP_SCHEMA_VERSION);
            if (schemaVersion != BACKUP_SCHEMA_VERSION) {
                return RestoreResult.error();
            }
            JSONArray highlights = root.optJSONArray("highlights");
            if (highlights == null) {
                return RestoreResult.error();
            }

            int addedCount = 0;
            int skippedCount = 0;
            int failedCount = 0;

            db.beginTransaction();
            try {
                for (int i = 0; i < highlights.length(); i++) {
                    JSONObject item = highlights.optJSONObject(i);
                    if (item == null) {
                        failedCount++;
                        continue;
                    }

                    try {
                        if (exists(db, item)) {
                            skippedCount++;
                            continue;
                        }

                        ContentValues values = toContentValues(item);
                        long insertResult = db.insert(DbLibraryHelper.HIGHLIGHTS_TABLE, null, values);
                        if (insertResult > 0) {
                            addedCount++;
                        } else {
                            failedCount++;
                        }
                    } catch (Exception ex) {
                        failedCount++;
                        StaticLogger.error(this, "Restore single highlight failed", ex);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return new RestoreResult(addedCount, skippedCount, failedCount);
        } catch (Exception ex) {
            StaticLogger.error(this, "Parse backup file failed", ex);
            return RestoreResult.error();
        }
    }

    private void backupHighlightsSafely() {
        String treeUriText = preferenceHelper.getModulesSourceTreeUri();
        if (treeUriText == null || treeUriText.isEmpty()) {
            return;
        }

        try {
            Uri treeUri = Uri.parse(treeUriText);
            DocumentFile selectedStorageDir = DocumentFile.fromTreeUri(appContext, treeUri);
            if (selectedStorageDir == null || !selectedStorageDir.exists() || !selectedStorageDir.isDirectory()) {
                return;
            }

            DocumentFile appDir = resolveAppDir(selectedStorageDir);
            if (appDir == null || !appDir.exists()) {
                return;
            }

            DocumentFile backupsDir = findDirectoryByName(appDir, BACKUPS_DIR_NAME);
            if (backupsDir == null) {
                backupsDir = appDir.createDirectory(BACKUPS_DIR_NAME);
            }
            if (backupsDir == null || !backupsDir.exists() || !backupsDir.isDirectory()) {
                return;
            }

            DocumentFile existing = backupsDir.findFile(BACKUP_FILE_NAME);
            if (existing != null && existing.exists() && !existing.delete()) {
                StaticLogger.error(this, "Delete old backup file failed");
                return;
            }

            DocumentFile backupFile = backupsDir.createFile("application/json", BACKUP_FILE_NAME);
            if (backupFile == null) {
                return;
            }

            JSONObject payload = buildBackupPayload();
            byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);

            ContentResolver contentResolver = appContext.getContentResolver();
            try (OutputStream outputStream = contentResolver.openOutputStream(backupFile.getUri())) {
                if (outputStream == null) {
                    return;
                }
                outputStream.write(bytes);
                outputStream.flush();
            }

            preferenceHelper.setHighlightsLastBackupAt(System.currentTimeMillis());
        } catch (Exception ex) {
            StaticLogger.error(this, "Auto backup highlights failed", ex);
        }
    }

    @NonNull
    private JSONObject buildBackupPayload() throws Exception {
        JSONObject root = new JSONObject();
        root.put("schemaVersion", BACKUP_SCHEMA_VERSION);
        root.put("createdAt", System.currentTimeMillis());
        root.put("appVersion", BuildConfig.VERSION_NAME);
        root.put("highlights", loadHighlights());
        return root;
    }

    @NonNull
    private JSONArray loadHighlights() throws Exception {
        JSONArray result = new JSONArray();
        SQLiteDatabase db = dbLibraryHelper.getDatabase();

        String query = "SELECT * FROM " + DbLibraryHelper.HIGHLIGHTS_TABLE + " ORDER BY " + Highlight.TIME + " ASC";
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (!cursor.moveToFirst()) {
                return result;
            }

            do {
                JSONObject item = new JSONObject();
                item.put(Highlight.MODULE_ID, cursor.getString(cursor.getColumnIndex(Highlight.MODULE_ID)));
                item.put(Highlight.BOOK_ID, cursor.getString(cursor.getColumnIndex(Highlight.BOOK_ID)));
                item.put(Highlight.CHAPTER, cursor.getInt(cursor.getColumnIndex(Highlight.CHAPTER)));
                item.put(Highlight.START_VERSE, cursor.getInt(cursor.getColumnIndex(Highlight.START_VERSE)));
                item.put(Highlight.START_OFFSET, cursor.getInt(cursor.getColumnIndex(Highlight.START_OFFSET)));
                item.put(Highlight.END_VERSE, cursor.getInt(cursor.getColumnIndex(Highlight.END_VERSE)));
                item.put(Highlight.END_OFFSET, cursor.getInt(cursor.getColumnIndex(Highlight.END_OFFSET)));
                item.put(Highlight.COLOR, cursor.getString(cursor.getColumnIndex(Highlight.COLOR)));
                item.put(Highlight.QUOTE, cursor.getString(cursor.getColumnIndex(Highlight.QUOTE)));
                item.put(Highlight.TIME, cursor.getLong(cursor.getColumnIndex(Highlight.TIME)));
                result.put(item);
            } while (cursor.moveToNext());
        }

        return result;
    }

    @NonNull
    private String readTextFromUri(@NonNull Uri uri) throws Exception {
        ContentResolver contentResolver = appContext.getContentResolver();
        try (InputStream inputStream = contentResolver.openInputStream(uri)) {
            if (inputStream == null) {
                throw new IllegalStateException("input stream is null");
            }
            byte[] bytes = new byte[8192];
            StringBuilder builder = new StringBuilder();
            int read;
            while ((read = inputStream.read(bytes)) != -1) {
                builder.append(new String(bytes, 0, read, StandardCharsets.UTF_8));
            }
            return builder.toString();
        }
    }

    private boolean exists(@NonNull SQLiteDatabase db, @NonNull JSONObject item) {
        String moduleId = item.optString(Highlight.MODULE_ID, "");
        String bookId = item.optString(Highlight.BOOK_ID, "");
        int chapter = item.optInt(Highlight.CHAPTER, -1);
        int startVerse = item.optInt(Highlight.START_VERSE, -1);
        int startOffset = item.optInt(Highlight.START_OFFSET, -1);
        int endVerse = item.optInt(Highlight.END_VERSE, -1);
        int endOffset = item.optInt(Highlight.END_OFFSET, -1);
        String color = item.optString(Highlight.COLOR, "");
        String quote = item.isNull(Highlight.QUOTE) ? null : item.optString(Highlight.QUOTE, null);
        long time = item.optLong(Highlight.TIME, -1L);

        String query = "SELECT 1 FROM " + DbLibraryHelper.HIGHLIGHTS_TABLE
                + " WHERE " + Highlight.MODULE_ID + "=?"
                + " AND " + Highlight.BOOK_ID + "=?"
                + " AND " + Highlight.CHAPTER + "=?"
                + " AND " + Highlight.START_VERSE + "=?"
                + " AND " + Highlight.START_OFFSET + "=?"
                + " AND " + Highlight.END_VERSE + "=?"
                + " AND " + Highlight.END_OFFSET + "=?"
                + " AND " + Highlight.COLOR + "=?"
                + " AND ((" + Highlight.QUOTE + " IS NULL AND ? IS NULL) OR " + Highlight.QUOTE + "=?)"
                + " AND " + Highlight.TIME + "=?"
                + " LIMIT 1";

        String[] args = new String[]{
                moduleId,
                bookId,
                String.valueOf(chapter),
                String.valueOf(startVerse),
                String.valueOf(startOffset),
                String.valueOf(endVerse),
                String.valueOf(endOffset),
                color,
                quote,
                quote,
                String.valueOf(time)
        };

        try (Cursor cursor = db.rawQuery(query, args)) {
            return cursor.moveToFirst();
        }
    }

    @NonNull
    private ContentValues toContentValues(@NonNull JSONObject item) {
        ContentValues values = new ContentValues();
        values.put(Highlight.MODULE_ID, item.optString(Highlight.MODULE_ID, ""));
        values.put(Highlight.BOOK_ID, item.optString(Highlight.BOOK_ID, ""));
        values.put(Highlight.CHAPTER, item.optInt(Highlight.CHAPTER, 0));
        values.put(Highlight.START_VERSE, item.optInt(Highlight.START_VERSE, 0));
        values.put(Highlight.START_OFFSET, item.optInt(Highlight.START_OFFSET, 0));
        values.put(Highlight.END_VERSE, item.optInt(Highlight.END_VERSE, 0));
        values.put(Highlight.END_OFFSET, item.optInt(Highlight.END_OFFSET, 0));
        values.put(Highlight.COLOR, item.optString(Highlight.COLOR, "#FFFF99"));
        if (item.has(Highlight.QUOTE) && !item.isNull(Highlight.QUOTE)) {
            values.put(Highlight.QUOTE, item.optString(Highlight.QUOTE, ""));
        } else {
            values.putNull(Highlight.QUOTE);
        }
        values.put(Highlight.TIME, item.optLong(Highlight.TIME, System.currentTimeMillis()));
        return values;
    }

    @Nullable
    private DocumentFile resolveAppDir(@NonNull DocumentFile selectedStorageDir) {
        String selectedName = selectedStorageDir.getName();
        if ("modules".equalsIgnoreCase(selectedName)) {
            DocumentFile parent = selectedStorageDir.getParentFile();
            if (parent != null && APP_DIR_NAME.equalsIgnoreCase(parent.getName())) {
                return parent;
            }
        }

        if (APP_DIR_NAME.equalsIgnoreCase(selectedName)) {
            return selectedStorageDir;
        }

        DocumentFile existingAppDir = findDirectoryByName(selectedStorageDir, APP_DIR_NAME);
        if (existingAppDir != null) {
            return existingAppDir;
        }

        try {
            return selectedStorageDir.createDirectory(APP_DIR_NAME);
        } catch (Exception ex) {
            StaticLogger.error(this, "Create BibleAxis folder failed", ex);
            return null;
        }
    }

    @Nullable
    private DocumentFile findDirectoryByName(@NonNull DocumentFile parent, @NonNull String dirName) {
        DocumentFile[] children;
        try {
            children = parent.listFiles();
        } catch (Exception ex) {
            StaticLogger.error(this, "List folders failed", ex);
            return null;
        }

        for (DocumentFile child : children) {
            if (child != null && child.isDirectory() && dirName.equalsIgnoreCase(child.getName())) {
                return child;
            }
        }
        return null;
    }

    public static final class RestoreResult {
        public final int addedCount;
        public final int skippedCount;
        public final int failedCount;

        RestoreResult(int addedCount, int skippedCount, int failedCount) {
            this.addedCount = addedCount;
            this.skippedCount = skippedCount;
            this.failedCount = failedCount;
        }

        @NonNull
        public static RestoreResult error() {
            return new RestoreResult(0, 0, 1);
        }
    }
}
