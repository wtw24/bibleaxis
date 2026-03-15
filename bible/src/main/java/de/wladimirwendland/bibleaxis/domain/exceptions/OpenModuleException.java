/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.exceptions;

import android.content.res.Resources;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;

public class OpenModuleException extends Exception {

	private static final long serialVersionUID = -941193264792260938L;
	private String moduleDatasourceId;
	private String moduleId;

	public OpenModuleException(String moduleId, String moduleDatasourceId) {
		this.moduleId = moduleId;
		this.moduleDatasourceId = moduleDatasourceId;
    }

	public OpenModuleException(BaseModule module) {
		this.moduleId = module.getID();
		this.moduleDatasourceId = module.getDataSourceID();
    }

	@Override
	public String getMessage() {
		Resources resources = BibleAxisApp.getInstance().getApplicationContext().getResources();
		return String.format(resources.getString(R.string.error_open_module), moduleId, moduleDatasourceId);
	}

	String getModuleDatasourceId() {
		return moduleDatasourceId;
	}

	String getModuleId() {
		return moduleId;
	}
}
