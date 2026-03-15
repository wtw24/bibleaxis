/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository.migration;

import android.database.sqlite.SQLiteDatabase;

import de.wladimirwendland.bibleaxis.dal.DbLibraryHelper;
import de.wladimirwendland.bibleaxis.domain.entity.Highlight;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class Migration_3_4 extends Migration {

    public Migration_3_4() {
        super(3, 4);
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        StaticLogger.info(this, String.format("Обновление БД (%d -> %d)", oldVersion, newVersion));

        String query = "create table " + DbLibraryHelper.HIGHLIGHTS_TABLE + " ("
                + Highlight.KEY_ID + " integer primary key autoincrement, "
                + Highlight.MODULE_ID + " text not null, "
                + Highlight.BOOK_ID + " text not null, "
                + Highlight.CHAPTER + " integer not null, "
                + Highlight.START_VERSE + " integer not null, "
                + Highlight.START_OFFSET + " integer not null, "
                + Highlight.END_VERSE + " integer not null, "
                + Highlight.END_OFFSET + " integer not null, "
                + Highlight.COLOR + " text not null, "
                + Highlight.QUOTE + " text, "
                + Highlight.TIME + " integer not null"
                + ");";

        database.execSQL(query);
    }
}
