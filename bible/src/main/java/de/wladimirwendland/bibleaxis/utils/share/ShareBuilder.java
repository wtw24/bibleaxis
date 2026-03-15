/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils.share;

import android.content.Context;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;

import java.util.LinkedHashMap;

public class ShareBuilder {

	public enum Destination {
		Clipboard, ActionSend
	}

	private Context context;
    private BaseModule module;
    private Book book;
    private Chapter chapter;
    private LinkedHashMap<Integer, String> verses;

    public ShareBuilder(Context context, BaseModule module, Book book, Chapter chapter, LinkedHashMap<Integer, String> verses) {
        this.context = context;
        this.module = module;
        this.book = book;
		this.chapter = chapter;
		this.verses = verses;
	}

	public void share(Destination dest) {
		BaseShareBuilder builder = getBuilder(dest);
		if (builder == null) {
			return;
		}
		builder.share();
	}

	private BaseShareBuilder getBuilder(Destination dest) {
		if (dest == Destination.ActionSend) {
			return new ActionSendShare(context, module, book, chapter, verses);
		} else if (dest == Destination.Clipboard) {
			return new ClipboardShare(context, module, book, chapter, verses);
		} else {
			return null;
		}
	}

}
