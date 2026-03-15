/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.migration

import android.content.Context
import de.wladimirwendland.bibleaxis.R
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController
import de.wladimirwendland.bibleaxis.utils.DataConstants
import de.wladimirwendland.bibleaxis.data.library.LibraryContext
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger
import de.wladimirwendland.bibleaxis.domain.migration.Migration
import java.io.File
import java.io.IOException

/**
 * Класс для обновления встроенных модулей приложения
 *
 * Копирует модули из папки ресурсов приложения в папку для модулей
 *
 */
class MigrationUpdateBuiltinModules(
    private val libraryContext: LibraryContext,
    private val libraryController: ILibraryController,
    private val context: Context,
    versionCode: Int
) : Migration(versionCode) {

    private val modules = mapOf(
        R.raw.bible_rst to RST_FILE_NAME,
        R.raw.bible_ubio to UBIO_FILE_NAME,
        R.raw.bible_kjv to KJV_FILE_NAME
    )

    override fun doMigrate() {
        StaticLogger.info(this, "Update built-in modules into ${libraryContext.libraryDir()}")

        // Удаление ранее скопированных встроенных модулей и файлов библиотеки
        DataConstants.getLibraryPath(context).deleteRecursively()
        File(context.filesDir, LibraryContext.FILE_CACHE).delete()

        val modulesDir = libraryContext.modulesDir()
        if (!modulesDir.exists() && !modulesDir.mkdirs()) {
            throw IOException("Modules folder create failed")
        }

        modules.entries.forEach { (resId, moduleFile) ->
            File(modulesDir, moduleFile).outputStream().use {
                context.resources.openRawResource(resId).copyTo(it)
            }
        }

        libraryController.reloadModules()
    }

    override fun getMigrationDescription(): Int {
        return R.string.update_builtin_modules
    }

    private companion object {
        private const val RST_FILE_NAME = "bible_rst.zip"
        private const val UBIO_FILE_NAME = "bible_ubio.zip"
        private const val KJV_FILE_NAME = "bible_kjv.zip"
    }
}