/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.widget.listview.itemview;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.Item;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.SubtextItem;

public class SubtextItemView extends LinearLayout implements ItemView {

	private TextView mTextView;
	private TextView mSubtextView;

	public SubtextItemView(Context context) {
		this(context, null);
	}

	public SubtextItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void prepareItemView() {
		mTextView = (TextView) findViewById(R.id.text);
		mSubtextView = (TextView) findViewById(R.id.subtext);
	}

	public void setObject(Item object) {
		final SubtextItem item = (SubtextItem) object;
		mTextView.setText(item.text);
		mSubtextView.setText(Html.fromHtml(item.subtext));
	}

}
