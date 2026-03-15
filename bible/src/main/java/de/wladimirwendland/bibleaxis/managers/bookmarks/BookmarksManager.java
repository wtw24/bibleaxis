/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.managers.bookmarks;

import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.domain.repository.IBookmarksRepository;
import de.wladimirwendland.bibleaxis.domain.repository.ITagsRepository;

import java.util.List;

public class BookmarksManager {

    private ITagsRepository tagsRepository;
    private IBookmarksRepository bookmarksRepository;

    public BookmarksManager(IBookmarksRepository repository, ITagsRepository tagsRepository) {
        this.bookmarksRepository = repository;
        this.tagsRepository = tagsRepository;
    }

    public boolean add(Bookmark bookmark) {
        long bmID = bookmarksRepository.add(bookmark);
        if (bmID == -1) { // при добавлении закладки получили ошибку
            return false;
        }
        tagsRepository.addTags(bmID, bookmark.tags); // записываем новые
        return true;
    }

    public void delete(Bookmark bookmark) {
        bookmarksRepository.delete(bookmark);
    }

    public List<Bookmark> getAll() {
        return bookmarksRepository.getAll(null);
    }

    public List<Bookmark> getAll(Tag tag) {
        return bookmarksRepository.getAll(tag);
    }
}