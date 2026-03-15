/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.async.task.command;

import android.content.Context;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.domain.exceptions.ExceptionHelper;

import java.util.ArrayList;
import java.util.List;

public class StartSearch implements AsyncCommand.ICommand {
    private static final String TAG = "StartSearch";
    private final String query;
    private final String searchScope;
    private final String fromBookID;
    private final String toBookID;
    private final boolean wholeWordsMatch;
    private final Context context;
    private final ArrayList<String> moduleIDs;
    private final ArrayList<String> selectedBookIDs;

    public StartSearch(
            Context context,
            String query,
            List<String> moduleIDs,
            String searchScope,
            List<String> selectedBookIDs,
            String fromBookID,
            String toBookID,
            boolean wholeWordsMatch) {
        this.context = context;
        this.query = query;
        this.searchScope = searchScope;
        this.fromBookID = fromBookID;
        this.toBookID = toBookID;
        this.wholeWordsMatch = wholeWordsMatch;
        this.moduleIDs = new ArrayList<>();
        if (moduleIDs != null) {
            this.moduleIDs.addAll(moduleIDs);
        }
        this.selectedBookIDs = new ArrayList<>();
        if (selectedBookIDs != null) {
            this.selectedBookIDs.addAll(selectedBookIDs);
        }
    }

    @Override
    public boolean execute() throws Exception {
        if (query == null || query.trim().isEmpty() || moduleIDs.isEmpty()) {
            return false;
        }

        try {
            BibleAxisApp.getInstance().getLibrarian().search(
                    query,
                    moduleIDs,
                    searchScope,
                    selectedBookIDs,
                    fromBookID,
                    toBookID,
                    wholeWordsMatch);
            return true;
        } catch (Exception e) {
            ExceptionHelper.onException(e, context, TAG);
        }

        return false;
    }
}
