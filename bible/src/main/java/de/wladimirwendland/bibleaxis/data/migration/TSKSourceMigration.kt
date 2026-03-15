/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.migration

import android.content.Context
import de.wladimirwendland.bibleaxis.R
import de.wladimirwendland.bibleaxis.data.library.LibraryContext
import de.wladimirwendland.bibleaxis.data.library.LibraryContext.Companion.FILE_TSK
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger
import de.wladimirwendland.bibleaxis.domain.migration.Migration
import java.io.File
import java.io.IOException

class TSKSourceMigration(
    private val libraryContext: LibraryContext,
    private val context: Context,
    version: Int
) : Migration(version) {

    override fun doMigrate() {
        removeOldTSKFile()
        saveTSK()
    }

    override fun getMigrationDescription(): Int {
        return R.string.update_tsk
    }

    private fun removeOldTSKFile() {
        val oldFile = File(context.filesDir, FILE_TSK)
        if (oldFile.exists() && oldFile.delete()) {
            StaticLogger.info(this, "Old TSK file removed")
        }
    }

    private fun saveTSK() {
        StaticLogger.info(this, "Save TSK file")

        val libraryDir = libraryContext.libraryDir()
        if (!libraryDir.exists() && !libraryDir.mkdirs()) {
            throw IOException("Library folder create failed")
        }

        context.resources.openRawResource(R.raw.tsk).use { inputStream ->
            libraryContext.tskFile().outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}