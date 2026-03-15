/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.entity;

import android.database.Cursor;

import androidx.annotation.NonNull;

public class Highlight {

    public static final String KEY_ID = "_id";
    public static final String MODULE_ID = "module_id";
    public static final String BOOK_ID = "book_id";
    public static final String CHAPTER = "chapter";
    public static final String START_VERSE = "start_verse";
    public static final String START_OFFSET = "start_offset";
    public static final String END_VERSE = "end_verse";
    public static final String END_OFFSET = "end_offset";
    public static final String COLOR = "color";
    public static final String QUOTE = "quote";
    public static final String TIME = "time";

    public long id;
    public String moduleId;
    public String bookId;
    public int chapter;
    public int startVerse;
    public int startOffset;
    public int endVerse;
    public int endOffset;
    public String color;
    public String quote;
    public long time;

    public Highlight(String moduleId,
                     String bookId,
                     int chapter,
                     int startVerse,
                     int startOffset,
                     int endVerse,
                     int endOffset,
                     String color,
                     String quote) {
        this(0L, moduleId, bookId, chapter, startVerse, startOffset, endVerse, endOffset, color, quote, System.currentTimeMillis());
    }

    public Highlight(long id,
                     String moduleId,
                     String bookId,
                     int chapter,
                     int startVerse,
                     int startOffset,
                     int endVerse,
                     int endOffset,
                     String color,
                     String quote,
                     long time) {
        this.id = id;
        this.moduleId = moduleId;
        this.bookId = bookId;
        this.chapter = chapter;
        this.startVerse = startVerse;
        this.startOffset = startOffset;
        this.endVerse = endVerse;
        this.endOffset = endOffset;
        this.color = color;
        this.quote = quote;
        this.time = time;
    }

    @NonNull
    public static Highlight fromCursor(Cursor cursor) {
        return new Highlight(
                cursor.getLong(cursor.getColumnIndex(KEY_ID)),
                cursor.getString(cursor.getColumnIndex(MODULE_ID)),
                cursor.getString(cursor.getColumnIndex(BOOK_ID)),
                cursor.getInt(cursor.getColumnIndex(CHAPTER)),
                cursor.getInt(cursor.getColumnIndex(START_VERSE)),
                cursor.getInt(cursor.getColumnIndex(START_OFFSET)),
                cursor.getInt(cursor.getColumnIndex(END_VERSE)),
                cursor.getInt(cursor.getColumnIndex(END_OFFSET)),
                cursor.getString(cursor.getColumnIndex(COLOR)),
                cursor.getString(cursor.getColumnIndex(QUOTE)),
                cursor.getLong(cursor.getColumnIndex(TIME))
        );
    }
}
