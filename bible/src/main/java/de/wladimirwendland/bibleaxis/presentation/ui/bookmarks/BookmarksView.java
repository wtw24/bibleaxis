/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseView;

import java.util.List;


public interface BookmarksView extends BaseView {

    void openBookmarkDialog(Bookmark bookmark);

    void updateBookmarks(@NonNull List<Bookmark> bookmarks);

    void setTagFilter(Tag tag);

    void startBookmarkAction(String title);

    void refreshBookmarks();
}
