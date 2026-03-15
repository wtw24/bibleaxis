/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.imagepreview

import android.graphics.Bitmap

sealed class ImagePreviewViewResult {

    data class DrawImage(val image: Bitmap) : ImagePreviewViewResult()
    object UnsuccessfulSearch : ImagePreviewViewResult()

}