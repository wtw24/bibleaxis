/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.controller;

import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;
import de.wladimirwendland.bibleaxis.domain.controller.LibraryRepository;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BooksDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.domain.repository.LibraryLoader;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class FsLibraryController implements ILibraryController {

    private final LibraryRepository libraryRepository;
    private final LibraryLoader libraryLoader;

    public FsLibraryController(LibraryLoader libraryLoader, LibraryRepository libraryRepository) {
        this.libraryLoader = libraryLoader;
        this.libraryRepository = libraryRepository;
    }

    @Override
    public void init() {
        StaticLogger.info(this, "Init");
        Map<String, BaseModule> modules = getModules();
        if (modules.isEmpty()) {
            reloadModules();
        }
    }

    @Override
    public Map<String, BaseModule> reloadModules() {
        Map<String, BaseModule> modules = libraryLoader.loadFileModules();
        libraryRepository.replace(modules.values());
        return modules;
    }

    @Override
    public Map<String, BaseModule> getModules() {
        Map<String, BaseModule> result = new TreeMap<>();
        final List<BaseModule> modules = libraryRepository.modules();
        for (BaseModule module : modules) {
            result.put(module.getID(), module);
        }
        return result;
    }

    @Override
    public BaseModule getModuleByID(String moduleID) throws OpenModuleException {
        if (moduleID == null) {
            return null;
        }

        BaseModule module = getModules().get(moduleID);
        if (module == null) {
            throw new OpenModuleException(moduleID, null);
        }
        return module;
    }

    @Override
    public void loadModule(File file) throws OpenModuleException, BooksDefinitionException, BookDefinitionException {
        StaticLogger.info(this, "Load module from " + file);
        libraryRepository.add(libraryLoader.loadModule(file));
    }
}
