/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils.bibleReferenceFormatter;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.Book;

import java.util.TreeSet;

abstract class ReferenceFormatter implements IBibleReferenceFormatter {

	protected BaseModule module;
	protected Book book;
	protected String chapter;
	protected TreeSet<Integer> verses;

	ReferenceFormatter(BaseModule module, Book book, String chapter,
			TreeSet<Integer> verses) {
		super();
		this.module = module;
		this.book = book;
		this.chapter = chapter;
		this.verses = verses;
	}

	String getOnLineBibleLink() {
		return "http://b-bq.eu/"
				+ book.getOSIS_ID() + "/" + chapter + "_" + getVerseLink()
				+ "/" + module.getShortName();

	}

	String getVerseLink() {
		StringBuilder verseLink = new StringBuilder();
		Integer fromVerse = 0;
		Integer toVerse = 0;
		for (Integer verse : verses) {
			if (fromVerse == 0) {
				fromVerse = verse;
			} else if ((toVerse + 1) != verse) {
				if (verseLink.length() != 0) {
					verseLink.append(",");
				}
				if (fromVerse.equals(toVerse)) {
					verseLink.append(fromVerse);
				} else {
					verseLink.append(fromVerse).append("-").append(toVerse);
				}
				fromVerse = verse;
			}
			toVerse = verse;
		}
		if (verseLink.length() != 0) {
			verseLink.append(",");
		}
		if (fromVerse.equals(toVerse)) {
			verseLink.append(fromVerse);
		} else {
			verseLink.append(fromVerse).append("-").append(toVerse);
		}

		return verseLink.toString();
	}
}
