/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils.bibleReferenceFormatter;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

import java.util.TreeSet;

public class ShortReferenceFormatter extends ReferenceFormatter implements IBibleReferenceFormatter {

    public ShortReferenceFormatter(BaseModule module, Book book, String chapter,
            TreeSet<Integer> verses) {
        super(module, book, chapter, verses);
    }

	@Override
	public String getLink() {
		PreferenceHelper prefHelper = BibleAxisApp.getInstance().getPrefHelper();
		String result = String.format(
				"%1$s.%2$s:%3$s",
				book.getShortName(), chapter, getVerseLink());
		if (prefHelper.addModuleToBibleReference()) {
			result = String.format("%1$s | %2$s", result, module.getID());
		}
		return result;
	}

}
