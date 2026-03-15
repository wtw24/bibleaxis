/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class ClickableListAdapter<T> extends RecyclerView.Adapter<ClickableListAdapter.ListViewHolder<T>> {

    private OnClickListener clickListener;
    private List<T> items;

    ClickableListAdapter(@NonNull List<T> items, @NonNull OnClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(ListViewHolder<T> holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    View getView(ViewGroup parent, int resId) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        view.setOnClickListener(v -> clickListener.onClick(v));
        view.setOnLongClickListener(v -> {
            clickListener.onLongClick(v);
            return true;
        });
        return view;
    }

    public interface OnClickListener {

        void onClick(View v);

        void onLongClick(View v);
    }

    abstract static class ListViewHolder<T> extends RecyclerView.ViewHolder {

        ListViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(T bookmark);
    }
}
