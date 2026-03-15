/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.splash

sealed class SplashViewResult {

    data class UpdateResult(val message: Int) : SplashViewResult()
    object InitFailure : SplashViewResult()
    object InitSuccess : SplashViewResult()
}
