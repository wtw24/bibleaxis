/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.reader;

public interface IReaderViewListener {
	void onReaderClickImage(String path);

	void onReaderClickHighlight(String highlightId);

	void onReaderClickStrong(String strongCode);

	void onReaderTextSelection(String payload);

	void onReaderViewChange(ChangeCode code);

	enum ChangeCode {
		onChangeSelection,
		onLongPress,
		onChangeReaderMode,
		onUpNavigation,
		onDownNavigation,
		onLeftNavigation,
		onRightNavigation
	}
}
