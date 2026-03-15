/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.base;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.async.AsyncManager;
import de.wladimirwendland.bibleaxis.async.OnTaskCompleteListener;
import de.wladimirwendland.bibleaxis.utils.Task;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public abstract class AsyncTaskActivity extends BaseAppActivity implements OnTaskCompleteListener {

    protected AsyncManager mAsyncManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAsyncManager();
    }

    @Override
    public abstract void onTaskComplete(Task task);

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mAsyncManager.retainTask();
    }

    private void initAsyncManager() {
        mAsyncManager = BibleAxisApp.getInstance().getAsyncManager();
        Object retainedTask = getLastCustomNonConfigurationInstance();
        if (retainedTask != null && retainedTask instanceof Task) {
            mAsyncManager.handleRetainedTask((Task) retainedTask, this);
        }
    }
}
