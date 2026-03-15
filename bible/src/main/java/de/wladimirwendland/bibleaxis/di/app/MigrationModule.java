/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.di.app;

import android.content.Context;

import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import de.wladimirwendland.bibleaxis.data.library.LibraryContext;
import de.wladimirwendland.bibleaxis.data.migration.MigrationUpdateBuiltinModules;
import de.wladimirwendland.bibleaxis.data.migration.TSKSourceMigration;
import de.wladimirwendland.bibleaxis.domain.migration.Migration;

/**
 * Dagger-модуль с классами, реализующими {@link Migration}
 *
 */
@Module
public interface MigrationModule {

    @IntoSet
    @Provides
    static Migration provideUpdateBuiltinModulesMigration(LibraryContext libraryContext,
                                                          ILibraryController libraryController,
                                                          Context context) {
        return new MigrationUpdateBuiltinModules(libraryContext, libraryController, context, 8);
    }

    @IntoSet
    @Provides
    static Migration provideTSKSourceMigration(LibraryContext libraryContext, Context context) {
        return new TSKSourceMigration(libraryContext, context, 8);
    }
}
