/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.logger

object StaticLogger {

    private var LOGGER_INSTANCE: Logger? = null

    /**
     * Запись в протокол событий отладочного сообщения
     *
     * @param tag имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     */
    @JvmStatic
    fun debug(tag: Any, message: String?) {
        LOGGER_INSTANCE?.debug(getTag(tag), message.orEmpty())
    }

    /**
     * Запись в протокол событий сообщения об ошибке
     *
     * @param tag имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     */
    @JvmStatic
    fun error(tag: Any, message: String?) {
        LOGGER_INSTANCE?.error(getTag(tag), message.orEmpty())
    }

    /**
     * Запись в протокол событий сообщения об ошибке
     *
     * @param tag имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     * @param th ссылка на полученный Exception
     */
    @JvmStatic
    fun error(tag: Any, message: String?, th: Throwable) {
        LOGGER_INSTANCE?.error(getTag(tag), message.orEmpty(), th)
    }

    /**
     * Запись в протокол событий информационного сообщения
     *
     * @param tag имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     */
    @JvmStatic
    fun info(tag: Any, message: String?) {
        LOGGER_INSTANCE?.info(getTag(tag), message.orEmpty())
    }

    /**
     * Запись в протокол событий сообщения с предупреждением
     *
     * @param tag имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     */
    @JvmStatic
    fun warn(tag: Any, message: String?) {
        LOGGER_INSTANCE?.warn(getTag(tag), message.orEmpty())
    }

    @JvmStatic
    fun init(logger: Logger?) {
        LOGGER_INSTANCE = logger
    }

    private fun getTag(src: Any): String =
        if (src is String) {
            src
        } else {
            "${src.javaClass.simpleName}(${src.hashCode()})"
        }
}