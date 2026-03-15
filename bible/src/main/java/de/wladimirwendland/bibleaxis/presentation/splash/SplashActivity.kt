/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import de.wladimirwendland.bibleaxis.R
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseAppActivity
import de.wladimirwendland.bibleaxis.presentation.ui.reader.ReaderActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger.info
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseAppActivity() {

    private val updateDescriptionView: TextView by lazy {
        findViewById(R.id.update_description)
    }
    private val rootLayout: ViewGroup by lazy {
        findViewById(R.id.root_layout)
    }
    private val viewModel: SplashViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var viewModelFactory: SplashViewModel.Factory

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel.result.observe(this) { result ->
            when (result) {
                is SplashViewResult.UpdateResult -> showUpdateMessage(result.message)
                is SplashViewResult.InitFailure -> showErrorMessage()
                is SplashViewResult.InitSuccess -> gotoReaderActivity()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onViewStarted()
    }

    override fun inject(component: ActivityComponent) {
        component.inject(this)
    }

    private fun showUpdateMessage(message: Int) {
        info(this, getString(message))
        updateDescriptionView.setText(message)
    }

    private fun showErrorMessage() {
        Snackbar.make(rootLayout, R.string.error_initialization_failed, LENGTH_INDEFINITE)
            .setAction(R.string.retry) { viewModel.onViewStarted() }
            .show()
    }

    private fun gotoReaderActivity() {
        startActivity(Intent(this, ReaderActivity::class.java))
        finish()
    }
}