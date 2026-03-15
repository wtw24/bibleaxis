/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;

import de.wladimirwendland.bibleaxis.utils.IProgressTracker;
import de.wladimirwendland.bibleaxis.utils.Task;

import java.lang.ref.WeakReference;

public final class AsyncTaskManager implements IProgressTracker, OnCancelListener {

    private static final String TAG = AsyncTaskManager.class.getSimpleName();

    private Task mAsyncTask;
    private ProgressDialog mProgressDialog;
    private OnTaskCompleteListener taskCompleteListener;
    private WeakReference<Context> weakContext;

    public AsyncTaskManager(OnTaskCompleteListener taskCompleteListener) {
        this.taskCompleteListener = taskCompleteListener;
        Context context = taskCompleteListener.getContext();
        this.weakContext = new WeakReference<>(context);
        setupProgressDialog(context);
    }

    @Override
    public void onProgress(String message) {
        if (mAsyncTask.isHidden()) {
            return;
        }

        Context context = weakContext.get();
        if (context == null) {
            return;
        }

        // Show dialog if it wasn't shown yet or was removed on configuration
        // (rotation) change
        try {
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Show current message in progress dialog
        mProgressDialog.setMessage(message);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mAsyncTask == null) {
            return;
        }
        // Cancel task
        mAsyncTask.cancel(true);
        // Notify activity about completion
        taskCompleteListener.onTaskComplete(mAsyncTask);
        // Reset task
        mAsyncTask = null;
    }

    @Override
    public void onComplete() {
        // Close progress dialog
        try {
            mProgressDialog.cancel();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "View not attached to window manager");
        }

        // Reset task
        Task completedTask = mAsyncTask;
        mAsyncTask = null;

        // Notify activity about completion
        taskCompleteListener.onTaskComplete(completedTask);
    }

    public void setupTask(Task asyncTask) {
        // Keep task
        mAsyncTask = asyncTask;
        // Wire task to tracker (this)
        mAsyncTask.setProgressTracker(this);
        // Start task
        mAsyncTask.execute();
    }

    void handleRetainedTask(Task task, OnTaskCompleteListener taskCompleteListener) {
        this.taskCompleteListener = taskCompleteListener;
        setupProgressDialog(taskCompleteListener.getContext());

        // Restore retained task and attach it to tracker (this)
        mAsyncTask = task;
        mAsyncTask.setProgressTracker(this);
    }

    boolean isWorking() {
        // Track current status
        return mAsyncTask != null;
    }

    Object retainTask() {
        // Close progress dialog
        mProgressDialog.cancel();

        // Detach task from tracker (this) before retain
        if (mAsyncTask != null) {
            mAsyncTask.setProgressTracker(null);
        }
        // Retain task
        return mAsyncTask;
    }

    private void setupProgressDialog(Context context) {
        // Setup progress dialog
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setOnCancelListener(this);
    }
}