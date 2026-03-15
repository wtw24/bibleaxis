/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.adapter;

import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;

import java.text.DateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookmarkAdapter extends ClickableListAdapter<Bookmark> {

    private final OnDeleteClickListener deleteClickListener;

    public BookmarkAdapter(@NonNull List<Bookmark> items,
            @NonNull ClickableListAdapter.OnClickListener clickListener,
            @NonNull OnDeleteClickListener deleteClickListener) {
        super(items, clickListener);
        this.deleteClickListener = deleteClickListener;
    }

    @Override
    public ListViewHolder<Bookmark> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_bookmark);
        return new BookmarkViewHolder(view, deleteClickListener);
    }

    public static class BookmarkViewHolder extends ListViewHolder<Bookmark> {

        @BindView(R.id.bookmark_link) TextView viewLink;
        @BindView(R.id.bookmark_time) TextView viewTime;
        @BindView(R.id.bookmark_title) TextView viewTitle;
        @BindView(R.id.bookmark_tags) TextView viewTags;
        @BindView(R.id.bookmark_delete) ImageButton viewDelete;

        private final OnDeleteClickListener deleteClickListener;

        BookmarkViewHolder(View itemView, OnDeleteClickListener deleteClickListener) {
            super(itemView);
            this.deleteClickListener = deleteClickListener;
            ButterKnife.bind(this, itemView);
            viewDelete.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    deleteClickListener.onDeleteClick(position);
                }
            });
        }

        @Override
        public void bind(Bookmark bookmark) {
            if (bookmark != null) {
                viewLink.setText(bookmark.humanLink);
                viewTime.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(bookmark.time));
                viewTitle.setText(bookmark.name);
                if (!TextUtils.isEmpty(bookmark.tags)) {
                    viewTags.setVisibility(View.VISIBLE);
                    viewTags.setText(bookmark.tags);
                } else {
                    viewTags.setText(null);
                    viewTags.setVisibility(View.GONE);
                }
            }
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
}
