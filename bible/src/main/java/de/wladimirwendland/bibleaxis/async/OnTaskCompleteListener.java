/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async;

import android.content.Context;

import de.wladimirwendland.bibleaxis.utils.Task;

public interface OnTaskCompleteListener {
	// Notifies about task completeness
	void onTaskComplete(Task task);

    Context getContext();
}