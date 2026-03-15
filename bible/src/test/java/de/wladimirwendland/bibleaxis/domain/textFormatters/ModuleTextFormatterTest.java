/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.textFormatters;

import static org.mockito.Mockito.when;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.entity.modules.BibleAxisModule;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ModuleTextFormatterTest {

    @Mock PreferenceHelper prefHelper;

    private final String testVerses =
            "<p>12 Услышав же Иисус, что Иоанн отдан <I>под</I> <I>стражу,</I> удалился в Галилею\n" +
            "<p>13 и, оставив Назарет, пришел и поселился в Капернауме приморском, в пределах Завулоновых и Неффалимовых,";

    private final String testVersesWithStrong =
            "<p>12 Услышав же Иисус G1234 G59, что Иоанн отдан <I>под</I> <I>стражу,</I> удалился в Галилею\n" +
            "<p>13 и, оставив Назарет, пришел 1234 59 и поселился в Капернауме приморском, в пределах Завулоновых и Неффалимовых,";

    private BaseModule mModule;

    @Before
    public void testBefore() {
        MockitoAnnotations.initMocks(this);
        when(prefHelper.viewBookVerse()).thenReturn(true);

        mModule = new BibleAxisModule("base", "biblequote.ini");
        mModule.setContainsStrong(false);
        mModule.setBible(true);
    }

    @After
    public void testAfter() {
        mModule = null;
    }

    @Test
    public void testFullTagsClean() {
        ModuleTextFormatter formatter = new ModuleTextFormatter(mModule, new StripTagsTextFormatter());
        formatter.setVisibleVerseNumbers(false);

        String result = formatter.format(testVerses);
        Assert.assertFalse(result.contains("<I>"));
        Assert.assertFalse(result.contains("</I>"));
        Assert.assertFalse(result.contains("<p>"));
        Assert.assertFalse(result.contains("</p>"));
    }

    @Test
    public void testSetVisibleVerseNumbers() {
        ModuleTextFormatter formatter = new ModuleTextFormatter(mModule, prefHelper);
        formatter.setVisibleVerseNumbers(false);

        String result = formatter.format(testVerses);
        Assert.assertFalse(result.contains("12"));
        Assert.assertFalse(result.contains("13"));
    }

    @Test
    public void testFormatCleanTags() {
        ModuleTextFormatter formatter = new ModuleTextFormatter(mModule, prefHelper);

        String result = formatter.format(testVerses);
        Assert.assertTrue(result.contains("<I>"));
        Assert.assertTrue(result.contains("<p>"));
    }

    @Test
    public void testFormatModuleWithStrong() {
        mModule.setContainsStrong(true);
        ModuleTextFormatter formatter = new ModuleTextFormatter(mModule, prefHelper);

        String result = formatter.format(testVersesWithStrong);
        Assert.assertFalse(result.contains("G1234"));
        Assert.assertFalse(result.contains("G59"));
        Assert.assertFalse(result.contains("1234"));
        Assert.assertFalse(result.contains("59"));

        Pattern pattern = Pattern.compile("\\s{2,}");
        Matcher matcher = pattern.matcher(result);
        Assert.assertFalse(matcher.find());
    }

    @Test
    public void testFormatModuleWithoutStrong() {
        mModule.setContainsStrong(false);
        ModuleTextFormatter formatter = new ModuleTextFormatter(mModule, prefHelper);

        String result = formatter.format(testVersesWithStrong);
        Assert.assertTrue(result.contains("G1234"));
        Assert.assertTrue(result.contains("G59"));
        Assert.assertTrue(result.contains("1234"));
        Assert.assertTrue(result.contains("59"));
    }
}