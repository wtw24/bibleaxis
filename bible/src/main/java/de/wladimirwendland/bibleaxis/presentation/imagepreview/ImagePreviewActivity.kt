/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.imagepreview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import de.wladimirwendland.bibleaxis.R
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseAppActivity
import de.wladimirwendland.bibleaxis.presentation.widget.TouchImageView
import javax.inject.Inject

class ImagePreviewActivity : BaseAppActivity() {

    private val imageView: TouchImageView by lazy {
        findViewById(R.id.image)
    }

    private val viewModel: ImagePreviewViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var viewModelFactory: ImagePreviewViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        intent.getStringExtra(IMAGE_PATH)?.let { path ->
            viewModel.imageState.observe(this) { result ->
                when (result) {
                    is ImagePreviewViewResult.DrawImage -> updatePreviewDrawable(result.image)
                    is ImagePreviewViewResult.UnsuccessfulSearch -> imageNotFound()
                }
            }
            viewModel.onActivityCreate(path)
        }
            ?: let {
                Log.e(TAG, "No params")
                imageNotFound()
            }
    }

    override fun inject(component: ActivityComponent) {
        component.inject(this)
    }

    private fun imageNotFound() {
        Toast.makeText(this, R.string.image_not_found, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun updatePreviewDrawable(value: Bitmap) {
        imageView.maxZoom = DEFAULT_ZOOM.toFloat()
        imageView.setImageDrawable(BitmapDrawable(resources, value))
    }

    companion object {
        private val TAG: String = ImagePreviewActivity::class.java.simpleName
        private const val IMAGE_PATH: String = "image_path"
        private const val DEFAULT_ZOOM: Int = 10

        @JvmStatic
        fun getIntent(context: Context, imagePath: String): Intent {
            val intent = Intent(context, ImagePreviewActivity::class.java)
            intent.putExtra(IMAGE_PATH, imagePath)
            return intent
        }
    }
}