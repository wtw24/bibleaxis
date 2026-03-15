/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.controller;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BooksDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;

import java.io.File;
import java.util.Map;

/**
 *
 */
public interface ILibraryController {

    /**
     * @return Возвращает коллекцию модулей с ключом по Module.ShortName
     */
    Map<String, BaseModule> getModules();

    /**
     * Получение модуля из коллекции по его ShortName.
     *
     * @param moduleID ShortName модуля
     * @return найденный модуль
     * @throws de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException - указанный ShortName отсутствует в коллекции
     */
    BaseModule getModuleByID(String moduleID) throws OpenModuleException;

    /**
     * Инициализация библиотеки
     */
    void init();

    /**
     * Загружает из хранилища модуль по его пути\
     *
     * @param file файл с модулем для загрузки
     * @throws OpenModuleException по указанному пути модуль не найден
     */
    void loadModule(File file) throws OpenModuleException, BooksDefinitionException, BookDefinitionException;

    /**
     * Загружает из хранилища список модулей без загрузки их данных. Для каждого из модулей
     * установлен флаг isClosed = true.
     *
     * @return Возвращает TreeMap, где в качестве ключа путь к модулю, а в качестве значения
     * closed-модуль
     */
    Map<String, BaseModule> reloadModules();
}
