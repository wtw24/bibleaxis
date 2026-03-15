/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

import android.os.AsyncTask;

public abstract class Task extends AsyncTask<String, String, Boolean> {

    /**
     * @uml.property name="mResult"
	 */
	private Boolean mResult;
	/**
	 * @uml.property name="mProgressMessage"
	 */
	private String mProgressMessage;
	/**
	 * @uml.property name="mProgressTracker"
	 * @uml.associationEnd
	 */
	private IProgressTracker mProgressTracker;
	private Boolean mIsHidden;

	public Task(String message, Boolean isHidden) {
		mProgressMessage = message;
		mIsHidden = isHidden;
	}

    public void setProgressTracker(IProgressTracker progressTracker) {
        // Attach to progress tracker
        mProgressTracker = progressTracker;
        // Initialize progress tracker with current task state
        if (mProgressTracker != null) {
            mProgressTracker.onProgress(mProgressMessage);
            if (mResult != null) {
                mProgressTracker.onComplete();
            }
        }
    }

	@Override
	protected abstract Boolean doInBackground(String... arg0);

	@Override
	protected void onPostExecute(Boolean result) {
		// Update result
		mResult = result;
		// And send it to progress tracker
		if (mProgressTracker != null) {
			mProgressTracker.onComplete();
		}
		// Detach from progress tracker
		mProgressTracker = null;
	}

	@Override
	protected void onCancelled() {
		// Detach from progress tracker
		mProgressTracker = null;
        mResult = false;
    }

	@Override
	protected void onProgressUpdate(String... values) {
		// Update progress message
		mProgressMessage = values[0];
		// And send it to progress tracker
		if (mProgressTracker != null) {
			mProgressTracker.onProgress(mProgressMessage);
		}
	}

    public void setHidden(Boolean value) {
        mIsHidden = value;
        if (!mIsHidden) {
            onProgressUpdate(mProgressMessage);
        }
    }

	public Boolean isHidden() {
		return mIsHidden;
	}

    public Boolean isSuccess() {
        return mResult != null && mResult;
    }
}