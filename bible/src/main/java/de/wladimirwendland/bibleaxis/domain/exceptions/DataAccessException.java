/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.exceptions;

public class DataAccessException extends Exception {

	private static final long serialVersionUID = -897391086822306905L;

	public DataAccessException(String message) {
		super(message);
	}

	public DataAccessException(Exception parent) {
		super(parent);
	}

}
