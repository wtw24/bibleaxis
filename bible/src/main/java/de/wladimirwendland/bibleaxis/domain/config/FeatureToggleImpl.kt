/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.config

/**
 * Реализация для получения значения рубильников фичей
 *
 * @param repository репозиторий для чтений значений рубильников
 */
class FeatureToggleImpl(
        private val repository: FeatureToggleRepository
) : FeatureToggle {

    override fun initToggles() {
        repository.initToggles()
    }

    override fun newLibraryUiEnabled(): Boolean = repository.isEnabled("new_library_ui")
}