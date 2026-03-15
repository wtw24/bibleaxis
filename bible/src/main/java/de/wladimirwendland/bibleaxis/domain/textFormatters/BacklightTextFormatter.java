/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.textFormatters;

import java.util.regex.Pattern;

public class BacklightTextFormatter implements ITextFormatter {

    private final ITextFormatter formatter;
    private final String query;
    private final String colorPattern;
    private final boolean wholeWordsMatch;

    public BacklightTextFormatter(ITextFormatter baseFormatter, String query, String color) {
        this(baseFormatter, query, color, false);
    }

    public BacklightTextFormatter(ITextFormatter baseFormatter, String query, String color, boolean wholeWordsMatch) {
        this.query = query;
        this.formatter = baseFormatter;
        this.colorPattern = "<b><font color=\"" + color + "\">$1</font></b>";
        this.wholeWordsMatch = wholeWordsMatch;
    }

    @Override
    public String format(String text) {
        String sourceText = formatter.format(text);
        if (query == null || query.trim().isEmpty()) {
            return sourceText;
        }

        String normalizedQuery = query.trim().replaceAll("\\s+", " ");
        if (wholeWordsMatch) {
            String quoted = Pattern.quote(normalizedQuery);
            Pattern regex = Pattern.compile("(?iu)(?<![\\p{L}\\p{N}_])(" + quoted + ")(?![\\p{L}\\p{N}_])");
            return regex.matcher(sourceText).replaceAll(colorPattern);
        }

        String[] words = normalizedQuery.toLowerCase().replaceAll("[^\\s\\w]", "").split("\\s+");
        StringBuilder pattern = new StringBuilder(normalizedQuery.length() + words.length);
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (pattern.length() != 0) {
                pattern.append("|");
            }
            pattern.append(Pattern.quote(word));
        }

        if (pattern.length() == 0) {
            return sourceText;
        }

        Pattern regex = Pattern.compile("((?ui)" + pattern.toString() + ")");
        return regex.matcher(sourceText).replaceAll(colorPattern);
    }
}
