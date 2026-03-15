/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.cache

interface Cache<T> {

    fun getOrNull(key: String): T?

    fun getOrDefault(key: String, defaultValue: T): T

    fun put(key: String, value: T)
}