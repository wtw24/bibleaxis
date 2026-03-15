/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.search.algorithm;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BoyerMoorAlgorithmTest {

    @Test
    public void indexOfWithoutStartAndEnd() throws Exception {
        int index = new BoyerMoorAlgorithm("abbad").indexOf("abeccaabadbabbad");
        assertThat(index, equalTo(11));
    }

    @Test
    public void indexOfWithOffset() throws Exception {
        int index = new BoyerMoorAlgorithm("abbad").indexOf("abeabbadccaabadbabbad", 4);
        assertThat(index, equalTo(16));
    }

    @Test
    public void indexOfWithOffsetAndBound() throws Exception {
        int index = new BoyerMoorAlgorithm("abbad").indexOf("abeabbadccaabadbabbad", 4, 18);
        assertThat(index, equalTo(-1));
    }
}