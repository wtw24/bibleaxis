/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.exceptions;

public class BookNotFoundException extends Exception {

	private static final long serialVersionUID = -941193264792260938L;

	private String moduleID;
	private String bookID;

	public BookNotFoundException(String moduleID, String bookID) {
		this.moduleID = moduleID;
		this.bookID = bookID;
	}

	public String getBookID() {
		return bookID;
	}

	public String getModuleID() {
		return moduleID;
	}

	@Override
	public String getMessage() {
		return String.format("Book %1$s not found in module %2$s", bookID, moduleID);
	}
}
