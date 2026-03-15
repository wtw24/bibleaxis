/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.analytics;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.AnalyticsHelper;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.entity.Bookmark;

/**
 * Отправка аналитики в Google Firebase
 *
 * @author Vladimir Yackushev <Yakushev.V.V@sberbank.ru>
 * @since 07/01/2019
 */
public class FirebaseAnalyticsHelper implements AnalyticsHelper {

    public FirebaseAnalyticsHelper() {
    }

    @Override
    public void moduleEvent(@NonNull BibleReference link) {
        // Analytics disabled for this fork.
    }

    @Override
    public void bookmarkEvent(@NonNull Bookmark bookmark) {
        // Analytics disabled for this fork.
    }

    @Override
    public void clickEvent(@NonNull String action) {
        // Analytics disabled for this fork.
    }

    @Override
    public void searchEvent(@NonNull String query, String module) {
        // Analytics disabled for this fork.
    }
}
