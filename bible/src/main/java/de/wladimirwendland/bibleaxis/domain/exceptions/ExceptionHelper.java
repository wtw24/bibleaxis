/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.exceptions;

import android.content.Context;
import android.util.Log;

import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.presentation.dialogs.NotifyDialog;

public final class ExceptionHelper {

    private ExceptionHelper() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    public static void onException(Throwable ex, Context context, String tag) {
        String message = ex.getMessage();
        if (message == null) {
            return;
        }
        Log.e(tag, message);
        new NotifyDialog(message, context).show();
    }

    public static void onOpenModuleException(OpenModuleException ex, Context context, String tag) {
        String moduleId = ex.getModuleId();
        if (moduleId == null) moduleId = "";
        String moduleDatasourceId = ex.getModuleDatasourceId();
        if (moduleDatasourceId == null) moduleDatasourceId = "";


        if (moduleId.equals("") && moduleDatasourceId.equals("")) {
            return;
        }

        String message;
        if (!moduleId.equals("") && !moduleDatasourceId.equals("")) {
            message = String.format(
                    context.getResources().getString(R.string.exception_open_module_short),
                    moduleId);
        } else {
            message = String.format(
                    context.getResources().getString(R.string.exception_open_module_short),
                    !moduleId.equals("") ? moduleId : moduleDatasourceId);
        }
        Log.e(tag, message);
        new NotifyDialog(message, context).show();
    }

    public static void onBooksDefinitionException(BooksDefinitionException ex, Context context, String tag) {
        String message = String.format(
                context.getResources().getString(R.string.exception_books_definition),
                ex.getModuleDatasourceID(), ex.getBooksCount(),
                ex.getPathNameCount(), ex.getFullNameCount(), ex.getShortNameCount(), ex.getChapterQtyCount());
        Log.e(tag, message);
        new NotifyDialog(message, context).show();
    }

    public static void onBookDefinitionException(BookDefinitionException ex, Context context, String tag) {
        String message = String.format(
                context.getResources().getString(R.string.exception_book_definition),
                ex.getBookNumber(), ex.getModuleDatasourceID());
        Log.e(tag, message);
        new NotifyDialog(message, context).show();
    }

    public static void onBookNotFoundException(BookNotFoundException ex, Context context, String tag) {
        String message = String.format(
                context.getResources().getString(R.string.exception_book_not_found),
                ex.getBookID(), ex.getModuleID());
        Log.e(tag, message);
        new NotifyDialog(message, context).show();
    }
}
