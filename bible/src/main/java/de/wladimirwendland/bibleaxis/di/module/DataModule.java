/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.di.module;

import android.content.Context;

import de.wladimirwendland.bibleaxis.dal.DbLibraryHelper;
import de.wladimirwendland.bibleaxis.dal.repository.FsHistoryRepository;
import de.wladimirwendland.bibleaxis.dal.repository.XmlTskRepository;
import de.wladimirwendland.bibleaxis.dal.repository.bookmarks.DbBookmarksRepository;
import de.wladimirwendland.bibleaxis.dal.repository.bookmarks.DbTagsRepository;
import de.wladimirwendland.bibleaxis.dal.repository.highlights.DbHighlightsRepository;
import de.wladimirwendland.bibleaxis.data.backup.HighlightsBackupManager;
import de.wladimirwendland.bibleaxis.domain.repository.IBookmarksRepository;
import de.wladimirwendland.bibleaxis.domain.repository.IHistoryRepository;
import de.wladimirwendland.bibleaxis.domain.repository.IHighlightsRepository;
import de.wladimirwendland.bibleaxis.domain.repository.ITagsRepository;
import de.wladimirwendland.bibleaxis.domain.repository.ITskRepository;
import de.wladimirwendland.bibleaxis.domain.threading.AppTaskRunner;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import de.wladimirwendland.bibleaxis.data.library.LibraryContext;

@Module
public class DataModule {

    @Provides
    @Singleton
    IBookmarksRepository getBookmarksRepository(DbLibraryHelper dbLibraryHelper) {
        return new DbBookmarksRepository(dbLibraryHelper);
    }

    @Provides
    @Singleton
    ITagsRepository getBookmarksTagsRepository(DbLibraryHelper dbLibraryHelper) {
        return new DbTagsRepository(dbLibraryHelper);
    }

    @Provides
    @Singleton
    IHighlightsRepository getHighlightsRepository(DbLibraryHelper dbLibraryHelper) {
        return new DbHighlightsRepository(dbLibraryHelper);
    }

    @Provides
    @Singleton
    HighlightsBackupManager provideHighlightsBackupManager(
            Context context,
            PreferenceHelper preferenceHelper,
            DbLibraryHelper dbLibraryHelper,
            AppTaskRunner appTaskRunner
    ) {
        return new HighlightsBackupManager(context, preferenceHelper, dbLibraryHelper, appTaskRunner);
    }

    @Provides
    @Singleton
    DbLibraryHelper getDbLibraryHelper(Context context) {
        return new DbLibraryHelper(context);
    }

    @Provides
    IHistoryRepository getHistoryRepository(Context context) {
        return new FsHistoryRepository(context);
    }

    @Provides
    ITskRepository getTskRepository(LibraryContext context) {
        return new XmlTskRepository(context.tskFile());
    }

    @Provides
    OkHttpClient provideOkHttpClient(Context context) {
        return new OkHttpClient.Builder()
                .cache(new Cache(context.getCacheDir(), 5_000_000))
                .build();
    }
}
