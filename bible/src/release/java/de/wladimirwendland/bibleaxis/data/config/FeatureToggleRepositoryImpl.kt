/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.config

import android.content.Context
import androidx.annotation.RawRes
import de.wladimirwendland.bibleaxis.domain.config.FeatureToggleRepository

class FeatureToggleRepositoryImpl(
        private val context: Context,
        @RawRes private val configResId: Int
) : FeatureToggleRepository() {

    override fun getTogglesStream() = context.resources.openRawResource(configResId)
}
