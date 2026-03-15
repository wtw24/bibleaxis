/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.di.module;

import de.wladimirwendland.bibleaxis.domain.repository.IBookmarksRepository;
import de.wladimirwendland.bibleaxis.domain.repository.ITagsRepository;
import de.wladimirwendland.bibleaxis.managers.bookmarks.BookmarksManager;
import de.wladimirwendland.bibleaxis.managers.tags.TagsManager;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentModule {

    @Provides
    BookmarksManager provideBookmarksManager(IBookmarksRepository bmRepo, ITagsRepository bmtRepo) {
        return new BookmarksManager(bmRepo, bmtRepo);
    }

    @Provides
    TagsManager provideTagsManager(ITagsRepository bmtRepo) {
        return new TagsManager(bmtRepo);
    }
}
