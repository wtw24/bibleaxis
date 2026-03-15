/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.repository;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BooksDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;

import java.io.File;

/**
 *
 */
public interface IModuleRepository<T extends BaseModule> {

    Bitmap getBitmap(T module, String path);

    Chapter loadChapter(T module, String bookID, int chapter) throws BookNotFoundException;

    T loadModule(File path) throws OpenModuleException, BooksDefinitionException, BookDefinitionException;

    @NonNull
    String getBookContent(T module, String bookID) throws BookNotFoundException;
}
