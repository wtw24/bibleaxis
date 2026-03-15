/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FilenameUtilsTest {

    @Test
    public void getExtension() throws Exception {
        assertNull(FilenameUtils.getExtension(null));
        assertNull(FilenameUtils.getExtension(""));
        assertNull(FilenameUtils.getExtension("/b/c"));
        assertNull(FilenameUtils.getExtension("/b.rtl/c"));
        assertEquals("gif", FilenameUtils.getExtension("/b.rtl/c.gif"));
    }
}