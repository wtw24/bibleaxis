/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.textFormatters;

import org.junit.Assert;
import org.junit.Test;

public class StrongLinkTextFormatterTest {

    @Test
    public void testFormatPrefixedStrongNumbers() {
        StrongLinkTextFormatter formatter = new StrongLinkTextFormatter();

        String result = formatter.format("Text G123 and H4567.");

        Assert.assertTrue(result.contains("<a class=\"strongNumber\" href=\"sG123\">G123</a>"));
        Assert.assertTrue(result.contains("<a class=\"strongNumber\" href=\"sH4567\">H4567</a>"));
    }

    @Test
    public void testFormatStrongNumberWithSpaceAfterPrefix() {
        StrongLinkTextFormatter formatter = new StrongLinkTextFormatter();

        String result = formatter.format("Text g 59 and h 7225");

        Assert.assertTrue(result.contains("<a class=\"strongNumber\" href=\"sG59\">G59</a>"));
        Assert.assertTrue(result.contains("<a class=\"strongNumber\" href=\"sH7225\">H7225</a>"));
    }

    @Test
    public void testFormatStrongNumberAdjacentToWord() {
        StrongLinkTextFormatter formatter = new StrongLinkTextFormatter();

        String result = formatter.format("НектоH376 из племениH1004");

        Assert.assertTrue(result.contains("Некто<a class=\"strongNumber\" href=\"sH376\">H376</a>"));
        Assert.assertTrue(result.contains("племени<a class=\"strongNumber\" href=\"sH1004\">H1004</a>"));
    }

    @Test
    public void testFormatUnprefixedStrongNumbers() {
        StrongLinkTextFormatter formatter = new StrongLinkTextFormatter();

        String result = formatter.format("1 Im Anfang 7225 schuf 1254");

        Assert.assertTrue(result.contains("<a class=\"strongNumber\" href=\"s7225\">7225</a>"));
        Assert.assertTrue(result.contains("<a class=\"strongNumber\" href=\"s1254\">1254</a>"));
    }
}
