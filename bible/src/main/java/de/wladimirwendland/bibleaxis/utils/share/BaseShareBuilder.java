/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils.share;

import android.content.Context;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.textFormatters.BreakVerseBibleShareFormatter;
import de.wladimirwendland.bibleaxis.domain.textFormatters.IShareTextFormatter;
import de.wladimirwendland.bibleaxis.domain.textFormatters.SimpleBibleShareFormatter;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;
import de.wladimirwendland.bibleaxis.utils.bibleReferenceFormatter.EmptyReferenceFormatter;
import de.wladimirwendland.bibleaxis.utils.bibleReferenceFormatter.FullReferenceFormatter;
import de.wladimirwendland.bibleaxis.utils.bibleReferenceFormatter.IBibleReferenceFormatter;
import de.wladimirwendland.bibleaxis.utils.bibleReferenceFormatter.ShortReferenceFormatter;

import java.util.LinkedHashMap;
import java.util.TreeSet;

abstract class BaseShareBuilder {
	Book book;
	Chapter chapter;
	Context context;
    BaseModule module;
    IBibleReferenceFormatter referenceFormatter;
    IShareTextFormatter textFormatter;
    LinkedHashMap<Integer, String> verses;
	private PreferenceHelper preferenceHelper = BibleAxisApp.getInstance().getPrefHelper();

	public abstract void share();

	String getShareText() {
		String text = textFormatter.format();
		if (!preferenceHelper.addReference()) {
			return text;
		}

		String reference = referenceFormatter.getLink();
		if (preferenceHelper.putReferenceInBeginning()) {
			return String.format("%1$s%n%2$s", reference, text);
		} else {
			return String.format("%1$s (%2$s)", text, reference);
		}
	}

	void initFormatters() {
		if (preferenceHelper.divideTheVerses()) {
			textFormatter = new BreakVerseBibleShareFormatter(verses);
        } else {
            textFormatter = new SimpleBibleShareFormatter(verses);
        }

		TreeSet<Integer> verseNumbers = new TreeSet<>(verses.keySet());
		String chapterNumber = String.valueOf(chapter.getNumber());
        if (!preferenceHelper.addReference()) {
            referenceFormatter = new EmptyReferenceFormatter(module, book, chapterNumber, verseNumbers);
        } else if (preferenceHelper.shortReference()) {
            referenceFormatter = new ShortReferenceFormatter(module, book, chapterNumber, verseNumbers);
		} else {
			referenceFormatter = new FullReferenceFormatter(module, book, chapterNumber, verseNumbers);
		}
	}
}
