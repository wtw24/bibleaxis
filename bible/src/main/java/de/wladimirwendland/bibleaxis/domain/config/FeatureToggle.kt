/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.config

import androidx.annotation.WorkerThread

/**
 * Рубильники для фичей
 */
interface FeatureToggle {

    /**
     * Инициализация рубильников
     */
    fun initToggles()

    /**
     * Доступность функционала нового UI для библиотеки
     */
    @WorkerThread
    fun newLibraryUiEnabled(): Boolean
}