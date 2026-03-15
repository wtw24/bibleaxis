/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils.share;

import android.content.Context;
import android.content.Intent;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;

import java.util.LinkedHashMap;

class ActionSendShare extends BaseShareBuilder {

	ActionSendShare(Context context, BaseModule module, Book book,
			Chapter chapter, LinkedHashMap<Integer, String> verses) {
		this.context = context;
		this.module = module;
		this.book = book;
		this.chapter = chapter;
		this.verses = verses;
	}

	@Override
	public void share() {
		initFormatters();
        if (textFormatter == null || referenceFormatter == null) {
            return;
		}

		final String share = context.getResources().getString(R.string.share);
		Intent send = new Intent(Intent.ACTION_SEND);
		send.setType("text/plain");
		send.putExtra(Intent.EXTRA_TEXT, getShareText());
		context.startActivity(Intent.createChooser(send, share));
	}

}
