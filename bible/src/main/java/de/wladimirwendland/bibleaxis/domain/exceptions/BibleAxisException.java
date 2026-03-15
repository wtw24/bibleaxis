/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.exceptions;

public class BibleAxisException extends Exception {

	private static final long serialVersionUID = 5535751040905987997L;
	private String errMessage;

	public BibleAxisException(String message) {
		this.errMessage = message;
	}

	@Override
	public String getMessage() {
		return errMessage;
	}
}
