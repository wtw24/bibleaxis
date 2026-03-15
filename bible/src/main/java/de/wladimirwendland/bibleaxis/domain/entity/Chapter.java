/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.entity;

import de.wladimirwendland.bibleaxis.domain.textFormatters.ITextFormatter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Chapter {

    private Integer number;
    private String text;
    private TreeMap<Integer, Verse> verses = new TreeMap<>();

    public Chapter(Integer number, ArrayList<Verse> verseList) {
        this.number = number;
        Integer verseNumber = 1;
        for (Verse verse : verseList) {
            verses.put(verseNumber++, verse);
        }
    }

    public Integer getNumber() {
        return number;
    }

    public String getText() {
        if (text == null && !verses.isEmpty()) {
            StringBuilder buffer = new StringBuilder();
            for (Map.Entry<Integer, Verse> entry : verses.entrySet()) {
                buffer.append(entry.getValue().getText());
            }
            text = buffer.toString();
        }
        return text;
    }

    public ArrayList<Verse> getVerseList() {
        return new ArrayList<>(verses.values());
    }

    public String getText(int fromVerse, int toVerse, ITextFormatter formatter) {
        StringBuilder buffer = new StringBuilder();
        for (int verseNumber = fromVerse; verseNumber <= toVerse; verseNumber++) {
            Verse ver = verses.get(verseNumber);
            if (ver != null) {
                buffer.append(formatter.format(ver.getText()));
            }
        }
        return buffer.toString();
    }

    public LinkedHashMap<Integer, String> getVerses(TreeSet<Integer> verses) {
        LinkedHashMap<Integer, String> result = new LinkedHashMap<>();
        ArrayList<Verse> versesList = getVerseList();
        int verseListSize = versesList.size();
        for (Integer verse : verses) {
            int verseIndex = verse - 1;
            if (verseIndex > verseListSize) {
                break;
            }
            result.put(verse, versesList.get(verseIndex).getText());
        }

        return result;
    }
}
