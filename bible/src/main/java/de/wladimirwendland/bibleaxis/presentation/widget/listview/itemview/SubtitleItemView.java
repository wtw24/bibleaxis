/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.widget.listview.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.Item;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.SubtitleItem;

public class SubtitleItemView extends LinearLayout implements ItemView {

	private TextView mTextView;
	private TextView mSubtitleView;

	public SubtitleItemView(Context context) {
		this(context, null);
	}

	public SubtitleItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void prepareItemView() {
		mTextView = (TextView) findViewById(R.id.text);
		mSubtitleView = (TextView) findViewById(R.id.subtitletext);
	}

	public void setObject(Item object) {
		final SubtitleItem item = (SubtitleItem) object;
		mTextView.setText(item.text);
		mSubtitleView.setText(item.subtitletext);
	}

}
