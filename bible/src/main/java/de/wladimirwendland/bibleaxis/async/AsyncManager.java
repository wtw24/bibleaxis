/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async;

import android.content.Context;

import de.wladimirwendland.bibleaxis.utils.Task;

public class AsyncManager implements OnTaskCompleteListener {

    private Context context;
    private AsyncTaskManager mAsyncTaskManager;
    private AsyncTaskManager mWaitTaskManager;
    private OnTaskCompleteListener taskCompleteListener;
    private volatile Task waitTask;    // the task is waiting its execution

    public synchronized boolean isWorking() {
        return mAsyncTaskManager != null && mAsyncTaskManager.isWorking();
    }

    @Override
    public synchronized void onTaskComplete(Task task) {
        if (mWaitTaskManager != null) {
            mWaitTaskManager.retainTask();
            mWaitTaskManager = null;
        }
        try {
            Task newTask = waitTask;
            waitTask = null;
            setupTask(newTask, taskCompleteListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getContext() {
        return context;
    }

    public synchronized void handleRetainedTask(Task task, OnTaskCompleteListener taskCompleteListener) {
        this.taskCompleteListener = taskCompleteListener;
        mAsyncTaskManager = new AsyncTaskManager(taskCompleteListener);
        mAsyncTaskManager.handleRetainedTask(task, taskCompleteListener);
    }

    public synchronized Task retainTask() {
        if (mWaitTaskManager != null) {
            mWaitTaskManager.retainTask();
            mWaitTaskManager = null;
        }
        waitTask = null;
        if (mAsyncTaskManager != null) {
            Task retainTask = (Task) mAsyncTaskManager.retainTask();
            mAsyncTaskManager = null;
            return retainTask;
        }
        return null;
    }

    public synchronized void setupTask(Task task, OnTaskCompleteListener taskCompleteListener) {
        this.taskCompleteListener = taskCompleteListener;
        this.context = taskCompleteListener.getContext();
        AsyncTaskManager newAsyncTaskManager = new AsyncTaskManager(taskCompleteListener);
        if (mAsyncTaskManager != null && mAsyncTaskManager.isWorking()) {
            // Override the next task only if a new task is a foreground task (with a progress dialog visible)
            if (waitTask == null || !task.isHidden()) {
                waitTask = task;
            }

            if (mWaitTaskManager == null) {
                // Start a wait thread until mAsyncTaskManager has completed
                mWaitTaskManager = new AsyncTaskManager(this);
                mWaitTaskManager.setupTask(new AsyncWait("Please wait ...", false, mAsyncTaskManager));
            }
        } else {
            mAsyncTaskManager = newAsyncTaskManager;
            Task nextTask;
            if (waitTask != null) {
                nextTask = waitTask;
                waitTask = task;
            } else {
                nextTask = task;
            }
            mAsyncTaskManager.setupTask(nextTask);
        }
    }
}
