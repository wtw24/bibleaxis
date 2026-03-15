/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.managers.history;

import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.exceptions.DataAccessException;
import de.wladimirwendland.bibleaxis.domain.repository.IHistoryRepository;
import de.wladimirwendland.bibleaxis.entity.ItemList;

import java.util.LinkedList;

public class HistoryManager implements IHistoryManager {

	private final int HISTORY_LENGHT;
	private IHistoryRepository repository;

	public HistoryManager(IHistoryRepository repository, int lenght) {
		this.repository = repository;
		this.HISTORY_LENGHT = lenght;
	}

	public synchronized void addLink(BibleReference link) {
		String humanLink = String.format("%1$s: %2$s %3$s",
				link.getModuleID(), link.getBookFullName(),
				link.getChapter());
		ItemList newItem = new ItemList(link.getPath(), humanLink);

		LinkedList<ItemList> history = getLinks();
		if (history.contains(newItem)) {
			history.remove(newItem);
		}
		history.addFirst(newItem);

		while (history.size() > this.HISTORY_LENGHT) {
			history.removeLast();
		}

		repository.save(history);
	}

	public synchronized LinkedList<ItemList> getLinks() {
		try {
			return repository.load();
		} catch (DataAccessException e) {
			return new LinkedList<>();
		}
	}

	@Override
	public void clearLinks() {
		LinkedList<ItemList> history = new LinkedList<>();
		repository.save(history);
	}

	@Override
	public synchronized void deleteLink(ItemList item) {
		LinkedList<ItemList> history = getLinks();
		history.remove(item);
		repository.save(history);
	}
}
