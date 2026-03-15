/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;

public interface AnalyticsHelper {

    String ATTR_ACTION = "action";
    String ATTR_BOOK = "book";
    String ATTR_MODULE = "module";
    String ATTR_OPEN_TAG = "tags";
    String ATTR_LINK = "link";
    String ATTR_QUERY = "query";

    String CATEGORY_ADD_TAGS = "add_tags";
    String CATEGORY_ADD_BOOKMARK = "add_bookmark";
    String CATEGORY_CLICK = "click";
    String CATEGORY_MODULES = "modules";
    String CATEGORY_SEARCH = "search";

    /**
     * Событие открытия места в модуле
     *
     * @param link ссылка на место
     */
    void moduleEvent(@NonNull BibleReference link);

    /**
     * Событие создания закладки на место в модуле
     *
     * @param bookmark созданная закладка
     */
    void bookmarkEvent(@NonNull Bookmark bookmark);

    /**
     * Событие выбора функционала приложения
     *
     * @param action имя выбранного функционала
     */
    void clickEvent(@NonNull String action);

    /**
     * Событие поиска по модулю
     *
     * @param query  поисковый запрос
     * @param module модуль, в котором осуществляется поиск
     */
    void searchEvent(@NonNull String query, String module);
}
