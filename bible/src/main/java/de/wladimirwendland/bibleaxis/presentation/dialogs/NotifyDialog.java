/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.dialogs;

import android.app.AlertDialog;
import android.content.Context;

import de.wladimirwendland.bibleaxis.R;

public class NotifyDialog {

    private AlertDialog alertDialog;

    public NotifyDialog(String message, Context context) {
        alertDialog = new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_dialog_header_logo)
                .setTitle(R.string.notify_dialog_title)
                .setView(DialogUiHelper.createMessageView(context, message))
                .setPositiveButton("OK", null)
                .create();
    }

    public void show() {
        alertDialog.show();
    }
}
