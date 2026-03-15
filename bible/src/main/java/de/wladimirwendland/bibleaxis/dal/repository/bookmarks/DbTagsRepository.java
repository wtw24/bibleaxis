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

import de.wladimirwendland.bibleaxis.dal.DbLibraryHelper;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.domain.entity.TagWithCount;
import de.wladimirwendland.bibleaxis.domain.repository.ITagsRepository;

import java.util.ArrayList;
import java.util.List;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class DbTagsRepository implements ITagsRepository {

    @NonNull
    private final DbLibraryHelper dbLibraryHelper;

    public DbTagsRepository(@NonNull DbLibraryHelper dbLibraryHelper) {
        this.dbLibraryHelper = dbLibraryHelper;
    }

    @Override
    public void addTags(long bookmarkIDs, String tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        SQLiteDatabase db = dbLibraryHelper.getDatabase();
        db.beginTransaction();
        try {
            for (String value : tags.split(",")) {
                String tag = value.trim().toLowerCase(); // храним все теги записанные строчными буквами
                if (tag.isEmpty()) {
                    continue;
                }

                long tagIDs;
                Cursor cur = db.rawQuery(
                        String.format("SELECT * FROM %s WHERE %s=?", DbLibraryHelper.TAGS_TABLE, Tag.NAME),
                        new String[]{tag});
                if (cur.moveToFirst()) { // тег существует, берем его идентификатор
                    tagIDs = cur.getLong(cur.getColumnIndex(Tag.KEY_ID));
                } else { // записываем новый тег
                    ContentValues values = new ContentValues(1);
                    values.put(Tag.NAME, tag);
                    tagIDs = db.insert(DbLibraryHelper.TAGS_TABLE, null, values);
                }
                cur.close();

                // привязываем тег к закладке
                ContentValues values = new ContentValues(2);
                values.put(BookmarksTags.BOOKMARKSTAGS_BM_ID, bookmarkIDs);
                values.put(BookmarksTags.BOOKMARKSTAGS_TAG_ID, tagIDs);
                db.insert(DbLibraryHelper.BOOKMARKS_TAGS_TABLE, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            StaticLogger.error(this, "Failed add tags: " + tags, ex);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean deleteTag(String tag) {
        SQLiteDatabase db = dbLibraryHelper.getDatabase();
        db.beginTransaction();
        try {
            db.execSQL(String.format("DELETE FROM %1$s WHERE %2$s=?",
                    DbLibraryHelper.TAGS_TABLE, Tag.NAME),
                    new String[]{tag});
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            StaticLogger.error(this, "Failed delete tag: " + tag, ex);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @Override
    public List<TagWithCount> getTagsWithCount() {
        SQLiteDatabase db = dbLibraryHelper.getDatabase();
        db.beginTransaction();
        List<TagWithCount> result = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery(
                    String.format(
                            "SELECT %1$s.%3$s, %1$s.%4$s, COUNT(%2$s.%5$s) AS count FROM %1$s " +
                                    "LEFT JOIN %2$s ON %1$s.%3$s = %2$s.%5$s GROUP BY %2$s.%5$s ORDER BY %1$s.%4$s",
                            DbLibraryHelper.TAGS_TABLE, DbLibraryHelper.BOOKMARKS_TAGS_TABLE,
                            Tag.KEY_ID, Tag.NAME, BookmarksTags.BOOKMARKSTAGS_TAG_ID), null);
            db.setTransactionSuccessful();
            if (cursor.moveToFirst()) {
                do {
                    result.add(TagWithCount.create(
                            new Tag(cursor.getInt(0), cursor.getString(1)),
                            cursor.getString(2)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            db.endTransaction();
        }

        return result;
    }
}
