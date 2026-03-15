/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yakushev Vladimir, Sergey Ursul
 */
public abstract class BaseModule implements Serializable {

    public static final String DEFAULT_LANGUAGE = "ru";

    private static final long serialVersionUID = -499369158022814559L;

    private boolean chapterZero;
    private String shortName = "";
    private String chapterSign = "";
    private String verseSign = "";
    private String htmlFilter = "";
    private boolean containsStrong;
    private boolean isBible;
    private String defaultEncoding = "utf-8";
    private String language = "ru_RU";
    private String fontName = "";
    private String fontPath = "";

    private Map<String, Book> books = new LinkedHashMap<>();    // to lazy loading on demand
    private String Name = "";

    public Map<String, Book> getBooks() {
        return books;
    }

    public String getChapterSign() {
        return chapterSign;
    }

    public abstract String getDataSourceID();

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    public String getFontName() {
        return fontName;
    }

    public String getFontPath() {
        return fontPath;
    }

    public String getHtmlFilter() {
        return htmlFilter;
    }

    public abstract String getID();

    public String getLanguage() {
        if (language == null || !language.contains("-")) {
            return DEFAULT_LANGUAGE;
        } else {
            return language.substring(0, language.indexOf("-")).toLowerCase();
        }
    }

    public String getName() {
        return Name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getVerseSign() {
        return verseSign;
    }

    public boolean isBible() {
        return isBible;
    }

    public boolean isChapterZero() {
        return chapterZero;
    }

    public boolean isContainsStrong() {
        return containsStrong;
    }

    public void setBible(boolean bible) {
        isBible = bible;
    }

    public void setBooks(Map<String, Book> books) {
        this.books = books;
    }

    public void setChapterSign(String chapterSign) {
        this.chapterSign = chapterSign;
    }

    public void setChapterZero(boolean chapterZero) {
        this.chapterZero = chapterZero;
    }

    public void setContainsStrong(boolean containsStrong) {
        this.containsStrong = containsStrong;
    }

    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    public void setHtmlFilter(String htmlFilter) {
        this.htmlFilter = htmlFilter;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setVerseSign(String verseSign) {
        this.verseSign = verseSign;
    }

    public Book getBook(String bookID) {
        return books.get(bookID);
    }

    public List<String> getBookList(String fromBookID, String toBookID) {
        ArrayList<String> result = new ArrayList<>();
        boolean startSearch = false;
        for (String bookID : books.keySet()) {
            if (!startSearch) {
                startSearch = bookID.equals(fromBookID);
                if (!startSearch) continue;
            }
            result.add(bookID);
            if (bookID.equals(toBookID)) break;
        }
        return result;

    }

    @Override
    public String toString() {
        return this.Name;
    }
}