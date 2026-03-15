/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository.migration;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class Migration_2_3 extends Migration {

    private static final Object TAG = Migration_2_3.class.getSimpleName();

    public Migration_2_3() {
        super(2, 3);
    }

    @Override
    public void migrate(SQLiteDatabase db) {
        StaticLogger.info(this, String.format("Обновление БД (%d -> %d)", oldVersion, newVersion));

        db.execSQL("ALTER TABLE bookmarks ADD COLUMN time INTEGER NOT NULL DEFAULT 0;");
        setBookmarksTime(db);

        // Добавляем триггеры

        db.execSQL(// удаляем ссылки на теги при удалении закладки
                "CREATE TRIGGER delete_bookmark AFTER DELETE ON bookmarks FOR EACH ROW " +
                        "BEGIN " +
                        "DELETE FROM bookmarks_tags WHERE OLD._id=bookmarks_tags.bm_id; " +
                        "END");
        db.execSQL( // при удалении ссылок на теги удаляем непривязанные теги
                "CREATE TRIGGER delete_bookmark_tags AFTER DELETE ON bookmarks_tags " +
                        "BEGIN " +
                        "DELETE FROM tags WHERE tags._id NOT IN (SELECT DISTINCT bookmarks_tags.tag_id FROM bookmarks_tags); " +
                        "END");
        db.execSQL( // при обновлении закладки удаляем все ссылки на теги (теги надо добавить заново)
                "CREATE TRIGGER update_bookmark AFTER UPDATE ON bookmarks FOR EACH ROW " +
                        "BEGIN " +
                        "DELETE FROM bookmarks_tags WHERE OLD._id=bookmarks_tags.bm_id; " +
                        "END");
        db.execSQL( // при удалении тега удаляем ссылки на него
                "CREATE TRIGGER delete_tag AFTER DELETE ON tags FOR EACH ROW " +
                        "BEGIN " +
                        "DELETE FROM bookmarks_tags WHERE OLD._id=bookmarks_tags.tag_id; " +
                        "END");

        // Добавляем представления

        db.execSQL(
                "CREATE VIEW bm_tags AS " +
                        "SELECT bookmarks_tags.bm_id AS bm_id, tags._id AS tag_id, tags.name AS tag_name " +
                        "FROM bookmarks_tags " +
                        "JOIN tags ON bookmarks_tags.tag_id=tags._id " +
                        "ORDER BY bookmarks_tags.bm_id;");
    }

    public static void setBookmarksTime(SQLiteDatabase database) {
        try (Cursor cursor = database.rawQuery("SELECT * FROM bookmarks", null)) {
            if (!cursor.moveToFirst()) {
                return;
            }

            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            String date;
            do {
                date = cursor.getString(cursor.getColumnIndex(Bookmark.DATE));
                ContentValues cv = new ContentValues(1);
                try {
                    cv.put(Bookmark.TIME, Objects.requireNonNull(dateFormat.parse(date)).getTime());
                } catch (Exception ex) {
                    StaticLogger.error(TAG, "Failure update time", ex);
                    cv.put(Bookmark.TIME, new Date().getTime());
                }
                String id = cursor.getString(cursor.getColumnIndex(Bookmark.KEY_ID));
                database.update("bookmarks", cv, "_id=?", new String[]{id});
            } while (cursor.moveToNext());
        }
    }
}
