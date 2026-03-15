/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async.task;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.wladimirwendland.bibleaxis.data.library.LibraryContext;
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;
import de.wladimirwendland.bibleaxis.utils.Task;

public class SyncModulesFromTreeTask extends Task {

    public enum StatusCode {
        Success,
        PartialSuccess,
        StorageFolderInvalid,
        SourcePermissionDenied,
        AppFolderCreateFailed,
        LibraryNotFound,
        CopyFailed
    }

    private final WeakReference<Context> weakContext;
    private final Uri treeUri;
    private final ILibraryController libraryController;
    private final LibraryContext libraryContext;

    private StatusCode statusCode = StatusCode.Success;
    private int addedCount;
    private int updatedCount;
    private int failedCount;
    private boolean appFolderCreated;
    private boolean modulesFolderCreated;

    public SyncModulesFromTreeTask(
            @NonNull Context context,
            @NonNull String message,
            @NonNull Uri treeUri,
            @NonNull ILibraryController libraryController,
            @NonNull LibraryContext libraryContext
    ) {
        super(message, false);
        this.weakContext = new WeakReference<>(context.getApplicationContext());
        this.treeUri = treeUri;
        this.libraryController = libraryController;
        this.libraryContext = libraryContext;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        Context context = weakContext.get();
        if (context == null) {
            statusCode = StatusCode.CopyFailed;
            return false;
        }

        File modulesDir = libraryContext.modulesDir();
        if (!modulesDir.exists() && !modulesDir.mkdirs()) {
            statusCode = StatusCode.LibraryNotFound;
            return false;
        }

        DocumentFile sourceDir;
        try {
            sourceDir = DocumentFile.fromTreeUri(context, treeUri);
        } catch (Exception e) {
            statusCode = StatusCode.StorageFolderInvalid;
            StaticLogger.error(this, "Can't open source tree", e);
            return false;
        }

        if (sourceDir == null || !sourceDir.exists() || !sourceDir.isDirectory()) {
            statusCode = StatusCode.StorageFolderInvalid;
            return false;
        }

        if (!sourceDir.canRead()) {
            statusCode = StatusCode.SourcePermissionDenied;
            return false;
        }

        DocumentFile modulesSourceDir = getOrCreateModulesSourceDir(sourceDir);
        if (modulesSourceDir == null) {
            return false;
        }

        List<DocumentFile> zipFiles = new ArrayList<>();
        collectZipFiles(modulesSourceDir, zipFiles);

        for (DocumentFile sourceFile : zipFiles) {
            if (!copySourceFile(context, sourceFile, modulesDir)) {
                failedCount++;
            }
        }

        libraryController.reloadModules();

        if (failedCount > 0 && (addedCount > 0 || updatedCount > 0)) {
            statusCode = StatusCode.PartialSuccess;
            return true;
        }

        if (failedCount > 0) {
            statusCode = StatusCode.CopyFailed;
            return false;
        }

        statusCode = StatusCode.Success;
        return true;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public int getAddedCount() {
        return addedCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public boolean isAppFolderCreated() {
        return appFolderCreated;
    }

    public boolean isModulesFolderCreated() {
        return modulesFolderCreated;
    }

    @NonNull
    public String getSummaryText() {
        return String.format(Locale.US, "Добавлено: %d, обновлено: %d, ошибок: %d", addedCount, updatedCount, failedCount);
    }

    private DocumentFile getOrCreateModulesSourceDir(@NonNull DocumentFile selectedStorageDir) {
        DocumentFile appDir = resolveAppDir(selectedStorageDir);
        if (appDir == null || !appDir.exists() || !appDir.isDirectory()) {
            statusCode = StatusCode.AppFolderCreateFailed;
            return null;
        }

        DocumentFile modulesDir = findDirectoryByName(appDir, "modules");
        if (modulesDir != null && modulesDir.exists() && modulesDir.isDirectory()) {
            return modulesDir;
        }

        DocumentFile createdModulesDir;
        try {
            createdModulesDir = appDir.createDirectory("modules");
        } catch (Exception e) {
            StaticLogger.error(this, "Can't create modules folder", e);
            statusCode = StatusCode.AppFolderCreateFailed;
            return null;
        }

        if (createdModulesDir == null || !createdModulesDir.exists() || !createdModulesDir.isDirectory()) {
            statusCode = StatusCode.AppFolderCreateFailed;
            return null;
        }

        modulesFolderCreated = true;
        return createdModulesDir;
    }

    private DocumentFile resolveAppDir(@NonNull DocumentFile selectedStorageDir) {
        String selectedName = selectedStorageDir.getName();
        if ("modules".equalsIgnoreCase(selectedName)) {
            DocumentFile parent = selectedStorageDir.getParentFile();
            if (parent != null && "BibleAxis".equalsIgnoreCase(parent.getName())) {
                return parent;
            }
        }

        if ("BibleAxis".equalsIgnoreCase(selectedName)) {
            return selectedStorageDir;
        }

        DocumentFile existingAppDir = findDirectoryByName(selectedStorageDir, "BibleAxis");
        if (existingAppDir != null) {
            return existingAppDir;
        }

        DocumentFile createdAppDir;
        try {
            createdAppDir = selectedStorageDir.createDirectory("BibleAxis");
        } catch (Exception e) {
            StaticLogger.error(this, "Can't create BibleAxis folder", e);
            statusCode = StatusCode.AppFolderCreateFailed;
            return null;
        }

        if (createdAppDir == null || !createdAppDir.exists() || !createdAppDir.isDirectory()) {
            statusCode = StatusCode.AppFolderCreateFailed;
            return null;
        }

        appFolderCreated = true;
        return createdAppDir;
    }

    private DocumentFile findDirectoryByName(@NonNull DocumentFile parent, @NonNull String dirName) {
        DocumentFile[] children;
        try {
            children = parent.listFiles();
        } catch (Exception e) {
            StaticLogger.error(this, "Can't list folders", e);
            return null;
        }

        for (DocumentFile child : children) {
            if (child != null && child.isDirectory() && dirName.equalsIgnoreCase(child.getName())) {
                return child;
            }
        }
        return null;
    }

    private void collectZipFiles(@NonNull DocumentFile current, @NonNull List<DocumentFile> result) {
        DocumentFile[] children;
        try {
            children = current.listFiles();
        } catch (Exception e) {
            StaticLogger.error(this, "Can't list files", e);
            return;
        }

        for (DocumentFile child : children) {
            if (child == null) {
                continue;
            }

            if (child.isDirectory()) {
                collectZipFiles(child, result);
                continue;
            }

            String name = child.getName();
            if (name != null && name.toLowerCase(Locale.US).endsWith(".zip")) {
                result.add(child);
            }
        }
    }

    private boolean copySourceFile(@NonNull Context context, @NonNull DocumentFile sourceFile, @NonNull File modulesDir) {
        String sourceName = sourceFile.getName();
        if (sourceName == null || sourceName.trim().isEmpty()) {
            return false;
        }

        File targetFile = new File(modulesDir, sourceName);
        boolean existed = targetFile.exists();

        try (
                InputStream inputStream = context.getContentResolver().openInputStream(sourceFile.getUri());
                OutputStream outputStream = new FileOutputStream(targetFile)
        ) {
            if (inputStream == null) {
                return false;
            }

            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        } catch (Exception e) {
            StaticLogger.error(this, "Copy failed for " + sourceName, e);
            return false;
        }

        if (existed) {
            updatedCount++;
        } else {
            addedCount++;
        }
        return true;
    }
}
