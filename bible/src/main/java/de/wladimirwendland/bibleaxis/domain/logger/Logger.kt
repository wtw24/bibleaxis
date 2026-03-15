/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.logger

interface Logger {
    /**
     * Запись в протокол событий отладочного сообщения
     *
     * @param tag     имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     */
    fun debug(tag: String, message: String)

    /**
     * Запись в протокол событий сообщения об ошибке
     *
     * @param tag     имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     */
    fun error(tag: String, message: String)

    /**
     * Запись в протокол событий сообщения об ошибке
     *
     * @param tag     имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     * @param th      ссылка на полученный Exception
     */
    fun error(tag: String, message: String, th: Throwable)

    /**
     * Запись в протокол событий информационного сообщения
     *
     * @param tag     имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     */
    fun info(tag: String, message: String)

    /**
     * Запись в протокол событий сообщения с предупреждением
     *
     * @param tag     имя класса-инициатора события
     * @param message текст помещаемый в протокол событий
     */
    fun warn(tag: String, message: String)
}