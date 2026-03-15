/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.search.algorithm;

public interface SearchAlgorithm {

    int indexOf(String source);
    int indexOf(String source, int fromIndex);
    int indexOf(String source, int fromIndex, int toIndex);
}
