/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.di.component.FragmentComponent;
import de.wladimirwendland.bibleaxis.domain.entity.TagWithCount;
import de.wladimirwendland.bibleaxis.presentation.dialogs.DialogUiHelper;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseFragment;
import de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.adapter.ClickableListAdapter;
import de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.adapter.TagsAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TagsFragment extends BaseFragment<TagsPresenter> implements TagsView {

    @BindView(R.id.tags_list) RecyclerView viewTagsList;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tags, container, false);

        unbinder = ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        viewTagsList.setLayoutManager(new LinearLayoutManager(getContext()));
        viewTagsList.setHasFixedSize(true);
        viewTagsList.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayout.VERTICAL));

        try {
            presenter.setChangeListener((OnTagsChangeListener) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnTagsChangeListener");
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tags, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_refresh:
                presenter.refreshTags();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateTags(List<TagWithCount> items) {
        if (viewTagsList == null) {
            return;
        }

        viewTagsList.setAdapter(new TagsAdapter(items, new ClickableListAdapter.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = viewTagsList.getChildAdapterPosition(v);
                presenter.onTagSelected(pos);
            }

            @Override
            public void onLongClick(View v) {
                int pos = viewTagsList.getChildAdapterPosition(v);
                new AlertDialog.Builder(getActivity())
                        .setIcon(R.drawable.ic_dialog_header_logo)
                        .setTitle(R.string.app_name)
                        .setView(DialogUiHelper.createMessageView(requireActivity(), getString(R.string.question_del_tag)))
                        .setPositiveButton("OK", (dialog, which) -> presenter.onDeleteTag(pos))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }));
    }

    @Override
    public void refreshTags() {
        presenter.refreshTags();
    }

    @Override
    protected void inject(FragmentComponent component) {
        component.inject(this);
    }

    @Override
    protected void attachView() {
        presenter.attachView(this);
    }
}
