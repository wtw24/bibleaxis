/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.search;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;
import de.wladimirwendland.bibleaxis.domain.repository.IModuleRepository;
import de.wladimirwendland.bibleaxis.domain.search.algorithm.BoyerMoorAlgorithm;
import de.wladimirwendland.bibleaxis.domain.search.algorithm.SearchAlgorithm;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class BookSearchProcessor<D, T extends BaseModule> {

    private static final String WORD_BOUNDARY_CHARS = "_";

    private final Map<String, String> result = new LinkedHashMap<>();
    private final IModuleRepository<T> repository;
    private final boolean wholeWordsMatch;
    private T module;
    private String bookID;
    private String query;
    private String phrase;
    private String[] words;
    private Map<String, SearchAlgorithm> algoritms = new HashMap<>();

    BookSearchProcessor(IModuleRepository<T> repository, T module, String bookID, String query, boolean wholeWordsMatch) {
        this.repository = repository;
        this.module = module;
        this.bookID = bookID;
        this.wholeWordsMatch = wholeWordsMatch;
        this.query = query == null ? "" : query.trim();
        this.phrase = this.query.replaceAll("\\s+", " ");
    }

    @NonNull
    public Map<String, String> search() throws BookNotFoundException {
        if (query == null || query.isEmpty()) {
            return result;
        }


        String bookContent = repository.getBookContent(module, bookID);

        words = phrase.split("\\s+");

        if (wholeWordsMatch) {
            BoyerMoorAlgorithm algorithm = new BoyerMoorAlgorithm(phrase);
            if (algorithm.indexOf(bookContent) == -1) {
                return result;
            }
            algoritms.put(phrase, algorithm);
        } else {
            // данная проверка позволяет сэкономить время на делении контента на главы и стихи
            for (String word : words) {
                BoyerMoorAlgorithm algorithm = new BoyerMoorAlgorithm(word);
                if (algorithm.indexOf(bookContent) == -1) {
                    return result;
                }
                algoritms.put(word, algorithm);
            }
        }

        int chapter = module.isChapterZero() ? 0 : 1;
        String chapterSign = module.getChapterSign();
        SearchAlgorithm chapterAlgorithm = new BoyerMoorAlgorithm(chapterSign);
        int indexStart = chapterAlgorithm.indexOf(bookContent);
        while (indexStart != -1) {
            int indexEnd = chapterAlgorithm.indexOf(bookContent, indexStart + chapterSign.length());
            searchInChapter(bookContent, indexStart, indexEnd, chapter);
            indexStart = indexEnd;
            chapter++;
        }

        return result;
    }

    private void searchInChapter(String bookContent, int start, int end, int chapter) {
        int verse = 1;
        String verseSign = module.getVerseSign();
        SearchAlgorithm verseAlgorithm = new BoyerMoorAlgorithm(verseSign);
        int indexStart = verseAlgorithm.indexOf(bookContent, start, end);
        while (indexStart != -1) {
            int indexEnd = verseAlgorithm.indexOf(bookContent, indexStart + verseSign.length(), end);
            searchInVerse(bookContent, indexStart, indexEnd == -1 ? end : indexEnd, chapter, verse);
            indexStart = indexEnd;
            verse++;
        }
    }

    private void searchInVerse(String bookContent, int start, int end, int chapter, int verse) {
        if (wholeWordsMatch) {
            searchInVerseWholeWords(bookContent, start, end, chapter, verse);
            return;
        }

        String moduleId = module.getID();
        int offset = start;
        for (String word : words) {
            SearchAlgorithm algorithm = algoritms.get(word);
            if (algorithm == null) {
                return;
            }

            offset = algorithm.indexOf(bookContent, offset, end);
            if (offset == -1) {
                return;
            }
            offset += word.length();
        }
        result.put(new BibleReference(moduleId, bookID, chapter, verse).getPath(),
                bookContent.substring(start, end == -1 ? bookContent.length() : end));
    }

    private void searchInVerseWholeWords(String bookContent, int start, int end, int chapter, int verse) {
        SearchAlgorithm algorithm = algoritms.get(phrase);
        if (algorithm == null) {
            algorithm = new BoyerMoorAlgorithm(phrase);
            algoritms.put(phrase, algorithm);
        }

        int verseEnd = end == -1 ? bookContent.length() : end;
        int offset = start;
        while (offset < verseEnd) {
            offset = algorithm.indexOf(bookContent, offset, verseEnd);
            if (offset == -1) {
                return;
            }

            int matchEnd = offset + phrase.length();
            if (isWholeWordMatch(bookContent, offset, matchEnd)) {
                result.put(new BibleReference(module.getID(), bookID, chapter, verse).getPath(),
                        bookContent.substring(start, verseEnd));
                return;
            }
            offset++;
        }
    }

    private boolean isWholeWordMatch(String source, int start, int end) {
        return isWordBoundary(source, start - 1) && isWordBoundary(source, end);
    }

    private boolean isWordBoundary(String source, int index) {
        if (index < 0 || index >= source.length()) {
            return true;
        }
        char ch = source.charAt(index);
        return !Character.isLetterOrDigit(ch) && WORD_BOUNDARY_CHARS.indexOf(ch) == -1;
    }

}
