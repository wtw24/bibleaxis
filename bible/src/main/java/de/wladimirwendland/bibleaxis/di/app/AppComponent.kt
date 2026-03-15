/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.di.app

import android.content.Context
import de.wladimirwendland.bibleaxis.BibleAxisApp
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent
import de.wladimirwendland.bibleaxis.di.component.FragmentComponent
import de.wladimirwendland.bibleaxis.di.module.ActivityModule
import de.wladimirwendland.bibleaxis.di.module.AppModule
import de.wladimirwendland.bibleaxis.di.module.DataModule
import de.wladimirwendland.bibleaxis.di.module.FragmentModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [
    AppModule::class,
    DataModule::class,
    ConfigModule::class,
    LibraryModule::class,
    MigrationModule::class
])
@Singleton
interface AppComponent {

    fun activityComponent(module: ActivityModule): ActivityComponent

    fun fragmentComponent(module: FragmentModule): FragmentComponent

    fun inject(application: BibleAxisApp)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}