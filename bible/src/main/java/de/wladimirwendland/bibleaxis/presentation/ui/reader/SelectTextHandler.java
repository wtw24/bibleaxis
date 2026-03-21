/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.reader;

import androidx.fragment.app.DialogFragment;
import androidx.appcompat.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.managers.Librarian;
import de.wladimirwendland.bibleaxis.presentation.dialogs.BookmarksDialog;
import de.wladimirwendland.bibleaxis.presentation.widget.ReaderWebView;
import de.wladimirwendland.bibleaxis.utils.share.ShareBuilder;

import java.util.List;
import java.util.TreeSet;

/**
 * @author Vladimir Yakushev
 * @version 1.0 of 01.2016
 */
final class SelectTextHandler implements ActionMode.Callback {

    private final ReaderActivity readerActivity;
    private final ReaderWebView webView;

    SelectTextHandler(ReaderActivity readerActivity, ReaderWebView webView) {
        this.readerActivity = readerActivity;
        this.webView = webView;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        readerActivity.getMenuInflater().inflate(R.menu.menu_action_text_select, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        TreeSet<Integer> selVerses = webView.getSelectedVerses();
        if (selVerses.isEmpty()) {
            return true;
        }

        Librarian myLibrarian = BibleAxisApp.getInstance().getLibrarian();

        switch (item.getItemId()) {
            case R.id.action_bookmarks:
                int firstVerse = selVerses.first();
                int lastVerse = selVerses.last();
                myLibrarian.setCurrentVerseNumber(firstVerse);
                Bookmark bookmark = new Bookmark(myLibrarian.getCurrentOSISLink());
                int firstDisplayedVerse = myLibrarian.getDisplayedVerseNumber(firstVerse);
                int lastDisplayedVerse = myLibrarian.getDisplayedVerseNumber(lastVerse);
                bookmark.humanLink = buildBookmarkLink(myLibrarian, firstDisplayedVerse, lastDisplayedVerse);
                bookmark.name = buildBookmarkName(firstVerse, selVerses.size() > 1, myLibrarian.getCleanedVersesText());
                DialogFragment bmDial = BookmarksDialog.newInstance(bookmark);
                bmDial.show(readerActivity.getSupportFragmentManager(), "bookmark");
                break;

            case R.id.action_share:
                myLibrarian.shareText(readerActivity, selVerses, ShareBuilder.Destination.ActionSend);
                break;

            case R.id.action_copy:
                myLibrarian.shareText(readerActivity, selVerses, ShareBuilder.Destination.Clipboard);
                Toast.makeText(readerActivity, readerActivity.getString(R.string.added), Toast.LENGTH_LONG).show();
                break;

            case R.id.action_references:
                myLibrarian.setCurrentVerseNumber(selVerses.first());
                readerActivity.openCrossReferenceActivity(myLibrarian.getCurrentOSISLink().getPath());
                break;

            default:
                return false;
        }

        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        webView.clearSelectedVerse();
    }

    private String buildBookmarkName(int firstVerse, boolean addEllipsis, List<String> versesText) {
        StringBuilder title = new StringBuilder();
        String verseText = "";
        int verseIndex = firstVerse - 1;
        if (verseIndex >= 0 && verseIndex < versesText.size()) {
            verseText = versesText.get(verseIndex).trim().replaceAll("\\s+", " ");
        }

        if (!verseText.isEmpty()) {
            title.append(verseText);
        }

        if (addEllipsis) {
            title.append(" ...");
        }

        return title.toString();
    }

    private String buildBookmarkLink(Librarian librarian, int firstVerse, int lastVerse) {
        StringBuilder link = new StringBuilder()
                .append(librarian.getModuleID())
                .append(" - ")
                .append(librarian.getHumanBookLink())
                .append(':')
                .append(firstVerse);
        if (lastVerse > firstVerse) {
            link.append('-').append(lastVerse);
        }
        return link.toString();
    }
}
