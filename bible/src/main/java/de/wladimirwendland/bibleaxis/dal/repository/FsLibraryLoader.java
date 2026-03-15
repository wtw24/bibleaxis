/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BooksDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.domain.repository.LibraryLoader;
import de.wladimirwendland.bibleaxis.utils.FsUtils;
import de.wladimirwendland.bibleaxis.utils.OnlyBQIni;
import de.wladimirwendland.bibleaxis.utils.OnlyBQZipIni;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class FsLibraryLoader implements LibraryLoader {

    @NonNull
    private final List<File> mModulesDirs;
    @NonNull
    private final BibleAxisModuleRepository mModuleRepository;

    public FsLibraryLoader(@NonNull List<File> modulesDirs, @NonNull BibleAxisModuleRepository moduleRepository) {
        this.mModulesDirs = Collections.unmodifiableList(modulesDirs);
        this.mModuleRepository = moduleRepository;
    }

    @NonNull
    @Override
    public synchronized Map<String, BaseModule> loadFileModules() {
        StaticLogger.info(this, "Load modules info");

        final List<File> libraryDirs = prepareLibraryDirs(mModulesDirs);
        if (libraryDirs.size() == 0) {
            StaticLogger.error(this, "Module library folder not found");
            return Collections.emptyMap();
        }

        Map<String, BaseModule> result = new TreeMap<>();

        // Load zip-compressed BQ-modules
        List<File> bqZipIniFiles = searchModules(libraryDirs, new OnlyBQZipIni());
        StaticLogger.info(this, "Load zip-modules info");
        for (File bqZipIniFile : bqZipIniFiles) {
            StaticLogger.info(this, "\t- " + bqZipIniFile);
            try {
                BaseModule module = loadFileModule(bqZipIniFile);
                result.put(module.getID(), module);
            } catch (OpenModuleException | BookDefinitionException | BooksDefinitionException e) {
                StaticLogger.error(this, e.getMessage(), e);
            }
        }

        // Load standard BQ-modules
        List<File> bqIniFiles = searchModules(libraryDirs, new OnlyBQIni());
        StaticLogger.info(this, "Load standard modules info");
        for (File item : bqIniFiles) {
            StaticLogger.info(this, "\t- " + item);
            try {
                BaseModule module = loadFileModule(item.getParentFile());
                result.put(module.getID(), module);
            } catch (OpenModuleException | BookDefinitionException | BooksDefinitionException e) {
                StaticLogger.error(this, e.getMessage(), e);
            }
        }

        return result;
    }

    @Override
    public BaseModule loadModule(File file) throws OpenModuleException, BooksDefinitionException,
            BookDefinitionException {
        return loadFileModule(file);
    }

    private BaseModule loadFileModule(File moduleDataSourceId)
            throws OpenModuleException, BooksDefinitionException, BookDefinitionException {
        return mModuleRepository.loadModule(moduleDataSourceId);
    }

    private List<File> prepareLibraryDirs(List<File> libraryDirs) {
        List<File> result = new ArrayList<>();
        for (File item : libraryDirs) {
            if (item.exists() || item.mkdirs()) {
                result.add(item);
            } else {
                StaticLogger.error(this, "Library directory inaccessible - " + item.getAbsolutePath());
            }
        }
        return result;
    }

    /**
     * Выполняет поиск папок с модулями Цитаты на внешнем носителе устройства
     *
     * @return Возвращает ArrayList со списком ini-файлов модулей
     */
    private List<File> searchModules(@NonNull List<File> libraryDirs, @NonNull FileFilter filter) {
        List<File> result = new ArrayList<>();
        for (File item : libraryDirs) {
            // Рекурсивная функция проходит по всем каталогам в поисках ini-файлов Цитаты
            FsUtils.searchByFilter(item, result, filter);
        }
        return result;
    }
}
