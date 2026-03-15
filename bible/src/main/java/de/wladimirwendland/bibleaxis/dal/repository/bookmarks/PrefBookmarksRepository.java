/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository.bookmarks;

import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.domain.repository.IBookmarksRepository;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Vladimir Yakushev
 * Date: 09.04.13
 * Time: 0:26
 */
public class PrefBookmarksRepository implements IBookmarksRepository {

	private static final Byte BOOKMARK_DELIMITER = (byte) 0xFE;
	private static final Byte BOOKMARK_PATH_DELIMITER = (byte) 0xFF;
    private static final String KEY_FAVORITS = "Favorits";

	private PreferenceHelper preferenceHelper;

	public PrefBookmarksRepository(PreferenceHelper preferenceHelper) {
		this.preferenceHelper = preferenceHelper;
	}

	@Override
	public long add(Bookmark bookmark) {
        String fav = preferenceHelper.getString(KEY_FAVORITS);
        preferenceHelper.saveString(KEY_FAVORITS, bookmark.humanLink + BOOKMARK_PATH_DELIMITER + bookmark.OSISLink + BOOKMARK_DELIMITER + fav);
        return 0;
    }

	@Override
	public void delete(Bookmark bookmark) {
        String fav = preferenceHelper.getString(KEY_FAVORITS);
        fav = fav.replaceAll(String.format("%s(.)+?%s", bookmark.humanLink, BOOKMARK_DELIMITER), "");
        preferenceHelper.saveString(KEY_FAVORITS, fav);
    }

    @Override
    public List<Bookmark> getAll(Tag tag) {
        return new ArrayList<>();
	}
}
