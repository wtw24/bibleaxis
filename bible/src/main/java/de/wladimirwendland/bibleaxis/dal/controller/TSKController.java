/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.controller;

import de.wladimirwendland.bibleaxis.domain.controller.ITSKController;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.exceptions.BibleAxisException;
import de.wladimirwendland.bibleaxis.domain.exceptions.TskNotFoundException;
import de.wladimirwendland.bibleaxis.domain.repository.ITskRepository;
import de.wladimirwendland.bibleaxis.utils.BibleLinkParser;
import de.wladimirwendland.bibleaxis.utils.CachePool;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TSKController implements ITSKController {

	private static final int MAX_PULL_SIZE = 10;

	private ITskRepository repository;
	private Map<String, LinkedHashSet<BibleReference>> bCrossReferenceCache = Collections
			.synchronizedMap(new CachePool<>(MAX_PULL_SIZE));

	public TSKController(ITskRepository repository) {
		this.repository = repository;
	}

	@Override
	public Set<BibleReference> getLinks(BibleReference reference) throws TskNotFoundException, BibleAxisException {
		if (bCrossReferenceCache.containsKey(reference.getPath())) {
			return bCrossReferenceCache.get(reference.getPath());
		}

		LinkedHashSet<BibleReference> crossReference = BibleLinkParser.parse(
                reference.getModuleID(), getParallels(reference));
		bCrossReferenceCache.put(reference.getPath(), crossReference);

		return crossReference;
	}

	private String getParallels(BibleReference link) throws TskNotFoundException, BibleAxisException {
		String book = link.getBookID();
		String chapter = String.valueOf(link.getChapter());
		String verse = String.valueOf(link.getFromVerse());
		return repository.getReferences(book, chapter, verse);
	}
}
