/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.textFormatters;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

import java.util.ArrayList;

/**
 * @author Vladimir Yakushev
 * @version 1.0 of 11.2015
 */
public class ModuleTextFormatter implements ITextFormatter {

    private static final String VERSE_NUMBER_PATTERN = "(?m)^(<[^/]+?>)*?(\\d+)(</(.)+?>){0,1}?\\s+";

    private ArrayList<ITextFormatter> formatters = new ArrayList<>();
    private boolean visibleVerseNumbers;

    public ModuleTextFormatter(BaseModule module, PreferenceHelper prefHelper) {
        this.visibleVerseNumbers = module.isBible() || prefHelper.viewBookVerse();
        String htmlFilter = module.getHtmlFilter();
        if (module.isContainsStrong()) {
            if (prefHelper.isStrongNumbersEnabled()) {
                formatters.add(new StrongLinkTextFormatter());
                htmlFilter = htmlFilter + "|(a)|(/a)";
            } else {
                formatters.add(new NoStrongTextFormatter());
            }
        }
        formatters.add(new StripTagsTextFormatter("<(?!" + htmlFilter + ")(.)*?>"));
    }

    public ModuleTextFormatter(BaseModule module, ITextFormatter formatter) {
        this.visibleVerseNumbers = module.isBible();
        if (module.isContainsStrong()) {
            formatters.add(new NoStrongTextFormatter());
        }
        formatters.add(formatter);
    }

    public void setVisibleVerseNumbers(boolean visible) {
        this.visibleVerseNumbers = visible;
    }

    @Override
    public String format(String text) {
        for (ITextFormatter formatter : formatters) {
            text = formatter.format(text);
        }
        if (visibleVerseNumbers) {
            text = text.replaceAll(VERSE_NUMBER_PATTERN, "$1<span class=\"verseNumber\">$2</span>$3 ").replaceAll("null", "");
        } else {
            text = text.replaceAll(VERSE_NUMBER_PATTERN, "");
        }
        return text;
    }
}
