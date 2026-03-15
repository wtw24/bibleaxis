/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.di.app

import android.content.Context
import de.wladimirwendland.bibleaxis.R
import dagger.Module
import dagger.Provides
import de.wladimirwendland.bibleaxis.data.config.FeatureToggleRepositoryImpl
import de.wladimirwendland.bibleaxis.domain.config.FeatureToggle
import de.wladimirwendland.bibleaxis.domain.config.FeatureToggleImpl
import javax.inject.Singleton

@Module
class ConfigModule {

    @Singleton
    @Provides
    fun provideFeatureToggle(context: Context): FeatureToggle {
        return FeatureToggleImpl(FeatureToggleRepositoryImpl(context, R.raw.bibleaxis_config))
    }
}
