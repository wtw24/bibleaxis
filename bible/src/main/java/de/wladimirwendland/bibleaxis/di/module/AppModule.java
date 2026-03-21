/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.di.module;

import static java.util.Collections.singletonList;

import android.content.Context;

import de.wladimirwendland.bibleaxis.BuildConfig;
import de.wladimirwendland.bibleaxis.async.AsyncManager;
import de.wladimirwendland.bibleaxis.dal.controller.CachedLibraryRepository;
import de.wladimirwendland.bibleaxis.dal.controller.FsLibraryController;
import de.wladimirwendland.bibleaxis.dal.controller.TSKController;
import de.wladimirwendland.bibleaxis.dal.repository.BibleAxisModuleRepository;
import de.wladimirwendland.bibleaxis.dal.repository.FsCacheRepository;
import de.wladimirwendland.bibleaxis.dal.repository.FsLibraryLoader;
import de.wladimirwendland.bibleaxis.domain.AnalyticsHelper;
import de.wladimirwendland.bibleaxis.domain.analytics.FirebaseAnalyticsHelper;
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;
import de.wladimirwendland.bibleaxis.domain.controller.ITSKController;
import de.wladimirwendland.bibleaxis.domain.repository.ICacheRepository;
import de.wladimirwendland.bibleaxis.domain.repository.IHistoryRepository;
import de.wladimirwendland.bibleaxis.domain.repository.ITskRepository;
import de.wladimirwendland.bibleaxis.domain.repository.LibraryLoader;
import de.wladimirwendland.bibleaxis.domain.threading.AppTaskRunner;
import de.wladimirwendland.bibleaxis.managers.history.HistoryManager;
import de.wladimirwendland.bibleaxis.managers.history.IHistoryManager;
import de.wladimirwendland.bibleaxis.utils.FsUtilsWrapper;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.wladimirwendland.bibleaxis.data.library.LibraryContext;
import de.wladimirwendland.bibleaxis.data.logger.AndroidLogger;
import de.wladimirwendland.bibleaxis.data.logger.CrashlyticsLogger;
import de.wladimirwendland.bibleaxis.domain.RxSchedulers;
import de.wladimirwendland.bibleaxis.domain.logger.CompositeLogger;
import de.wladimirwendland.bibleaxis.domain.logger.Logger;
import de.wladimirwendland.bibleaxis.domain.migration.Migration;
import de.wladimirwendland.bibleaxis.domain.migration.UpdateManager;

@Module
public class AppModule {

    @Provides
    @Singleton
    AsyncManager getAsyncManager() {
        return new AsyncManager();
    }

    @Provides
    IHistoryManager getHistoryManager(IHistoryRepository repository, PreferenceHelper prefHelper) {
        return new HistoryManager(repository, prefHelper.getHistorySize());
    }

    @Provides
    @Singleton
    ILibraryController getLibraryController(LibraryContext context, LibraryLoader repository) {
        ICacheRepository cacheRepository = new FsCacheRepository(context.libraryCacheFile());
        return new FsLibraryController(repository, new CachedLibraryRepository(cacheRepository));
    }

    @Provides
    LibraryLoader getLibraryLoader(LibraryContext libraryContext) {
        List<File> modulesDir = singletonList(libraryContext.modulesDir());
        final FsUtilsWrapper fsUtils = new FsUtilsWrapper();
        return new FsLibraryLoader(modulesDir, new BibleAxisModuleRepository(fsUtils));
    }

    @Provides
    PreferenceHelper getPreferenceHelper(Context context) {
        return new PreferenceHelper(context);
    }

    @Provides
    ITSKController getTskController(ITskRepository repository) {
        return new TSKController(repository);
    }

    @Provides
    AnalyticsHelper analyticsHelper(Context context) {
        return new FirebaseAnalyticsHelper();
    }

    @Provides
    @Singleton
    Logger provideLogger() {
        final List<Logger> loggers = new ArrayList<>();
       loggers.add(new CrashlyticsLogger(FirebaseCrashlytics.getInstance()));
        if (BuildConfig.DEBUG) {
            loggers.add(new AndroidLogger());
        }

        return new CompositeLogger(loggers);
    }

    @Singleton
    @Provides
    UpdateManager provideUpdateManager(PreferenceHelper prefHelper, Set<Migration> migrations) {
        return new UpdateManager(prefHelper, migrations);
    }


    @Singleton
    @Provides
    RxSchedulers  provideRxSchedulers() {
        return new RxSchedulers();
    }

    @Singleton
    @Provides
    AppTaskRunner provideAppTaskRunner() {
        return new AppTaskRunner();
    }
}
