/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

import java.io.File;
import java.io.FileFilter;


public class OnlyBQIni implements FileFilter {
	private String filter;

	public OnlyBQIni() {
		this.filter = "bibleqt.ini";
	}

	public OnlyBQIni(String filter) {
		this.filter = filter;
	}

	public boolean accept(File myFile) {
		return myFile.getName().toLowerCase().equals(this.filter)
				|| myFile.isDirectory();
	}

	@Override
	public String toString() {
		return this.filter;
	}
}