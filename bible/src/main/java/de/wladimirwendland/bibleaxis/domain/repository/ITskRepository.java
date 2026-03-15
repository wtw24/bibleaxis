/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.repository;

import de.wladimirwendland.bibleaxis.domain.exceptions.BibleAxisException;
import de.wladimirwendland.bibleaxis.domain.exceptions.TskNotFoundException;

public interface ITskRepository {
    String getReferences(String book, String chapter, String verse) throws TskNotFoundException, BibleAxisException;
}
