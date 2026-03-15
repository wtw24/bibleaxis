/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks;

import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;

interface OnBookmarksChangeListener {

    void onBookmarksSelect(Bookmark osisLink);

    void onBookmarksUpdate();
}
