/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks;

import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.domain.entity.TagWithCount;
import de.wladimirwendland.bibleaxis.managers.tags.TagsManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("WeakerAccess")
public class TagsPresenterTest {

    @Mock TagsManager tagsManager;
    @Mock TagsView view;
    @Mock OnTagsChangeListener changeListener;

    private TagsPresenter presenter;
    private List<TagWithCount> tags;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter = new TagsPresenter(tagsManager);
        presenter.attachView(view);

        tags = Arrays.asList(
                TagWithCount.create(new Tag(1, "abraham"), "2"),
                TagWithCount.create(new Tag(2, "God"), "3"));
        when(tagsManager.getAllWithCount()).thenReturn(tags);
    }

    @Test(expected = IllegalStateException.class)
    public void onViewCreatedWithNoChangeListener() throws Exception {
        presenter.onViewCreated();
    }

    @Test
    public void onViewCreated() throws Exception {
        presenter.setChangeListener(changeListener);
        presenter.onViewCreated();
        verify(view).updateTags(anyList());
    }

    @Test
    public void onDeleteTagIllegalPosition() throws Exception {
        presenter.setChangeListener(changeListener);
        presenter.onViewCreated();
        Mockito.reset(view);

        presenter.onDeleteTag(tags.size());

        verify(tagsManager, never()).delete(any(Tag.class));
        verify(view, never()).updateTags(anyList());
    }

    @Test
    public void onDeleteTag() throws Exception {
        presenter.setChangeListener(changeListener);
        presenter.onViewCreated();
        Mockito.reset(view);

        presenter.onDeleteTag(0);

        verify(tagsManager).delete(any(Tag.class));
        verify(view).updateTags(anyList());
        verify(changeListener).onTagsUpdate();
    }

    @Test
    public void onTagSelectedIllegalPosition() throws Exception {
        presenter.setChangeListener(changeListener);
        presenter.onViewCreated();
        Mockito.reset(view);

        presenter.onTagSelected(tags.size());

        verify(changeListener, never()).onTagSelect(any(Tag.class));
    }

    @Test
    public void onTagSelected() throws Exception {
        presenter.setChangeListener(changeListener);
        presenter.onViewCreated();
        Mockito.reset(view);

        presenter.onTagSelected(0);

        verify(changeListener).onTagSelect(any(Tag.class));
    }

    @Test
    public void refreshTags() throws Exception {
        presenter.refreshTags();
        verify(view).updateTags(anyList());
    }
}