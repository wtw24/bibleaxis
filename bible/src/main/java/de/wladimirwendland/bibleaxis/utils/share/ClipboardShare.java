/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils.share;

import android.content.Context;
import android.text.ClipboardManager;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;

import java.util.LinkedHashMap;

class ClipboardShare extends BaseShareBuilder {

	ClipboardShare(Context context, BaseModule module, Book book,
			Chapter chapter, LinkedHashMap<Integer, String> verses) {
		this.context = context;
		this.module = module;
		this.book = book;
		this.chapter = chapter;
		this.verses = verses;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void share() {
		initFormatters();
		if (textFormatter == null || referenceFormatter == null) {
			return;
		}

		ClipboardManager clpbdManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		if (clpbdManager != null) {
			clpbdManager.setText(getShareText());
		}
	}

}
