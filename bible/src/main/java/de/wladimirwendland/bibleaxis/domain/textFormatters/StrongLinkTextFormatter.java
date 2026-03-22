/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.textFormatters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrongLinkTextFormatter implements ITextFormatter {

    private static final Pattern STRONG_PREFIXED_PATTERN = Pattern.compile(
            "(^|[^\\p{L}\\p{Nd}_])([GHgh])\\s*(\\d{1,5})(?=$|[^\\p{L}\\p{Nd}_])"
                    + "|([\\p{L}\\p{M}])([GH])(\\d{1,5})(?=$|[^\\p{L}\\p{Nd}_])"
    );

    private static final Pattern STRONG_UNPREFIXED_PATTERN = Pattern.compile(
            "(?<=[\\p{L}\\p{M}\\]\\)\\.,;:!?])\\s+(\\d{1,5})(?=$|[^\\p{L}\\p{Nd}_])"
    );

    @Override
    public String format(String text) {
        String withPrefixed = formatPrefixed(text);
        return formatUnprefixed(withPrefixed);
    }

    private String formatPrefixed(String text) {
        Matcher matcher = STRONG_PREFIXED_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String replacement;
            if (matcher.group(2) != null) {
                String delimiter = matcher.group(1);
                String strongCode = matcher.group(2).toUpperCase() + matcher.group(3);
                replacement = delimiter + buildStrongLink(strongCode);
            } else {
                String before = matcher.group(4);
                String strongCode = matcher.group(5).toUpperCase() + matcher.group(6);
                replacement = before + buildStrongLink(strongCode);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String formatUnprefixed(String text) {
        Matcher matcher = STRONG_UNPREFIXED_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String number = matcher.group(1);
            String replacement = " " + buildStrongLink(number);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String buildStrongLink(String strongCode) {
        return "<a class=\"strongNumber\" href=\"s" + strongCode + "\">" + strongCode + "</a>";
    }
}
