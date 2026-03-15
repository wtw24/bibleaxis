/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

import de.wladimirwendland.bibleaxis.domain.exceptions.DataAccessException;

import java.io.BufferedReader;
import java.io.InputStream;

public class FsUtilsWrapper {

    public byte[] getBytes(InputStream stream) {
        return FsUtils.getBytes(stream);
    }

    public InputStream getStreamFromZip(String path, String fileName) {
        return FsUtils.getStreamFromZip(path, fileName);
    }

    public InputStream getStream(String path, String fileName) {
        return FsUtils.getStream(path, fileName);
    }

    public BufferedReader getTextFileReader(String path, String dataSourceID, String encoding) throws DataAccessException {
        return FsUtils.getTextFileReader(path, dataSourceID, encoding);
    }

    public BufferedReader getTextFileReaderFromZipArchive(String path, String dataSourceID, String encoding) throws DataAccessException {
        return FsUtils.getTextFileReaderFromZipArchive(path, dataSourceID, encoding);
    }
}
