/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.repository;

import de.wladimirwendland.bibleaxis.domain.exceptions.DataAccessException;
import de.wladimirwendland.bibleaxis.entity.ItemList;

import java.util.LinkedList;

public interface IHistoryRepository {
	void save(LinkedList<ItemList> list);

	LinkedList<ItemList> load() throws DataAccessException;
}
