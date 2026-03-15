/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.repository;

import de.wladimirwendland.bibleaxis.domain.entity.TagWithCount;

import java.util.List;

/**
 *
 */
public interface ITagsRepository {

    void addTags(long bookmarkIDs, String tags);

    boolean deleteTag(String tag);

    List<TagWithCount> getTagsWithCount();
}
