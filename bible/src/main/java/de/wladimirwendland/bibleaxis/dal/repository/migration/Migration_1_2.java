/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository.migration;

import android.database.sqlite.SQLiteDatabase;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class Migration_1_2 extends Migration {

    public Migration_1_2() {
        super(1, 2);
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        StaticLogger.info(this, String.format("Обновление БД (%d -> %d)", oldVersion, newVersion));
        database.execSQL("ALTER TABLE bookmarks ADD COLUMN name TEXT;");
    }
}
