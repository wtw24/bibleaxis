/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.managers.Librarian;
import de.wladimirwendland.bibleaxis.managers.bookmarks.BookmarksManager;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

public class BookmarksPresenter extends BasePresenter<BookmarksView> {

    private List<Bookmark> bookmarks;
    private BookmarksManager bookmarksManager;
    private OnBookmarksChangeListener changeListener;
    private Bookmark currBookmark;
    private Librarian myLibrarian;

    @Inject
    BookmarksPresenter(BookmarksManager bookmarksManager, Librarian myLibrarian) {
        this.bookmarksManager = bookmarksManager;
        this.myLibrarian = myLibrarian;
    }

    void setChangeListener(OnBookmarksChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    @Override
    public void onViewCreated() {
        if (changeListener == null) {
            throw new IllegalStateException("OnBookmarksChangeListener is not specified");
        }
        updateBookmarks(null);
    }

    void onClickBookmarkDelete() {
        if (currBookmark == null) {
            return;
        }

        StaticLogger.info(this, "Delete bookmark: " + currBookmark);
        getView().showToast(R.string.removed);
        deleteBookmarkAndRefresh(currBookmark);
    }

    void onClickBookmarkDelete(int position) {
        if (position < 0 || position >= bookmarks.size()) {
            return;
        }

        Bookmark bookmark = bookmarks.get(position);
        StaticLogger.info(this, "Delete bookmark: " + bookmark);
        getView().showToast(R.string.removed);
        deleteBookmarkAndRefresh(bookmark);
    }

    void onClickBookmarkEdit() {
        if (currBookmark != null) {
            getView().openBookmarkDialog(currBookmark);
        }
    }

    void onClickBookmarkOpen(int position) {
        if (position >= bookmarks.size()) {
            return;
        }

        Bookmark bookmark = bookmarks.get(position);
        BibleReference osisLink = new BibleReference(bookmark.OSISLink);
        if (!myLibrarian.isOSISLinkValid(osisLink)) { // модуль был удален и закладка больше не актуальна
            StaticLogger.info(this, "Delete invalid bookmark: " + position);
            getView().showToast(R.string.bookmark_invalid_removed);
            deleteBookmarkAndRefresh(bookmark);
        } else {
            changeListener.onBookmarksSelect(bookmark);
        }
    }

    void onRefresh() {
        updateBookmarks(null);
    }

    void onSelectBookmark(int position) {
        if (position < bookmarks.size()) {
            currBookmark = bookmarks.get(position);
            getView().startBookmarkAction(currBookmark.name);
        }
    }

    void onSetTag(Tag tag) {
        updateBookmarks(tag);
    }

    void removeBookmarks() {
        for (Bookmark bookmark : bookmarks) {
            bookmarksManager.delete(bookmark);
        }
        updateBookmarks(null);
        changeListener.onBookmarksUpdate();
    }

    private void deleteBookmarkAndRefresh(Bookmark bookmark) {
        bookmarksManager.delete(bookmark);
        updateBookmarks(null);
        changeListener.onBookmarksUpdate();
    }

    private void updateBookmarks(Tag tag) {
        bookmarks = bookmarksManager.getAll(tag);
        currBookmark = null;
        getView().updateBookmarks(bookmarks);
    }
}
