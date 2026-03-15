/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.repository;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BooksDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;

import java.io.File;
import java.util.Map;

public interface LibraryLoader {

	/**
	 * Загрузка списка модулей из хранилища без чтения данных.
	 * Модулям устанавливается флаг isClosed=true
	 * <br><font color='red'>Производится полная перезапись в кэш коллекции модулей.</font><br>
	 */
	@NonNull
    Map<String, BaseModule> loadFileModules();

    BaseModule loadModule(File file) throws OpenModuleException, BooksDefinitionException, BookDefinitionException;
}
