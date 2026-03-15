/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.logger

import com.google.firebase.crashlytics.FirebaseCrashlytics
import de.wladimirwendland.bibleaxis.domain.logger.Logger

/**
 * Логирование событий в Crashlytics
 *
 */
class CrashlyticsLogger(
    private val crashlytics: FirebaseCrashlytics
) : Logger {

    override fun debug(tag: String, message: String) {
        crashlytics.log(formatMessage("D", tag, message))
    }

    override fun error(tag: String, message: String) {
        crashlytics.log(formatMessage("E", tag, message))
    }

    override fun error(tag: String, message: String, th: Throwable) {
        crashlytics.log(formatMessage("E", tag, message))
        crashlytics.recordException(th)
    }

    override fun info(tag: String, message: String) {
        crashlytics.log(formatMessage("I", tag, message))
    }

    override fun warn(tag: String, message: String) {
        crashlytics.log(formatMessage("W", tag, message))
    }

    private fun formatMessage(logLevel: String, tag: String, message: String) = "$logLevel/$tag: $message"
}