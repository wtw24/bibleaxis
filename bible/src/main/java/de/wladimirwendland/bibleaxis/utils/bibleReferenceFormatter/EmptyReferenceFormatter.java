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

public class EmptyReferenceFormatter extends ReferenceFormatter implements IBibleReferenceFormatter {

    public EmptyReferenceFormatter(BaseModule module, Book book, String chapter,
            TreeSet<Integer> verses) {
        super(module, book, chapter, verses);
    }

	@Override
	public String getLink() {
		return "";
	}

}
