/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.search.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Алгоритм поиска вхождения строки Бойера-Мура без учета индекса символов
 */
public final class BoyerMoorAlgorithm implements SearchAlgorithm {

    private Map<Character, Integer> charTable;
    private int[] offsetTable;
    private final String target;

    public BoyerMoorAlgorithm(String target) {
        this.target = target;
        this.charTable = makeCharTable(target);
        this.offsetTable = makeOffsetTable(target);
    }

    @Override
    public int indexOf(String source) {
        return indexOf(source, 0);
    }

    @Override
    public int indexOf(String source, int fromIndex) {
        return indexOf(source, fromIndex, source.length());
    }

    @Override
    public int indexOf(String source, int fromIndex, int toIndex) {
        if (target.length() == 0) {
            return 0;
        }

        if (toIndex < 0) {
            toIndex = source.length();
        }

        for (int i = fromIndex + target.length() - 1, j; i < toIndex; ) {
            for (j = target.length() - 1;
                    Character.toLowerCase(target.charAt(j)) == Character.toLowerCase(source.charAt(i));
                    --i, --j) {
                if (j == 0) {
                    return i;
                }
            }

            // i += target.length - j; // For naive method
            Integer offset = charTable.get(Character.toLowerCase(source.charAt(i)));
            i += Math.max(
                    offsetTable[target.length() - 1 - j],
                    offset == null ? target.length() : offset
            );
        }
        return -1;
    }

    /**
     * Makes the jump table based on the mismatched character information
     **/
    private Map<Character, Integer> makeCharTable(String pattern) {
        Map<Character, Integer> result = new HashMap<>(pattern.length());
        for (int i = 0; i < pattern.length() - 1; ++i) {
            result.put(Character.toLowerCase(pattern.charAt(i)), pattern.length() - 1 - i);
        }
        return result;
    }

    /**
     * Makes the jump table based on the scan offset which mismatch occurs.
     **/
    private int[] makeOffsetTable(String pattern) {
        int[] table = new int[pattern.length()];
        int lastPrefixPosition = pattern.length();
        for (int i = pattern.length() - 1; i >= 0; --i) {
            if (isPrefix(pattern, i + 1)) {
                lastPrefixPosition = i + 1;
            }
            table[pattern.length() - 1 - i] = lastPrefixPosition - i + pattern.length() - 1;
        }
        for (int i = 0; i < pattern.length() - 1; ++i) {
            int slen = suffixLength(pattern, i);
            table[slen] = pattern.length() - 1 - i + slen;
        }
        return table;
    }

    /**
     * function to check if needle[p:end] a prefix of pattern
     **/
    private boolean isPrefix(String pattern, int p) {
        for (int i = p, j = 0; i < pattern.length(); ++i, ++j) {
            if (Character.toLowerCase(pattern.charAt(i)) != Character.toLowerCase(pattern.charAt(j))) {
                return false;
            }
        }
        return true;
    }

    /**
     * function to returns the maximum length of the substring ends at p and is a suffix
     **/
    private int suffixLength(String pattern, int p) {
        int len = 0;
        for (int i = p, j = pattern.length() - 1;
                i >= 0 && Character.toLowerCase(pattern.charAt(i)) == Character.toLowerCase(pattern.charAt(j));
                --i, --j) {
            len += 1;
        }
        return len;
    }
}
