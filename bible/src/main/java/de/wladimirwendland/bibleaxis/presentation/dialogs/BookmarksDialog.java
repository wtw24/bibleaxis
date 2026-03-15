/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.di.component.FragmentComponent;
import de.wladimirwendland.bibleaxis.di.module.FragmentModule;
import de.wladimirwendland.bibleaxis.domain.AnalyticsHelper;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.managers.bookmarks.BookmarksManager;
import de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.BookmarksActivity;

import javax.inject.Inject;

import de.wladimirwendland.bibleaxis.di.app.AppComponent;

public class BookmarksDialog extends DialogFragment {

    private Bookmark bookmark;
    private TextView tvDate, tvHumanLink, tvName;
    private EditText tvTags;

    @Inject AnalyticsHelper analyticsHelper;
    @Inject BookmarksManager bookmarksManager;

    public static BookmarksDialog newInstance(Bookmark bookmark) {
        // TODO: 29.09.17 переделать на Bundle
        BookmarksDialog result = new BookmarksDialog();
        result.setBookmark(bookmark);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentComponent().inject(this);
    }

    private FragmentComponent getFragmentComponent() {
        AppComponent appComponent = BibleAxisApp.instance(getContext()).getAppComponent();
        return appComponent.fragmentComponent(new FragmentModule());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_dialog_header_logo)
                .setTitle(R.string.bookmarks)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> addBookmarks())
                .setNegativeButton(android.R.string.cancel, null);
        View customView = inflater.inflate(R.layout.bookmarks_dialog, null);
        builder.setView(customView);

        tvDate = (TextView) customView.findViewById(R.id.bm_date);
        tvHumanLink = (TextView) customView.findViewById(R.id.bm_humanLink);
        tvName = (TextView) customView.findViewById(R.id.bm_name);
        tvTags = (EditText) customView.findViewById(R.id.bm_tags);

        fillField();

        return builder.create();
    }

    private void addBookmarks() {
        readField();
        bookmarksManager.add(bookmark);
        analyticsHelper.bookmarkEvent(bookmark);

        if (getActivity() instanceof BookmarksActivity) {
            ((BookmarksActivity) getActivity()).onBookmarksUpdate();
            ((BookmarksActivity) getActivity()).onTagsUpdate();
        }

        Toast.makeText(getActivity(), getString(R.string.added), Toast.LENGTH_LONG).show();
        dismiss();
    }

    private void fillField() {
        if (bookmark != null) {
            tvDate.setText(bookmark.date);
            tvHumanLink.setText(bookmark.humanLink);
            tvName.setText(bookmark.name);
            tvTags.setText(bookmark.tags);
        }
    }

    private void readField() {
        if (bookmark != null) {
            bookmark.humanLink = tvHumanLink.getText().toString();
            bookmark.name = tvName.getText().toString();
            bookmark.date = tvDate.getText().toString();
            bookmark.tags = tvTags.getText().toString();

            if (TextUtils.isEmpty(bookmark.name)) {
                bookmark.name = bookmark.humanLink;
            }
        }
    }

    public void setBookmark(Bookmark bookmark) {
        this.bookmark = bookmark;
    }
}
