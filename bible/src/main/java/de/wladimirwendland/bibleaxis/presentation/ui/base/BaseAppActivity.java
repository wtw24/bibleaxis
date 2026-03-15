/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent;
import de.wladimirwendland.bibleaxis.di.module.ActivityModule;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import de.wladimirwendland.bibleaxis.di.app.AppComponent;
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public abstract class BaseAppActivity extends AppCompatActivity {

    private PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject(getActivityComponent());
        preferenceHelper = BibleAxisApp.instance(this).getPrefHelper();
        applyImmersiveNavigationMode();
        StaticLogger.info(this, "Create activity");
    }

    @Override
    protected void onStart() {
        super.onStart();
        StaticLogger.info(this, "Start activity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyImmersiveNavigationMode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveNavigationMode();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        StaticLogger.info(this, "Stop activity");
    }

    public Scheduler backgroundThread() {
        return Schedulers.newThread();
    }

    public Scheduler mainThread() {
        return AndroidSchedulers.mainThread();
    }

    public void refreshSystemBarsVisibility() {
        applyImmersiveNavigationMode();
    }

    private void applyImmersiveNavigationMode() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller == null) {
            return;
        }
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        boolean autoHideNavigationBar = preferenceHelper == null || preferenceHelper.isAutoHideNavigationBarEnabled();
        if (autoHideNavigationBar) {
            controller.hide(WindowInsetsCompat.Type.navigationBars());
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars());
        }
    }

    protected abstract void inject(ActivityComponent component);

    protected ActivityComponent getActivityComponent() {
        AppComponent appComponent = BibleAxisApp.instance(this).getAppComponent();
        return appComponent.activityComponent(new ActivityModule());
    }
}
