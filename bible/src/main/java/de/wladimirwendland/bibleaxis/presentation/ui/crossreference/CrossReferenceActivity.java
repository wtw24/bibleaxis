/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.crossreference;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.async.task.command.AsyncCommand;
import de.wladimirwendland.bibleaxis.async.task.command.AsyncCommand.ICommand;
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.exceptions.ExceptionHelper;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.managers.Librarian;
import de.wladimirwendland.bibleaxis.presentation.ui.base.AsyncTaskActivity;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.ItemAdapter;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.Item;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.SubtextItem;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.TextItem;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;
import de.wladimirwendland.bibleaxis.utils.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CrossReferenceActivity extends AsyncTaskActivity {

    private static final String TAG = CrossReferenceActivity.class.getSimpleName();
	private ListView LV;
    private BibleReference bReference;
    private LinkedHashMap<String, BibleReference> crossReference = new LinkedHashMap<>();
    private HashMap<BibleReference, String> crossReferenceContent = new HashMap<>();
	private AdapterView.OnItemClickListener list_OnClick = (a, v, position, id) -> {
		String key = ((TextItem) a.getAdapter().getItem(position)).text;
		BibleReference ref = crossReference.get(key);

		Intent intent = new Intent();
		intent.putExtra("linkOSIS", ref.getPath());
		setResult(RESULT_OK, intent);
		finish();
	};
	private Librarian myLibrarian;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parallels_list);

		BibleAxisApp app = (BibleAxisApp) getApplication();
		myLibrarian = app.getLibrarian();

		LV = (ListView) findViewById(R.id.Parallels_List);
		LV.setOnItemClickListener(list_OnClick);

		Intent parent = getIntent();
		String link = parent.getStringExtra("linkOSIS");
		if (link == null) {
			finish();
			return;
		}
		bReference = new BibleReference(link);

		String bookName;
		try {
			bookName = myLibrarian.getBookFullName(bReference.getModuleID(), bReference.getBookID());
		} catch (OpenModuleException e) {
			bookName = bReference.getBookFullName();
		}

		TextView referenceSource = (TextView) findViewById(R.id.referenceSource);
		referenceSource.setText(String.format("%1$s %2$s:%3$s", bookName, bReference.getChapter(), bReference.getFromVerse()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mAsyncManager.isWorking()) {
            String progressMessage = getString(R.string.messageLoad);
            mAsyncManager.setupTask(new AsyncCommand(new GetParallelsLinks(), progressMessage, false), this);
        }
    }

    @Override
    protected void inject(ActivityComponent component) {
		component.inject(this);
    }

    @Override
	public void onTaskComplete(Task task) {
		if (task != null && !task.isCancelled()) {
			if (task instanceof AsyncCommand) {
				AsyncCommand t = (AsyncCommand) task;
				if (t.isSuccess()) {
					setListAdapter();
				} else {
					Exception e = t.getException();
					ExceptionHelper.onException(e, this, TAG);
				}
			}
		}
	}

	private void setListAdapter() {
        List<Item> items = new ArrayList<>();
        PreferenceHelper prefHelper = BibleAxisApp.getInstance().getPrefHelper();
		for (Map.Entry<String, BibleReference> entry : crossReference.entrySet()) {
			if (prefHelper.crossRefViewDetails()) {
				items.add(new SubtextItem(entry.getKey(), crossReferenceContent.get(entry.getValue())));
			} else {
				items.add(new TextItem(entry.getKey()));
			}
		}

		ItemAdapter adapter = new ItemAdapter(this, items);
		LV.setAdapter(adapter);
	}

    private class GetParallelsLinks implements ICommand {

        @Override
        public boolean execute() throws Exception {
            crossReference = myLibrarian.getCrossReference(bReference);
            PreferenceHelper prefHelper = BibleAxisApp.getInstance().getPrefHelper();
            if (prefHelper.crossRefViewDetails()) {
                crossReferenceContent = myLibrarian.getCrossReferenceContent(crossReference.values());
			}
            return true;
        }
	}
}