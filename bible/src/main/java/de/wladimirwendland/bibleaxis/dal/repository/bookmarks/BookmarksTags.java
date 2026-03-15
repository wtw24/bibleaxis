/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository.bookmarks;

/**
 * User: Vladimir
 * Date: 23.10.13
 */
public final class BookmarksTags {
	public static final String BOOKMARKSTAGS_KEY_ID = "_id";
	public static final String BOOKMARKSTAGS_BM_ID = "bm_id";
	public static final String BOOKMARKSTAGS_TAG_ID = "tag_id";

	private BookmarksTags() throws InstantiationException {
		throw new InstantiationException("This class is not for instantiation");
	}

}
