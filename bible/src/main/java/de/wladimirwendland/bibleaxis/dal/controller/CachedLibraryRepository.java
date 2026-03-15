/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.controller;

import de.wladimirwendland.bibleaxis.domain.controller.LibraryRepository;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.ModuleList;
import de.wladimirwendland.bibleaxis.domain.exceptions.DataAccessException;
import de.wladimirwendland.bibleaxis.domain.repository.ICacheRepository;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class CachedLibraryRepository implements LibraryRepository {

    private ICacheRepository cacheRepository;
    private CopyOnWriteArrayList<BaseModule> modules = new CopyOnWriteArrayList<>();

    public CachedLibraryRepository(ICacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    @Override
    public List<BaseModule> modules() {
        StaticLogger.info(this, "Get module list");
        if (modules.isEmpty() && cacheRepository.isCacheExist()) {
            try {
                modules.addAll(cacheRepository.getData());
            } catch (DataAccessException e) {
                StaticLogger.error(this, "Get module list failure", e);
            }
        }

        return modules;
    }

    @Override
    public void replace(Collection<BaseModule> list) {
        StaticLogger.info(this, "Replacing modules in the cache");
        modules.clear();
        modules.addAll(list);
        cacheModulesList();
    }

    @Override
    public void add(BaseModule module) {
        StaticLogger.info(this, "Adding a module to the cache");
        modules.add(module);
        cacheModulesList();
    }

    private void cacheModulesList() {
        try {
            cacheRepository.saveData(new ModuleList(modules));
        } catch (DataAccessException e) {
            StaticLogger.error(this, "Can't save modules to a cache.", e);
        }
    }
}
