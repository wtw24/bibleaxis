/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.app.NavUtils;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;
import de.wladimirwendland.bibleaxis.domain.entity.Tag;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseAppActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookmarksActivity extends BaseAppActivity
        implements OnBookmarksChangeListener, OnTagsChangeListener {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_TAGS = "tags";
    public static final String MODE_BOOKMARKS = "bookmarks";

    private static final String KEY_TAB = "tab";
    private static final int BOOKMARKS_TAB_POSITION = 0;
    private static final int TAGS_TAB_POSITION = 1;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.pager) ViewPager2 viewPager;
    @BindView(R.id.tab_layout) TabLayout tabLayout;

    private BookmarksFragment bookmarksFragment;
    private TagsFragment tagsFragment;
    private PagerAdapter pagerAdapter;
    private TabLayoutMediator tabLayoutMediator;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        bookmarksFragment = new BookmarksFragment();
        tagsFragment = new TagsFragment();
        pagerAdapter = new PagerAdapter(this);
        pagerAdapter.addPage(bookmarksFragment);
        pagerAdapter.addPage(tagsFragment);
        viewPager.setAdapter(pagerAdapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == BOOKMARKS_TAB_POSITION) {
                tab.setText(getString(R.string.bookmarks));
            } else if (position == TAGS_TAB_POSITION) {
                tab.setText(getString(R.string.tags));
            }
        });
        tabLayoutMediator.attach();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_MODE)) {
            final String mode = intent.getStringExtra(EXTRA_MODE);
            viewPager.setCurrentItem(mode.equals(MODE_BOOKMARKS) ? BOOKMARKS_TAB_POSITION : TAGS_TAB_POSITION, false);
        } else if (savedInstanceState != null) {
            viewPager.setCurrentItem(savedInstanceState.getInt(KEY_TAB), false);
        }
    }

    @Override
    protected void inject(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TAB, viewPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        viewPager.setCurrentItem(state.getInt(KEY_TAB), false);
    }

    @Override
    protected void onDestroy() {
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBookmarksSelect(Bookmark bookmark) {
        Intent intent = new Intent();
        intent.putExtra("linkOSIS", bookmark.OSISLink);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBookmarksUpdate() {
        tagsFragment.refreshTags();
    }

    @Override
    public void onTagSelect(Tag tag) {
        viewPager.setCurrentItem(BOOKMARKS_TAB_POSITION);
        bookmarksFragment.setTagFilter(tag);
    }

    @Override
    public void onTagsUpdate() {
        bookmarksFragment.refreshBookmarks();
    }

    private static class PagerAdapter extends FragmentStateAdapter {

        private final List<Fragment> pages = new ArrayList<>();

        PagerAdapter(BookmarksActivity activity) {
            super(activity);
        }

        @Override
        public int getItemCount() {
            return pages.size();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position >= 0 && pages.size() > position) {
                return pages.get(position);
            }
            return new Fragment();
        }

        void addPage(Fragment page) {
            pages.add(page);
        }

    }
}
