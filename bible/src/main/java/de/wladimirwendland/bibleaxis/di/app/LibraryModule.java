/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.di.app;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import de.wladimirwendland.bibleaxis.data.library.DefaultLibraryContext;
import de.wladimirwendland.bibleaxis.data.library.LibraryContext;

/**
 * Dagger-модуль с зависимостями для библиотеки приложения
 *
 */
@Module
public interface LibraryModule {

    @Provides
    static LibraryContext provideLibraryContext(Context context) {
        return new DefaultLibraryContext(context.getFilesDir());
    }
}
