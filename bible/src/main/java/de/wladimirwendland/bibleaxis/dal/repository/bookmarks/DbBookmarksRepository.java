/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository.bookmarks;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.wladimirwendland.bibleaxis.dal.DbLibraryHelper;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.domain.repository.IBookmarksRepository;

import java.util.ArrayList;
import java.util.List;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class DbBookmarksRepository implements IBookmarksRepository {

    private static final String TABLE_NAME = DbLibraryHelper.BOOKMARKS_TABLE;

    @NonNull
    private final DbLibraryHelper dbLibraryHelper;

    public DbBookmarksRepository(@NonNull DbLibraryHelper dbLibraryHelper) {
        this.dbLibraryHelper = dbLibraryHelper;
    }

    @Override
    public long add(Bookmark bookmark) {
        StaticLogger.info(this, String.format("Add bookmarks %S:%s", bookmark.OSISLink, bookmark.humanLink));

        ContentValues values = new ContentValues();
        values.put(Bookmark.LINK, bookmark.humanLink);
        values.put(Bookmark.OSIS, bookmark.OSISLink);
        values.put(Bookmark.NAME, bookmark.name);
        values.put(Bookmark.DATE, bookmark.date);
        values.put(Bookmark.TIME, bookmark.time);

        String query = String.format("SELECT * FROM %s WHERE %s=?", DbLibraryHelper.BOOKMARKS_TABLE, Bookmark.OSIS);
        String[] args = {bookmark.OSISLink};

        long result = -1;
        SQLiteDatabase db = dbLibraryHelper.getDatabase();
        try (Cursor curr = db.rawQuery(query, args)) {
            db.beginTransaction();
            if (curr.moveToFirst()) {
                result = curr.getLong(curr.getColumnIndex(Bookmark.KEY_ID));
                db.update(DbLibraryHelper.BOOKMARKS_TABLE, values, Bookmark.OSIS + "=?", args);
            } else {
                result = db.insert(DbLibraryHelper.BOOKMARKS_TABLE, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            StaticLogger.error(this, "Add bookmark failed", ex);
        } finally {
            db.endTransaction();
        }

        return result;
    }

    @Override
    public void delete(final Bookmark bookmark) {
        StaticLogger.info(this, String.format("Delete bookmarks %S:%s", bookmark.OSISLink, bookmark.humanLink));
        SQLiteDatabase db = dbLibraryHelper.getDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_NAME, Bookmark.OSIS + "=?", new String[]{bookmark.OSISLink});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public List<Bookmark> getAll(@Nullable Tag tag) {
        SQLiteDatabase db = dbLibraryHelper.getDatabase();
        List<Bookmark> result = new ArrayList<>();
        Cursor cursorB = null;
        try {
            if (tag != null) {
                String query = String.format("SELECT * FROM %1$s WHERE %4$s IN " +
                                "(SELECT %6$s FROM %3$s " +
                                "JOIN %2$s ON %2$s.%8$s=%3$s.%7$s AND %2$s.%5$s=?) ORDER BY %9$s DESC",
                        DbLibraryHelper.BOOKMARKS_TABLE, DbLibraryHelper.TAGS_TABLE,
                        DbLibraryHelper.BOOKMARKS_TAGS_TABLE, Bookmark.KEY_ID, Tag.NAME,
                        BookmarksTags.BOOKMARKSTAGS_BM_ID, BookmarksTags.BOOKMARKSTAGS_TAG_ID,
                        Tag.KEY_ID, Bookmark.TIME);
                cursorB = db.rawQuery(query, new String[]{tag.name});
            } else {
                cursorB = db.rawQuery(String.format("SELECT * FROM %s ORDER BY %s DESC",
                        DbLibraryHelper.BOOKMARKS_TABLE, Bookmark.TIME), null);
            }

            if (!cursorB.moveToFirst()) {
                cursorB.close();
                return result;
            }

            do {
                Bookmark bookmark = Bookmark.fromCursor(cursorB);
                bookmark.tags = getTags(db, bookmark.id);
                result.add(bookmark);
            } while (cursorB.moveToNext());

        } finally {
            if (cursorB != null) {
                cursorB.close();
            }
        }
        return result;
    }

    @NonNull
    private String getTags(SQLiteDatabase db, long bookmarkIDs) {
        StringBuilder result = new StringBuilder();
        try (Cursor cur = db.rawQuery(
                "SELECT bm_tags.tag_name FROM bm_tags WHERE bm_tags.bm_id=?",
                new String[]{String.valueOf(bookmarkIDs)})) {
            if (cur.moveToFirst()) {
                result.append(cur.getString(0));
                while (cur.moveToNext()) {
                    result.append(", ");
                    result.append(cur.getString(0));
                }
            }
        }
        return result.toString();
    }
}
