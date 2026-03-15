/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async;

import android.util.Log;
import de.wladimirwendland.bibleaxis.utils.Task;

public class AsyncWait extends Task {
	private static final String TAG = "AsyncWait";
	private AsyncTaskManager currentAsyncTaskManager;


	public AsyncWait(String message, Boolean isHidden, AsyncTaskManager currentAsyncTaskManager) {
		super(message, isHidden);
		this.currentAsyncTaskManager = currentAsyncTaskManager;
	}


	@Override
	protected Boolean doInBackground(String... arg0) {
		try {
			if (currentAsyncTaskManager != null) {
				while (true) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						break;
					}
					if (!currentAsyncTaskManager.isWorking()) {
						break;
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return true;
	}


	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
	}


}
