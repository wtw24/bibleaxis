/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import de.wladimirwendland.bibleaxis.entity.modules.BibleAxisModule;
import de.wladimirwendland.bibleaxis.utils.FsUtils;
import de.wladimirwendland.bibleaxis.utils.FsUtilsWrapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BibleAxisModuleRepositoryTest {

    @Mock FsUtilsWrapper fsUtilsWrapper;
    @Mock BibleAxisModule module;

    private BibleAxisModuleRepository repository;

    @Test
    public void convertImagePathsToBase64WithAltInEnd() {
        String result1 = repository.cacheFileFromArchive(module, "test string with <img src=\"bible.png\" alt=\"bible\"> and other symbols");
        assertNotNull(result1);
        assertEquals(27921, result1.length());
    }

    @Test
    public void convertImagePathsToBase64WithAltInStart() {
        String result2 = repository.cacheFileFromArchive(module, "test string with <img alt=\"bible\" src=\"bible.png\"> and other symbols");
        assertNotNull(result2);
        assertEquals(27921, result2.length());
    }

    @Test
    public void convertImagePathsToBase64WithoutAlt() {
        String result3 = repository.cacheFileFromArchive(module, "test string with <img src='bible.png'> and other symbols");
        assertNotNull(result3);
        assertEquals(27909, result3.length());
    }

    @Test
    public void convertImagePathsToBase64WithoutAltLowerCase() {
        String result3 = repository.cacheFileFromArchive(module, "<p><IMG SRC= \"theatre_seats.jpg\"> ".toLowerCase());
        assertNotNull(result3);
        assertEquals(27879, result3.length());
    }

    @Test
    public void convertImagePathsToBase64withNonArchiveModules() {
        when(module.isArchive()).thenReturn(false);
        String testLine = "test string with <img src=\"bible.png\" alt=\"bible\"> and other symbols";
        String result = repository.cacheFileFromArchive(module, testLine);
        assertNotNull(result);
        assertEquals(testLine, result);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        repository = new BibleAxisModuleRepository(fsUtilsWrapper);
        init();
    }

    private void init() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream imageStream = classLoader.getResourceAsStream("bible.png");
        when(fsUtilsWrapper.getStreamFromZip(anyString(), anyString())).thenReturn(imageStream);
        when(fsUtilsWrapper.getBytes(any(InputStream.class))).thenReturn(FsUtils.getBytes(imageStream));
        when(module.isArchive()).thenReturn(true);
        when(module.getModulePath()).thenReturn("");
    }
}