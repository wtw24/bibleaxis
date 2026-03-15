/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.imagepreview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.wladimirwendland.bibleaxis.di.scope.PerActivity
import de.wladimirwendland.bibleaxis.managers.Librarian
import javax.inject.Inject

class ImagePreviewViewModel(
    private val myLibrarian: Librarian,
) : ViewModel() {

    private val _imageState = MutableLiveData<ImagePreviewViewResult>()
    val imageState: LiveData<ImagePreviewViewResult>
        get() = _imageState

    fun onActivityCreate(imagePath: String) {
        val imageBitmap = myLibrarian.getModuleImage(imagePath)
        _imageState.value = ImagePreviewViewResult.DrawImage(imageBitmap)
    }

    @PerActivity
    class Factory @Inject constructor(
        private val myLibrarian: Librarian,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ImagePreviewViewModel(myLibrarian) as T
    }
}