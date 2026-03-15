/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository;

import de.wladimirwendland.bibleaxis.domain.entity.ModuleList;
import de.wladimirwendland.bibleaxis.domain.exceptions.DataAccessException;
import de.wladimirwendland.bibleaxis.domain.repository.ICacheRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class FsCacheRepository implements ICacheRepository {

    private final File cache;

    public FsCacheRepository(File cacheDir) {
        this.cache = cacheDir;
    }

    @Override
    public ModuleList getData() throws DataAccessException {
        StaticLogger.info(this, "Loading data from a file system cache.");
        ModuleList result;
        try (
                FileInputStream fStr = new FileInputStream(cache);
                ObjectInputStream out = new ObjectInputStream(fStr)
        ) {
            result = (ModuleList) out.readObject();
        } catch (ClassNotFoundException e) {
            String message = String.format("Unexpected data format in the cache %s: %s",
                    cache.getAbsolutePath(), e.getMessage());
            throw new DataAccessException(message);
        } catch (IOException e) {
            String message = String.format("Data isn't loaded from the cache %s: %s",
                    cache.getAbsolutePath(), e.getMessage());
            throw new DataAccessException(message);
        } catch (ClassCastException e) {
            String message = String.format("Data isn't cast to ModuleList from the cache %s: %s",
                    cache.getAbsolutePath(), e.getMessage());
            throw new DataAccessException(message);
        }

        return result;
    }

    @Override
    public void saveData(ModuleList data) throws DataAccessException {
        StaticLogger.info(this, "Save modules to a file system cache.");
        try (
                FileOutputStream fStr = new FileOutputStream(cache);
                ObjectOutputStream out = new ObjectOutputStream(fStr)
        ) {
            out.writeObject(data);
        } catch (IOException e) {
            String message = String.format("Data isn't stored in the cache %s: %s",
                    cache.getAbsolutePath(), e.getMessage());
            throw new DataAccessException(message);
        }
    }

    @Override
    public boolean isCacheExist() {
        boolean exists = cache.exists();
        if (!exists) {
            StaticLogger.info(this, "Modules list cache not found");
        }
        return exists;
    }
}
