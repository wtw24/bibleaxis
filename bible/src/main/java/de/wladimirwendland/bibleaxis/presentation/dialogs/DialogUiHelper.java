/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.dialogs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public final class DialogUiHelper {

    private DialogUiHelper() {
    }

    @NonNull
    public static View createMessageView(@NonNull Context context, @NonNull CharSequence message) {
        LinearLayout container = createContainer(context);
        TextView messageView = new TextView(context);
        messageView.setText(message);
        container.addView(messageView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return container;
    }

    @NonNull
    public static LinearLayout createContainer(@NonNull Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(context, 24), dp(context, 12), dp(context, 24), 0);
        return container;
    }

    public static int dp(@NonNull Context context, int value) {
        return Math.round(context.getResources().getDisplayMetrics().density * value);
    }
}
