/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.widget.Toast;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent;
import de.wladimirwendland.bibleaxis.domain.AnalyticsHelper;

import javax.inject.Inject;

import butterknife.ButterKnife;

public abstract class BaseActivity<T extends BasePresenter> extends BaseAppActivity implements BaseView {

    private static final int DELAY_SHOW_PROGRESS = 500;

    @Inject protected T presenter;
    @Inject protected AnalyticsHelper analyticsHelper;

    private ProgressDialog progressDialog;
    private Handler progressHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getRootLayout());
        ButterKnife.bind(this);
        attachView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onViewCreated();
    }

    @Override
    public void showProgress(boolean cancelable) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setCancelable(cancelable);
        progressDialog.setMessage(getString(R.string.messageLoad));
        if (!progressDialog.isShowing()) {
            progressHandler.postDelayed(() -> progressDialog.show(), DELAY_SHOW_PROGRESS);
        }
    }

    @Override
    public void hideProgress() {
        progressHandler.removeCallbacksAndMessages(null);
        if (!isFinishing() && progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }
    }

    @Override
    public void showToast(int resource) {
        Toast.makeText(this, resource, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    protected abstract void attachView();

    protected abstract int getRootLayout();

    protected abstract void inject(ActivityComponent component);
}
