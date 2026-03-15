/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.textFormatters;

/**
 * Интерфейс для форматирования текста с учетом особенностей модуля
 *
 * @author Vladimir Yakushev
 * @version 1.0 of 11.2015
 */
public interface ITextFormatter {
    String format(String text);
}
