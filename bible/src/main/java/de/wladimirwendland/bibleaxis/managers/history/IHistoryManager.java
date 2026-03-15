/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.managers.history;

import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.entity.ItemList;

import java.util.LinkedList;

public interface IHistoryManager {
	void addLink(BibleReference link);

	void clearLinks();

	void deleteLink(ItemList item);

	LinkedList<ItemList> getLinks();
}
