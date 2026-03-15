/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async.task

import android.content.Context
import android.net.Uri
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController
import de.wladimirwendland.bibleaxis.utils.FilenameUtils
import de.wladimirwendland.bibleaxis.utils.Task
import de.wladimirwendland.bibleaxis.data.library.LibraryContext
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger.error
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger.info
import java.io.File
import java.lang.ref.WeakReference

/**
 * @author ru_phoenix
 * @version 1.0
 */
class LoadModuleFromFile(
    context: Context,
    message: String,
    private val uri: Uri,
    private val libraryController: ILibraryController,
    private val libraryContext: LibraryContext
) : Task(message, false) {
    private val weakContext: WeakReference<Context> = WeakReference(context.applicationContext)

    var statusCode = StatusCode.Success
        private set

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg arg0: String): Boolean {
        info(this, "Load module from $uri")

        val modulesDir = libraryContext.modulesDir()
        if (!modulesDir.exists() && !modulesDir.mkdirs()) {
            statusCode = StatusCode.LibraryNotFound
            error(this, "Library directory not found")
            return false
        }

        val context = weakContext.get() ?: return false
        val contentResolver = context.contentResolver

        val type = contentResolver.getType(uri)
        if ("application/zip" != type) {
            statusCode = StatusCode.FileNotSupported
            return false
        }

        val fileName = resolveTargetFileName(context, uri)

        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val target = File(modulesDir, fileName)
                stream.copyTo(target.outputStream())
                libraryController.loadModule(target)
            }
        } catch (e: Exception) {
            error(this, e.message, e)
            statusCode = StatusCode.MoveFailed
            return false
        }

        return true
    }

    enum class StatusCode {
        Success, FileNotExist, FileNotSupported, MoveFailed, LibraryNotFound
    }

    private fun resolveTargetFileName(context: Context, uri: Uri): String {
        val sourceName = FilenameUtils.getFileName(context, uri)
        if (!sourceName.isNullOrBlank()) {
            return if (sourceName.endsWith(".zip", ignoreCase = true)) {
                sourceName
            } else {
                "$sourceName.zip"
            }
        }

        return "imported_module_${System.currentTimeMillis()}.zip"
    }
}
