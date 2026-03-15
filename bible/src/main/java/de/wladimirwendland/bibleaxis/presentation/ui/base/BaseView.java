/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.base;

import io.reactivex.Scheduler;

public interface BaseView {

    Scheduler backgroundThread();
    Scheduler mainThread();
    void showProgress(boolean cancelable);
    void hideProgress();
    void showToast(int resource);
}
