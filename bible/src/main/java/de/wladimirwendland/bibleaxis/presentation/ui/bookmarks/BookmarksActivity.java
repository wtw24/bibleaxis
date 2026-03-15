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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.app.NavUtils;
import androidx.viewpager.widget.ViewPager;
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

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.tab_layout) TabLayout tabLayout;

    private PagerAdapter pagerAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addPage(getString(R.string.bookmarks), new BookmarksFragment());
        pagerAdapter.addPage(getString(R.string.tags), new TagsFragment());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_MODE)) {
            final String mode = intent.getStringExtra(EXTRA_MODE);
            viewPager.setCurrentItem(mode.equals(MODE_BOOKMARKS) ? 0 : 1);
        } else if (savedInstanceState != null) {
            viewPager.setCurrentItem(savedInstanceState.getInt(KEY_TAB));
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
        viewPager.setCurrentItem(state.getInt(KEY_TAB));
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
        ((TagsFragment) pagerAdapter.getItem(1)).refreshTags();
    }

    @Override
    public void onTagSelect(Tag tag) {
        viewPager.setCurrentItem(0);
        ((BookmarksFragment) pagerAdapter.getItem(0)).setTagFilter(tag);
    }

    @Override
    public void onTagsUpdate() {
        ((BookmarksFragment) pagerAdapter.getItem(0)).refreshBookmarks();
    }

    private static class PagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> pages = new ArrayList<>();
        private List<String> titles = new ArrayList<>();

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public Fragment getItem(int position) {
            if (pages.size() > position) {
                return pages.get(position);
            }
            return new Fragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (titles.size() > position) {
                return titles.get(position);
            }
            return "";
        }

        void addPage(String name, Fragment page) {
            titles.add(name);
            pages.add(page);
        }

    }
}
