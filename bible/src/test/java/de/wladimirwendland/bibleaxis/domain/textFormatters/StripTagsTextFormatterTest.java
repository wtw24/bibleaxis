/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.textFormatters;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StripTagsTextFormatterTest {

    private static final String testVerses =
            "<p>12 Услышав же Иисус, что Иоанн отдан <I>под</I> <I>стражу,</I> удалился в Галилею\n" +
            "<p>13 и, оставив Назарет, пришел и поселился в Капернауме приморском, в пределах Завулоновых и Неффалимовых,";

    @Test
    public void testFormat() throws Exception {
        ITextFormatter formatter = new StripTagsTextFormatter();

        String result = formatter.format(testVerses);
        Assert.assertFalse(result.contains("<I>"));
        Assert.assertFalse(result.contains("</I>"));
        Assert.assertFalse(result.contains("<p>"));
        Assert.assertFalse(result.contains("</p>"));
    }
}