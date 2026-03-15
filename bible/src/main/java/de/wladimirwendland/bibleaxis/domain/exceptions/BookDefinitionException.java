/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.exceptions;

public class BookDefinitionException extends Exception {

	private static final long serialVersionUID = -1652902166548627455L;
	private String moduleDatasourceID;
	private int bookNumber;

	public BookDefinitionException(String message,
								   String moduleDatasourceID, int bookNumber) {
		super(message);
		this.moduleDatasourceID = moduleDatasourceID;
		this.bookNumber = bookNumber;
	}

	public BookDefinitionException(Exception parent) {
		super(parent);
	}

	public String getModuleDatasourceID() {
		return moduleDatasourceID;
	}

	public int getBookNumber() {
		return bookNumber;
	}

}
