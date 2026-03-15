/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.entity.modules;

import de.wladimirwendland.bibleaxis.domain.entity.Book;

/**
 * @author Yakushev Vladimir, Sergey Ursul
 */
public class BibleAxisBook extends Book {

	private static final long serialVersionUID = -6570010365754882585L;

	/**
	 * Путь к файлу с книгой
	 */
	private final String fileName;

	public BibleAxisBook(BibleAxisModule module, String name, String fileName, String shortNames, int chapterQty) {
		super(name, shortNames, chapterQty, module.isChapterZero());
		this.fileName = fileName;
	}

	@Override
	public String getDataSourceID() {
		return this.fileName;
	}

}
