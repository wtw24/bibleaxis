/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.search.algorithm;

class SimpleSearchAlgorithm implements SearchAlgorithm {

    private final String target;

    SimpleSearchAlgorithm(String target) {
        this.target = target;
    }

    @Override
    public int indexOf(String source) {
        return indexOf(source, 0, source.length());
    }

    @Override
    public int indexOf(String source, int fromIndex) {
        return indexOf(source, fromIndex, source.length());
    }

    @Override
    public int indexOf(String source, int fromIndex, int toIndex) {
        if (fromIndex >= source.length()) {
            return (target.length() == 0 ? source.length() : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (toIndex < 0) {
            toIndex = source.length();
        }
        if (target.length() == 0) {
            return fromIndex;
        }

        char first = Character.toLowerCase(target.charAt(0));
        int max = (toIndex - target.length());

        for (int i = fromIndex; i <= max; i++) {
                /* Look for first character. */
            if (Character.toLowerCase(source.charAt(i)) != first) {
                while (++i <= max && Character.toLowerCase(source.charAt(i)) != first) {
                }
            }

                /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + target.length() - 1;
                for (int k = 1;
                        j < end && Character.toLowerCase(source.charAt(j)) == Character.toLowerCase(target.charAt(k));
                        j++, k++) {
                }

                if (j == end) {
                        /* Found whole string. */
                    return i;
                }
            }
        }
        return -1;
    }
}
