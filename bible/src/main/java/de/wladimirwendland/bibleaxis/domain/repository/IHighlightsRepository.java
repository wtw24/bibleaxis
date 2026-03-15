/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.repository;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.entity.Highlight;

import java.util.List;

public interface IHighlightsRepository {

    long add(@NonNull Highlight highlight);

    boolean delete(long highlightId);

    @NonNull
    List<Highlight> getByChapter(@NonNull String moduleId, @NonNull String bookId, int chapter);
}
