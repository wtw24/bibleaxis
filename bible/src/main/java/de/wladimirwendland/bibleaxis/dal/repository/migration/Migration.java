/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository.migration;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

/**
 * Базовый класс для выполнения миграции БД
 *
 * @since 07.05.2018
 */
public abstract class Migration implements Comparable<Migration> {

    /**
     * Версия БД, с которой выполняется миграция
     */
    public final int oldVersion;

    /**
     * Версия БД, на которую выполняется миграция
     */
    public final int newVersion;

    public Migration(int oldVersion, int newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    @Override
    public int compareTo(@NonNull Migration o) {
        if (o.oldVersion < this.oldVersion) {
            return 1;
        } else if (o.oldVersion == this.oldVersion && o.newVersion > this.newVersion) {
            return 1;
        } else if (o.oldVersion == this.oldVersion && o.newVersion == this.newVersion) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Migration migration = (Migration) o;

        return oldVersion == migration.oldVersion
                && newVersion == migration.newVersion;
    }

    @Override
    public int hashCode() {
        int result = oldVersion;
        result = 31 * result + newVersion;
        return result;
    }

    /**
     * Запуск процедуры миграции.
     *
     * @param database ссылка на БД, для которой выполняется миграция
     */
    public abstract void migrate(SQLiteDatabase database);
}
