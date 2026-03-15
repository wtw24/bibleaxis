/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.di.component;

import de.wladimirwendland.bibleaxis.di.module.FragmentModule;
import de.wladimirwendland.bibleaxis.di.scope.PerFragment;
import de.wladimirwendland.bibleaxis.presentation.dialogs.BookmarksDialog;
import de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.BookmarksFragment;
import de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.TagsFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = {FragmentModule.class})
public interface FragmentComponent {

    void inject(BookmarksDialog fragment);

    void inject(BookmarksFragment fragment);

    void inject(TagsFragment fragment);
}
