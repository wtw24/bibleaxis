/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.wladimirwendland.bibleaxis.di.scope.PerActivity
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import de.wladimirwendland.bibleaxis.domain.RxSchedulers
import de.wladimirwendland.bibleaxis.domain.config.FeatureToggle
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger
import de.wladimirwendland.bibleaxis.domain.migration.UpdateManager
import javax.inject.Inject

class SplashViewModel(
    private val libraryController: ILibraryController,
    private val updateManager: UpdateManager,
    private val featureToggle: FeatureToggle,
    private val rxSchedulers: RxSchedulers
): ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val resultData = MutableLiveData<SplashViewResult>()
    val result: LiveData<SplashViewResult>
        get() = resultData

    fun onViewStarted() {
        updateManager.update()
            .subscribeOn(rxSchedulers.computation)
            .observeOn(rxSchedulers.mainThread)
            .subscribe(
                { message: Int ->
                    resultData.value = SplashViewResult.UpdateResult(message)
                },
                { throwable: Throwable ->
                    StaticLogger.error(this, "Update failure", throwable)
                    resultData.value = SplashViewResult.InitFailure
                },
                { initLibrary() }
            ).let {
                compositeDisposable.add(it)
            }
    }

    private fun initLibrary() {
        Completable.fromRunnable { libraryController.init() }
            .concatWith(Completable.fromRunnable { featureToggle.initToggles() })
            .subscribeOn(rxSchedulers.computation)
            .observeOn(rxSchedulers.mainThread)
            .subscribe(
                { resultData.value = SplashViewResult.InitSuccess },
                { throwable: Throwable ->
                    StaticLogger.error(this, "Init library failure", throwable)
                    resultData.value = SplashViewResult.InitFailure
            }).let {
                compositeDisposable.add(it)
            }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    @PerActivity
    class Factory @Inject constructor(
        private val libraryController: ILibraryController,
        private val updateManager: UpdateManager,
        private val featureToggle: FeatureToggle,
        private val rxSchedulers: RxSchedulers
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SplashViewModel(libraryController, updateManager, featureToggle, rxSchedulers) as T
    }
}