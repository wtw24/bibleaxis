/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.repository;

import androidx.annotation.Nullable;

import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;

import java.util.List;

public interface IBookmarksRepository {

    /**
     * Добавляет новую закладку или обновляет существующую.
     *
     * @param bookmark закладка для добавления
     *
     * @return уникальный идентификатор добавленной закладки
     */
    long add(Bookmark bookmark);

    void delete(Bookmark bookmark);

    List<Bookmark> getAll(@Nullable Tag tag);
}
