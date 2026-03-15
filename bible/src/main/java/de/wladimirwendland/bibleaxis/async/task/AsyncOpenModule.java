/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async.task;

import android.util.Log;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.utils.Task;

public class AsyncOpenModule extends Task {
	private static final String TAG = "AsyncOpenBooks";

	private BibleReference link;
	private Exception exception;
    private BaseModule module;
    private ILibraryController libCtrl;

	public AsyncOpenModule(String message, Boolean isHidden, BibleReference link) {
		super(message, isHidden);
		this.libCtrl = BibleAxisApp.getInstance().getLibraryController();
		this.link = link;
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
		try {
			Log.i(TAG, String.format("Open OSIS link with moduleID=%1$s", link.getModuleID()));
			module = libCtrl.getModuleByID(link.getModuleID());
            return true;
        } catch (OpenModuleException e) {
			Log.e(TAG, String.format("AsyncOpenBooks(): %s", e.toString()), e);
			exception = e;
		}
        return false;
    }

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
	}

	public Exception getException() {
		return exception;
	}

    public BaseModule getModule() {
        return module;
    }
}
