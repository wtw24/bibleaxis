/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.repository;

import de.wladimirwendland.bibleaxis.domain.entity.ModuleList;
import de.wladimirwendland.bibleaxis.domain.exceptions.DataAccessException;

/**
 *
 */
public interface ICacheRepository {

    ModuleList getData() throws DataAccessException;

    boolean isCacheExist();

    void saveData(ModuleList data) throws DataAccessException;
}
