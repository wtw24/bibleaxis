/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.async.AsyncManager;
import de.wladimirwendland.bibleaxis.data.backup.HighlightsBackupManager;
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;
import de.wladimirwendland.bibleaxis.domain.repository.IBookmarksRepository;
import de.wladimirwendland.bibleaxis.domain.threading.AppTaskRunner;
import de.wladimirwendland.bibleaxis.managers.Librarian;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

import javax.inject.Inject;

import de.wladimirwendland.bibleaxis.di.app.AppComponent;
import de.wladimirwendland.bibleaxis.di.app.DaggerAppComponent;
import de.wladimirwendland.bibleaxis.domain.logger.Logger;
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class BibleAxisApp extends Application implements Thread.UncaughtExceptionHandler {

    private static BibleAxisApp instance;

    @Inject AsyncManager asyncManager;
    @Inject IBookmarksRepository bookmarksRepository;
    @Inject Librarian librarian;
    @Inject ILibraryController libraryController;
    @Inject PreferenceHelper prefHelper;
    @Inject AppTaskRunner appTaskRunner;
    @Inject HighlightsBackupManager highlightsBackupManager;
    @Inject Logger logger;

    private AppComponent appComponent;

    private final Thread.UncaughtExceptionHandler exceptionHandler;

    public BibleAxisApp() {
        super();
        instance = this;
        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static BibleAxisApp getInstance() {
        return instance;
    }

    public static BibleAxisApp instance(Context context) {
        return (BibleAxisApp) context.getApplicationContext();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public AsyncManager getAsyncManager() {
        return asyncManager;
    }

    public Librarian getLibrarian() {
        return librarian;
    }

    public ILibraryController getLibraryController() {
        return libraryController;
    }

    public PreferenceHelper getPrefHelper() {
        return prefHelper;
    }

    public AppTaskRunner getAppTaskRunner() {
        return appTaskRunner;
    }

    public HighlightsBackupManager getHighlightsBackupManager() {
        return highlightsBackupManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.factory().create(this);
        appComponent.inject(this);

        StaticLogger.init(logger);
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, @NonNull Throwable ex) {
        logger.error(thread.getName(), Log.getStackTraceString(ex));
        if (exceptionHandler != null) {
            exceptionHandler.uncaughtException(thread, ex);
        }
    }
}
