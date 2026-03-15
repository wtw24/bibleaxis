/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks;

import de.wladimirwendland.bibleaxis.domain.entity.TagWithCount;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseView;

import java.util.List;


public interface TagsView extends BaseView {

    void updateTags(List<TagWithCount> items);

    void refreshTags();
}
