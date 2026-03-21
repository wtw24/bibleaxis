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
import androidx.lifecycle.viewModelScope
import de.wladimirwendland.bibleaxis.di.scope.PerActivity
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController
import de.wladimirwendland.bibleaxis.domain.config.FeatureToggle
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger
import de.wladimirwendland.bibleaxis.domain.migration.UpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SplashViewModel(
    private val libraryController: ILibraryController,
    private val updateManager: UpdateManager,
    private val featureToggle: FeatureToggle
): ViewModel() {

    private var initJob: Job? = null

    private val resultData = MutableLiveData<SplashViewResult>()
    val result: LiveData<SplashViewResult>
        get() = resultData

    fun onViewStarted() {
        initJob?.cancel()
        initJob = viewModelScope.launch {
            if (!runUpdateStep()) {
                return@launch
            }
            runInitStep()
        }
    }

    private suspend fun runUpdateStep(): Boolean {
        return try {
            updateManager.runPendingUpdates { message ->
                resultData.postValue(SplashViewResult.UpdateResult(message))
            }
            true
        } catch (throwable: Throwable) {
            StaticLogger.error(this, "Update failure", throwable)
            resultData.value = SplashViewResult.InitFailure
            false
        }
    }

    private suspend fun runInitStep() {
        try {
            withContext(Dispatchers.Default) {
                libraryController.init()
                featureToggle.initToggles()
            }
            resultData.value = SplashViewResult.InitSuccess
        } catch (throwable: Throwable) {
            StaticLogger.error(this, "Init library failure", throwable)
            resultData.value = SplashViewResult.InitFailure
        }
    }

    override fun onCleared() {
        initJob?.cancel()
        super.onCleared()
    }

    @PerActivity
    class Factory @Inject constructor(
        private val libraryController: ILibraryController,
        private val updateManager: UpdateManager,
        private val featureToggle: FeatureToggle
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SplashViewModel(libraryController, updateManager, featureToggle) as T
    }
}
