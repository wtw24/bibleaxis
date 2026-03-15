/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.controller;

import android.graphics.Bitmap;

import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface IModuleController {

    List<Book> getBooks();

    Bitmap getBitmap(String path);

    Book getBookByID(String bookId) throws BookNotFoundException;

    Book getNextBook(String bookId) throws BookNotFoundException;

    Book getPrevBook(String bookId) throws BookNotFoundException;

    List<String> getChapterNumbers(String bookId) throws BookNotFoundException;

    Chapter getChapter(String bookId, int chapter) throws BookNotFoundException;

    Map<String, String> search(List<String> bookList, String searchQuery, boolean wholeWordsMatch);
}
