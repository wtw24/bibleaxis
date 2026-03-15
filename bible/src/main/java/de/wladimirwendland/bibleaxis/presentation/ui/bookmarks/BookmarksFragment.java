/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.di.component.FragmentComponent;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.presentation.dialogs.BookmarksDialog;
import de.wladimirwendland.bibleaxis.presentation.dialogs.DialogUiHelper;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseFragment;
import de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.adapter.BookmarkAdapter;
import de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.adapter.ClickableListAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

/**
 * User: Vladimir Yakushev
 * Date: 14.05.13
 */
public class BookmarksFragment extends BaseFragment<BookmarksPresenter> implements BookmarksView {

    @BindView(R.id.list_bookmarks)
    RecyclerView viewBookmarksList;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        unbinder = ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        viewBookmarksList.setLayoutManager(new LinearLayoutManager(requireContext()));
        viewBookmarksList.setHasFixedSize(true);
        viewBookmarksList.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayout.VERTICAL));

        try {
            presenter.setChangeListener((OnBookmarksChangeListener) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity().toString() + " must implement OnBookmarksChangeListener");
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        StaticLogger.info(this, "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_bookmarks, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.drawable.ic_dialog_header_logo);
                builder.setTitle(R.string.bookmarks);
                builder.setView(DialogUiHelper.createMessageView(requireActivity(), getString(R.string.fav_delete_all_question)));
                builder.setPositiveButton("OK", (dialog, which) -> presenter.removeBookmarks());
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
                break;

            case R.id.action_bar_refresh:
                presenter.onRefresh();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void inject(FragmentComponent component) {
        component.inject(this);
    }

    @Override
    protected void attachView() {
        presenter.attachView(this);
    }

    @Override
    public void setTagFilter(Tag tag) {
        presenter.onSetTag(tag);
    }

    @Override
    public void startBookmarkAction(String title) {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        ActionMode currActionMode = activity
                .startSupportActionMode(new BookmarksSelectAction(activity, presenter));
        if (currActionMode != null) {
            currActionMode.setTitle(title);
        }
    }

    @Override
    public void openBookmarkDialog(Bookmark bookmark) {
        DialogFragment bmDial = BookmarksDialog.newInstance(bookmark);
        bmDial.show(requireActivity().getSupportFragmentManager(), "bookmark");
    }

    @Override
    public void updateBookmarks(@NonNull final List<Bookmark> bookmarks) {
        ClickableListAdapter.OnClickListener clickListener = new ClickableListAdapter.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewBookmarksList.getChildAdapterPosition(v);
                presenter.onClickBookmarkOpen(position);
            }

            @Override
            public void onLongClick(View v) {
                int position = viewBookmarksList.getChildAdapterPosition(v);
                presenter.onSelectBookmark(position);
            }
        };

        BookmarkAdapter.OnDeleteClickListener deleteClickListener = new BookmarkAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(R.drawable.ic_dialog_header_logo)
                        .setTitle(R.string.app_name)
                        .setView(DialogUiHelper.createMessageView(requireActivity(), getString(R.string.fav_question_del_fav)))
                        .setPositiveButton("OK", (dialog, which) -> presenter.onClickBookmarkDelete(position))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        };

        viewBookmarksList.setAdapter(new BookmarkAdapter(bookmarks, clickListener, deleteClickListener));
    }

    @Override
    public void refreshBookmarks() {
        presenter.onRefresh();
    }

    private static class BookmarksSelectAction implements ActionMode.Callback {

        private final Context context;
        private final BookmarksPresenter presenter;
        private final MenuInflater menuInflater;

        BookmarksSelectAction(Activity activity, BookmarksPresenter presenter) {
            this.presenter = presenter;
            this.context = activity;
            this.menuInflater = activity.getMenuInflater();
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menuInflater.inflate(R.menu.menu_action_bookmark_select, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    presenter.onClickBookmarkEdit();
                    break;
                case R.id.action_delete:
                    new AlertDialog.Builder(context)
                            .setIcon(R.drawable.ic_dialog_header_logo)
                            .setTitle(R.string.app_name)
                            .setView(DialogUiHelper.createMessageView(context, context.getString(R.string.fav_question_del_fav)))
                            .setPositiveButton("OK", (dialog, which) -> presenter.onClickBookmarkDelete())
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                    break;
                default:
                    return false;
            }

            mode.finish();
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }
    }
}
