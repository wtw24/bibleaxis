/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks;

import de.wladimirwendland.bibleaxis.domain.entity.TagWithCount;
import de.wladimirwendland.bibleaxis.managers.tags.TagsManager;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

public class TagsPresenter extends BasePresenter<TagsView> {

    private OnTagsChangeListener changeListener;
    private List<TagWithCount> tags;
    private TagsManager tagsManager;

    @Inject
    TagsPresenter(TagsManager tagsManager) {
        this.tagsManager = tagsManager;
    }

    void setChangeListener(OnTagsChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    @Override
    public void onViewCreated() {
        if (changeListener == null) {
            throw new IllegalStateException("OnTagsChangeListener is not specified");
        }
        refreshTags();
    }

    void onDeleteTag(int pos) {
        if (pos < tags.size()) {
            TagWithCount tagWithCount = tags.get(pos);
            tagsManager.delete(tagWithCount.tag());
            refreshTags();
            changeListener.onTagsUpdate();
        }
    }

    void onTagSelected(int pos) {
        if (pos < tags.size()) {
            changeListener.onTagSelect(tags.get(pos).tag());
        }
    }

    void refreshTags() {
        tags = tagsManager.getAllWithCount();
        getView().updateTags(tags);
    }
}
