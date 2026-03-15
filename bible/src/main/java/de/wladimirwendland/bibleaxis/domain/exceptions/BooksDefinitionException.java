/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.exceptions;

public class BooksDefinitionException extends Exception {

	private static final long serialVersionUID = -1652902166548627455L;
	private String moduleDatasourceID;
	private int pathNameCount;
	private int fullNameCount;
	private int shortNameCount;
	private int chapterQtyCount;
	private int booksCount;

	public BooksDefinitionException(String message,
									String moduleDatasourceID, int booksCount, int pathNameCount, int fullNameCount, int shortNameCount, int chapterQtyCount) {
		super(message);
		this.moduleDatasourceID = moduleDatasourceID;
		this.booksCount = booksCount;
		this.pathNameCount = pathNameCount;
		this.fullNameCount = fullNameCount;
		this.shortNameCount = shortNameCount;
		this.chapterQtyCount = chapterQtyCount;
	}

	public BooksDefinitionException(Exception parent) {
		super(parent);
	}

	public String getModuleDatasourceID() {
		return moduleDatasourceID;
	}

	public int getBooksCount() {
		return booksCount;
	}

	public int getPathNameCount() {
		return pathNameCount;
	}

	public int getFullNameCount() {
		return fullNameCount;
	}

	public int getShortNameCount() {
		return shortNameCount;
	}

	public int getChapterQtyCount() {
		return chapterQtyCount;
	}
}
