/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.di.component;

import de.wladimirwendland.bibleaxis.di.module.ActivityModule;
import de.wladimirwendland.bibleaxis.di.scope.PerActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.BookmarksActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.crossreference.CrossReferenceActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.help.HelpActivity;
import de.wladimirwendland.bibleaxis.presentation.history.HistoryActivity;
import de.wladimirwendland.bibleaxis.presentation.imagepreview.ImagePreviewActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.library.LibraryActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.reader.ReaderActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.search.SearchActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.settings.SettingsActivity;

import dagger.Subcomponent;
import de.wladimirwendland.bibleaxis.presentation.splash.SplashActivity;

@PerActivity
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {

    void inject(ImagePreviewActivity activity);
    void inject(ReaderActivity activity);
    void inject(LibraryActivity activity);
    void inject(SplashActivity activity);
    void inject(SettingsActivity activity);
    void inject(SearchActivity activity);
    void inject(HistoryActivity activity);
    void inject(HelpActivity activity);
    void inject(CrossReferenceActivity activity);
    void inject(BookmarksActivity activity);
}
