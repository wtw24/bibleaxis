/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.managers.tags;

import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.domain.entity.TagWithCount;
import de.wladimirwendland.bibleaxis.domain.repository.ITagsRepository;

import java.util.List;

/**
 * User: Vladimir
 * Date: 10.10.13
 */
public class TagsManager {

	private ITagsRepository tagsRepository;

	public TagsManager(ITagsRepository tagsRepository) {
		this.tagsRepository = tagsRepository;
	}

	public boolean delete(Tag tag) {
		return tagsRepository.deleteTag(tag.name);
	}

	public List<TagWithCount> getAllWithCount() {
		return tagsRepository.getTagsWithCount();
	}
}
