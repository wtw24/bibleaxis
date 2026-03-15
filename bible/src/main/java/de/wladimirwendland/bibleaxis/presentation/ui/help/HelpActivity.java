/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.help;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseAppActivity;
import de.wladimirwendland.bibleaxis.utils.FsUtils;

public class HelpActivity extends BaseAppActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);

		ActionBar supportActionBar = getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		}

		String helpText = FsUtils.getAssetString(getApplicationContext(), "help.html");
		WebView vWeb = (WebView) findViewById(R.id.helpView);
		vWeb.loadDataWithBaseURL("file:///url_initial_load", helpText, "text/html", "UTF-8", "about:config");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected void inject(ActivityComponent component) {
		component.inject(this);
    }
}
