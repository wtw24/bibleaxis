/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.entity.modules;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;

import java.io.File;

/**
 * @author Yakushev Vladimir, Sergey Ursul
 */
public class BibleAxisModule extends BaseModule {

	private static final long serialVersionUID = -660821372799486761L;
	/**
	 * modulePath is a directory path or an archive path with a name
	 */
	public final String modulePath;
	/**
	 * Имя ini-файла (раскладка в названии файла может быть произвольной)
	 */
	public final String iniFileName;
	private final Boolean isArchive;

	public BibleAxisModule(String modulePath, String iniFilename) {
		this.modulePath = modulePath;
		this.iniFileName = iniFilename;
		this.isArchive = this.modulePath.toLowerCase().endsWith(".zip");
	}

	public String getModulePath() {
		return modulePath;
	}

	@Override
	public String getDataSourceID() {
		return this.modulePath + File.separator + this.iniFileName;
	}

	@Override
	public String getID() {
		return getShortName().toUpperCase();
	}

	public Boolean isArchive() {
		return isArchive;
	}

}
