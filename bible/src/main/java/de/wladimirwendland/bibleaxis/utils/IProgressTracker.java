/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

public interface IProgressTracker {
	// Updates progress message
	void onProgress(String message);

	// Notifies about task completeness
	void onComplete();
}