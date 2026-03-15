/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.exceptions;

public class TskNotFoundException extends Exception {

	private static final long serialVersionUID = 5535751040905987997L;

	@Override
	public String getMessage() {
		return "TSK cross-reference library not found";
	}
}
