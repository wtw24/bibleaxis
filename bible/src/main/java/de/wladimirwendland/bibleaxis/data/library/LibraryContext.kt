/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.library

import java.io.File

/**
 * Контекст библиотеки приложения, хранящий данные о размещении различных модулей
 * для работы с Библией
 *
 */
interface LibraryContext {

    /**
     * Корневая директория с файлами для работы с библиотекой
     */
    fun libraryDir(): File

    /**
     * Файл со списком модулей
     */
    fun libraryCacheFile(): File

    /**
     * Директория с файлами модулей (лежит внутри корневой директории библиотеки)
     */
    fun modulesDir(): File

    /**
     * Файл с параллельными местами Писания
     */
    fun tskFile(): File

    companion object {
        const val DIR_LIBRARY = "library"
        const val DIR_MODULES = "modules"
        const val FILE_CACHE = "library.cache"
        const val FILE_TSK = "tsk.xml"
    }
}