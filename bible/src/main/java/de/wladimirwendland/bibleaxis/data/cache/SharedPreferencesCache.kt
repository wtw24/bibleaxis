/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.cache

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

class SharedPreferencesCache<T>(
        context: Context,
        preferencesName: String
) : Cache<T> {

    private val preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE)

    @Suppress("UNCHECKED_CAST")
    override fun getOrNull(key: String): T? {
        return preferences.all[key] as T?
    }

    @Suppress("UNCHECKED_CAST")
    override fun getOrDefault(key: String, defaultValue: T): T =
            preferences.all.getOrDefault(key, defaultValue) as T

    override fun put(key: String, value: T) {
        preferences.edit {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                is Long -> putLong(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                else -> throw UnsupportedOperationException()
            }
        }
    }
}