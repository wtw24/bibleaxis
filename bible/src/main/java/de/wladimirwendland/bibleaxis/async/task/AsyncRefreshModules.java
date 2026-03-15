/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async.task;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;
import de.wladimirwendland.bibleaxis.utils.Task;

public class AsyncRefreshModules extends Task {

	private ILibraryController libCtrl;

	public AsyncRefreshModules(String message, Boolean isHidden) {
		super(message, isHidden);
		this.libCtrl = BibleAxisApp.getInstance().getLibraryController();
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
        libCtrl.reloadModules();
        return true;
    }
}
