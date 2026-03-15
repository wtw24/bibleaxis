/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository.highlights;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.dal.DbLibraryHelper;
import de.wladimirwendland.bibleaxis.domain.entity.Highlight;
import de.wladimirwendland.bibleaxis.domain.repository.IHighlightsRepository;

import java.util.ArrayList;
import java.util.List;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class DbHighlightsRepository implements IHighlightsRepository {

    @NonNull
    private final DbLibraryHelper dbLibraryHelper;

    public DbHighlightsRepository(@NonNull DbLibraryHelper dbLibraryHelper) {
        this.dbLibraryHelper = dbLibraryHelper;
    }

    @Override
    public long add(@NonNull Highlight highlight) {
        ContentValues values = new ContentValues();
        values.put(Highlight.MODULE_ID, highlight.moduleId);
        values.put(Highlight.BOOK_ID, highlight.bookId);
        values.put(Highlight.CHAPTER, highlight.chapter);
        values.put(Highlight.START_VERSE, highlight.startVerse);
        values.put(Highlight.START_OFFSET, highlight.startOffset);
        values.put(Highlight.END_VERSE, highlight.endVerse);
        values.put(Highlight.END_OFFSET, highlight.endOffset);
        values.put(Highlight.COLOR, highlight.color);
        values.put(Highlight.QUOTE, highlight.quote);
        values.put(Highlight.TIME, highlight.time);

        SQLiteDatabase db = dbLibraryHelper.getDatabase();
        try {
            return db.insert(DbLibraryHelper.HIGHLIGHTS_TABLE, null, values);
        } catch (Exception ex) {
            StaticLogger.error(this, "Failed add highlight", ex);
            return -1;
        }
    }

    @Override
    public boolean delete(long highlightId) {
        SQLiteDatabase db = dbLibraryHelper.getDatabase();
        try {
            return db.delete(
                    DbLibraryHelper.HIGHLIGHTS_TABLE,
                    Highlight.KEY_ID + "=?",
                    new String[]{String.valueOf(highlightId)}
            ) > 0;
        } catch (Exception ex) {
            StaticLogger.error(this, "Failed delete highlight", ex);
            return false;
        }
    }

    @NonNull
    @Override
    public List<Highlight> getByChapter(@NonNull String moduleId, @NonNull String bookId, int chapter) {
        List<Highlight> result = new ArrayList<>();
        SQLiteDatabase db = dbLibraryHelper.getDatabase();

        String query = String.format("SELECT * FROM %s WHERE %s=? AND %s=? AND %s=? ORDER BY %s ASC",
                DbLibraryHelper.HIGHLIGHTS_TABLE,
                Highlight.MODULE_ID,
                Highlight.BOOK_ID,
                Highlight.CHAPTER,
                Highlight.TIME);

        String[] args = {moduleId, bookId, String.valueOf(chapter)};
        try (Cursor cursor = db.rawQuery(query, args)) {
            if (!cursor.moveToFirst()) {
                return result;
            }

            do {
                result.add(Highlight.fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        return result;
    }
}
