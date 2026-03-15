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
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.TextItem;

public class TextItemView extends LinearLayout implements ItemView {

	private TextView mTextView;

	public TextItemView(Context context) {
		this(context, null);
	}

	public TextItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void prepareItemView() {
		mTextView = (TextView) findViewById(R.id.text);
	}

	public void setObject(Item object) {
		final TextItem item = (TextItem) object;
		mTextView.setText(item.text);
	}
}
