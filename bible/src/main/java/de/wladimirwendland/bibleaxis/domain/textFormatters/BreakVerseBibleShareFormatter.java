/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.textFormatters;

import java.util.LinkedHashMap;
import java.util.Map;

public class BreakVerseBibleShareFormatter implements IShareTextFormatter {

    private LinkedHashMap<Integer, String> verses;

    public BreakVerseBibleShareFormatter(LinkedHashMap<Integer, String> verses) {
        this.verses = verses;
    }

    @Override
    public String format() {
        StringBuilder shareText = new StringBuilder();
        Integer prevVerseNumber = 0;
        for (Map.Entry<Integer, String> entry : verses.entrySet()) {
            if (prevVerseNumber == 0) {
                prevVerseNumber = entry.getKey();
            }

            if (entry.getKey() - prevVerseNumber > 1) {
                shareText.append("...\r\n");
            }
            shareText.append(String.format("%1$s %2$s", entry.getKey(), entry.getValue())).append("\r\n");
            prevVerseNumber = entry.getKey();
        }
        return shareText.toString();
    }

}
