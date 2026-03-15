/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

import java.io.File;
import java.io.FileFilter;

public class OnlyBQZipIni implements FileFilter {
	private String filter;

	public OnlyBQZipIni() {
		this.filter = ".zip";
	}

	public OnlyBQZipIni(String filter) {
		this.filter = filter;
	}

	public boolean accept(File myFile) {
		return myFile.getName().toLowerCase().endsWith(this.filter)
				|| myFile.isDirectory();
	}

	@Override
	public String toString() {
		return this.filter;
	}
}