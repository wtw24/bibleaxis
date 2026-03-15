/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.reader;

import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.entity.Highlight;
import de.wladimirwendland.bibleaxis.domain.textFormatters.ITextFormatter;
import de.wladimirwendland.bibleaxis.entity.TextAppearance;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseView;
import de.wladimirwendland.bibleaxis.presentation.widget.Mode;

import java.util.List;

interface ReaderView extends BaseView {

    int getCurrVerse();

    void setCurrentOrientation(boolean disableAutoRotation);

    void setKeepScreen(boolean isKeepScreen);

    void setReaderMode(Mode mode);

    void setTextAppearance(TextAppearance textAppearance);

    void setTextFormatter(ITextFormatter formatter);

    void disableActionMode();

    void onOpenChapterFailure(Throwable ex);

    void openLibraryActivity();

    void setContent(String baseUrl, Chapter chapter, int verse, boolean isBible, List<Highlight> highlights);

    void setTitle(String moduleName, String link);

    void updateActivityMode();

    void updateContent();

}
