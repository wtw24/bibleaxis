/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.adapter;

import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.domain.entity.TagWithCount;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TagsAdapter extends ClickableListAdapter<TagWithCount> {

    public TagsAdapter(@NonNull List<TagWithCount> items, @NonNull OnClickListener clickListener) {
        super(items, clickListener);
    }

    @Override
    public ListViewHolder<TagWithCount> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_tag);
        return new TagViewHolder(view);
    }

    public static class TagViewHolder extends ListViewHolder<TagWithCount> {

        @BindView(R.id.tag_name) TextView viewName;
        @BindView(R.id.tag_count) TextView viewCount;

        TagViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(TagWithCount tag) {
            if (tag != null) {
                viewName.setText(tag.tag().toString());
                viewCount.setText(tag.count());
            }
        }
    }
}
