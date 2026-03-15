/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.logger

/**
 * Реализация [Logger] позволяющая отправить сообщения с логами в несколько назначений
 *
 * @property loggerList назначения, в который будут рассылаться сообщения логов
 */
class CompositeLogger(private val loggerList: List<Logger>) : Logger {

    override fun debug(tag: String, message: String) {
        loggerList.forEach { it.debug(tag, message) }
    }

    override fun error(tag: String, message: String) {
        loggerList.forEach { it.error(tag, message) }
    }

    override fun error(tag: String, message: String, th: Throwable) {
        loggerList.forEach { it.error(tag, message, th) }
    }

    override fun info(tag: String, message: String) {
        loggerList.forEach { it.info(tag, message) }
    }

    override fun warn(tag: String, message: String) {
        loggerList.forEach { it.warn(tag, message) }
    }
}