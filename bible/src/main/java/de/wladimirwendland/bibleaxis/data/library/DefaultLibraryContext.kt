/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.library

import de.wladimirwendland.bibleaxis.data.library.LibraryContext.Companion.DIR_LIBRARY
import de.wladimirwendland.bibleaxis.data.library.LibraryContext.Companion.DIR_MODULES
import de.wladimirwendland.bibleaxis.data.library.LibraryContext.Companion.FILE_CACHE
import de.wladimirwendland.bibleaxis.data.library.LibraryContext.Companion.FILE_TSK
import java.io.File

/**
 * Основная реализация контекста библиотеки приложения
 */
class DefaultLibraryContext(
    private val filesDir: File
) : LibraryContext {

    override fun libraryDir(): File = File(filesDir, DIR_LIBRARY)

    override fun libraryCacheFile(): File  = File(libraryDir(), FILE_CACHE)

    override fun modulesDir(): File = File(libraryDir(), DIR_MODULES)

    override fun tskFile(): File = File(libraryDir(), FILE_TSK)
}