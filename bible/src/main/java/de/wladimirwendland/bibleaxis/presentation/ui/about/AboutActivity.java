/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.about;

import android.os.Bundle;
import android.view.MenuItem;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.text.HtmlCompat;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseAppActivity;

public class AboutActivity extends BaseAppActivity {

    private static final String ABOUT_VERSION = "1.0.0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		ActionBar supportActionBar = getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		}

		TextView tvApp = (TextView) findViewById(R.id.about_name);
		tvApp.setText(String.format(
				getResources().getText(R.string.app_about_name).toString(),
				ABOUT_VERSION));

        TextView tvThanks = findViewById(R.id.about_thanks);
        String thanksWithLink = getString(R.string.app_about_thanks)
                + " <a href=\"https://github.com/YakushevVladimir/DeskBible\">DeskBible</a>";
        tvThanks.setText(HtmlCompat.fromHtml(thanksWithLink, HtmlCompat.FROM_HTML_MODE_LEGACY));
        tvThanks.setMovementMethod(LinkMovementMethod.getInstance());
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

    }
}
