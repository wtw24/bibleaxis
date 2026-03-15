/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.controller;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import android.util.Log;

import de.wladimirwendland.bibleaxis.dal.repository.BibleAxisModuleRepository;
import de.wladimirwendland.bibleaxis.domain.controller.IModuleController;
import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;
import de.wladimirwendland.bibleaxis.domain.search.MultiThreadSearchProcessor;
import de.wladimirwendland.bibleaxis.entity.modules.BibleAxisModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class BibleAxisModuleController implements IModuleController {

    private static final String TAG = BibleAxisModuleController.class.getSimpleName();

    private BibleAxisModule module;
    private BibleAxisModuleRepository repository;

    public BibleAxisModuleController(BibleAxisModule module, BibleAxisModuleRepository repository) {
        this.module = module;
        this.repository = repository;
    }

    @Override
    public List<Book> getBooks() {
        return new ArrayList<>(module.getBooks().values());
    }

    @Override
    public Bitmap getBitmap(String path) {
        return repository.getBitmap(module, path);
    }

    @Override
    @NonNull
    public Book getBookByID(String bookId) throws BookNotFoundException {
        Map<String, Book> books = module.getBooks();
        Book result = books.get(bookId);
        if (result == null) {
            throw new BookNotFoundException(module.getID(), bookId);
        }
        return result;
    }

    @Override
    public Book getNextBook(String bookId) throws BookNotFoundException {
        Book result = getBookByID(bookId);
        List<Book> books = getBooks();
        int pos = books.indexOf(result);
        if (books.size() > ++pos) {
            return books.get(pos);
        }
        return null;
    }

    @Override
    public Book getPrevBook(String bookId) throws BookNotFoundException {
        Book result = getBookByID(bookId);
        List<Book> books = getBooks();
        int pos = books.indexOf(result);
        if (pos > 0) {
            return books.get(--pos);
        }
        return null;
    }

    @Override
    public List<String> getChapterNumbers(String bookId) throws BookNotFoundException {
        ArrayList<String> result = new ArrayList<>();
        Book book = getBookByID(bookId);
        for (int i = 0; i < book.getChapterQty(); i++) {
            result.add("" + (i + (module.isChapterZero() ? 0 : 1)));
        }
        return result;
    }

    @Override
    public Chapter getChapter(String bookId, int chapter) throws BookNotFoundException {
        return repository.loadChapter(module, bookId, chapter);
    }

    @Override
    public Map<String, String> search(List<String> bookList, String searchQuery, boolean wholeWordsMatch) {
        long searchStart = System.currentTimeMillis();
        Map<String, String> result = new MultiThreadSearchProcessor<>(repository).search(module, bookList, searchQuery, wholeWordsMatch);
        long searchEnd = System.currentTimeMillis();
        Log.d(TAG, String.format(Locale.US, "Search '%s' completed in %d ms", searchQuery, searchEnd - searchStart));
        return result;
    }
}
