/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import de.wladimirwendland.bibleaxis.dal.repository.bookmarks.BookmarksTags;
import de.wladimirwendland.bibleaxis.dal.repository.migration.Migration;
import de.wladimirwendland.bibleaxis.dal.repository.migration.Migration_1_2;
import de.wladimirwendland.bibleaxis.dal.repository.migration.Migration_2_3;
import de.wladimirwendland.bibleaxis.dal.repository.migration.Migration_3_4;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @since 02.05.13
 */
public final class DbLibraryHelper {

    public static final String DB_NAME = "library.db";
    public static final String TAGS_TABLE = "tags";
    public static final String BOOKMARKS_TAGS_TABLE = "bookmarks_tags";
    public static final String BOOKMARKS_TABLE = "bookmarks";
    public static final String HIGHLIGHTS_TABLE = "highlights";

    private static final String[] CREATE_DATABASE = new String[]{
            "create table " + BOOKMARKS_TABLE + " ("
                    + Bookmark.KEY_ID + " integer primary key autoincrement, "
                    + Bookmark.OSIS + " text unique not null, "
                    + Bookmark.LINK + " text not null, "
                    + Bookmark.DATE + " text not null"
                    + ");",
            "create table " + BOOKMARKS_TAGS_TABLE + " ("
                    + BookmarksTags.BOOKMARKSTAGS_KEY_ID + " integer primary key autoincrement, "
                    + BookmarksTags.BOOKMARKSTAGS_BM_ID + " integer not null, "
                    + BookmarksTags.BOOKMARKSTAGS_TAG_ID + " integer not null"
                    + ");",
            "create table " + TAGS_TABLE + " ("
                    + Tag.KEY_ID + " integer primary key autoincrement, "
                    + Tag.NAME + " text unique not null"
                    + ");"
    };
    private static final List<Migration> MIGRATIONS = Arrays.asList(
            new Migration_1_2(),
            new Migration_2_3(),
            new Migration_3_4()
    );
    private static final int VERSION = 4;

    private final Context mContext;
    private SQLiteDatabase database;

    public DbLibraryHelper(Context context) {
        mContext = context;
    }

    public SQLiteDatabase getDatabase() {
        if (database == null) {
            database = openOrCreateDatabase();
        }
        return database;
    }

    private void onCreate(SQLiteDatabase db) {
        for (String command : CREATE_DATABASE) {
            db.execSQL(command);
        }
    }

    private void onUpgrade(SQLiteDatabase db, final int oldVersion) {
        if (oldVersion == DbLibraryHelper.VERSION) {
            // миграция не требуется
            return;
        }

        Collections.sort(MIGRATIONS);
        int currVersion = oldVersion;
        for (Migration migration : MIGRATIONS) {
            // ищем миграцию для текущей версии БД
            if (migration.oldVersion != currVersion) {
                continue;
            }

            // выполняем миграцию и обновляем текущую версию
            migration.migrate(db);
            currVersion = migration.newVersion;

            // если достигли требуемой версии БД, то прерываем миграцию
            if (currVersion == DbLibraryHelper.VERSION) {
                break;
            }
        }
    }

    private SQLiteDatabase openOrCreateDatabase() {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(mContext.getDatabasePath(DB_NAME), null);
        int oldVersion = db.getVersion();
        if (oldVersion < VERSION) {
            db.beginTransaction();
            try {
                if (oldVersion == 0) {
                    onCreate(db);
                    oldVersion = 1;
                }
                onUpgrade(db, oldVersion);
                db.setVersion(VERSION);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        return db;
    }
}
