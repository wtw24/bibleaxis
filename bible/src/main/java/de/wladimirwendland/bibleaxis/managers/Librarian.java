/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.managers;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;
import de.wladimirwendland.bibleaxis.domain.controller.IModuleController;
import de.wladimirwendland.bibleaxis.domain.controller.ITSKController;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.entity.Verse;
import de.wladimirwendland.bibleaxis.domain.exceptions.BibleAxisException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BooksDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.domain.exceptions.TskNotFoundException;
import de.wladimirwendland.bibleaxis.domain.textFormatters.BacklightTextFormatter;
import de.wladimirwendland.bibleaxis.domain.textFormatters.ModuleTextFormatter;
import de.wladimirwendland.bibleaxis.domain.textFormatters.StripTagsTextFormatter;
import de.wladimirwendland.bibleaxis.entity.ItemList;
import de.wladimirwendland.bibleaxis.managers.history.IHistoryManager;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;
import de.wladimirwendland.bibleaxis.utils.modules.LinkConverter;
import de.wladimirwendland.bibleaxis.utils.share.ShareBuilder;
import de.wladimirwendland.bibleaxis.utils.share.ShareBuilder.Destination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger;

@Singleton
public class Librarian {

    public static final String EMPTY_OBJ = "---";
    public static final String SEARCH_SCOPE_WHOLE_BIBLE = "whole_bible";
    public static final String SEARCH_SCOPE_OLD_TESTAMENT = "old_testament";
    public static final String SEARCH_SCOPE_NEW_TESTAMENT = "new_testament";
    public static final String SEARCH_SCOPE_BOOK = "book";
    public static final String SEARCH_SCOPE_SELECTED_BOOKS = "selected_books";
    public static final String SEARCH_SCOPE_BOOK_RANGE = "book_range";

    private static final Set<String> OLD_TESTAMENT_BOOK_IDS = new HashSet<>(Arrays.asList(
            "Gen", "Exod", "Lev", "Num", "Deut", "Josh", "Judg", "Ruth", "1Sam", "2Sam",
            "1Kgs", "2Kgs", "1Chr", "2Chr", "Ezra", "Neh", "Esth", "Job", "Ps", "Prov",
            "Eccl", "Song", "Isa", "Jer", "Lam", "Ezek", "Dan", "Hos", "Joel", "Amos",
            "Obad", "Jonah", "Mic", "Nah", "Hab", "Zeph", "Hag", "Zech", "Mal"
    ));

    private static final Set<String> NEW_TESTAMENT_BOOK_IDS = new HashSet<>(Arrays.asList(
            "Matt", "Mark", "Luke", "John", "Acts", "Rom", "1Cor", "2Cor", "Gal", "Eph",
            "Phil", "Col", "1Thess", "2Thess", "1Tim", "2Tim", "Titus", "Phlm", "Heb", "Jas",
            "1Pet", "2Pet", "1John", "2John", "3John", "Jude", "Rev"
    ));

    private static final Pattern LEADING_NUMBER_PATTERN = Pattern.compile("^(\\d+)");

    private Book currBook;
    private Chapter currChapter;
    private Integer currChapterNumber = -1;
    private BaseModule currModule;
    private Integer currVerseNumber = 1;
    private IHistoryManager historyManager;
    private ILibraryController libCtrl;
    private PreferenceHelper preferenceHelper;
    private Map<String, String> searchResults = new LinkedHashMap<>();
    private ITSKController tskCtrl;

    /**
     * Инициализация контроллеров библиотеки, модулей, книг и глав.
     * Подписка на событие ChangeBooksEvent
     */
    @Inject
    public Librarian(ILibraryController libCtrl, ITSKController tskCtrl, IHistoryManager historyManager,
            PreferenceHelper preferenceHelper) {
        this.libCtrl = libCtrl;
        this.tskCtrl = tskCtrl;
        this.historyManager = historyManager;
        this.preferenceHelper = preferenceHelper;
    }

    public String getBaseUrl() {
        if (currModule == null) {
            return "file:///url_initial_load";
        }
        String dataSourceID = currModule.getDataSourceID();
        int pos = dataSourceID.lastIndexOf('/');
        if (++pos <= dataSourceID.length()) {
            return dataSourceID.substring(0, pos);
        } else {
            return dataSourceID;
        }
    }

    public ArrayList<String> getCleanedVersesText() {
        ArrayList<String> result = new ArrayList<>();

        if (currModule == null || currChapter == null) {
            return result;
        }

        ModuleTextFormatter formatter = new ModuleTextFormatter(currModule, new StripTagsTextFormatter());
        formatter.setVisibleVerseNumbers(false);

        ArrayList<Verse> verses = currChapter.getVerseList();
        for (Verse verse : verses) {
            result.add(formatter.format(verse.getText()));
        }
        return result;
    }

    public int getDisplayedVerseNumber(int verseIndex) {
        if (currModule == null || currChapter == null) {
            return verseIndex;
        }

        ArrayList<Verse> verses = currChapter.getVerseList();
        int internalIndex = verseIndex - 1;
        if (internalIndex < 0 || internalIndex >= verses.size()) {
            return verseIndex;
        }

        String rawVerseText = verses.get(internalIndex).getText();
        int parsedFromSign = getVerseNumberFromSign(rawVerseText, currModule.getVerseSign());
        if (parsedFromSign > 0) {
            return parsedFromSign;
        }

        String plainText = new StripTagsTextFormatter().format(rawVerseText).trim();
        int parsedFromPlain = getLeadingNumber(plainText);
        if (parsedFromPlain > 0) {
            return parsedFromPlain;
        }

        return verseIndex;
    }

    private int getVerseNumberFromSign(String rawVerseText, String verseSign) {
        if (rawVerseText == null || verseSign == null || verseSign.isEmpty()) {
            return -1;
        }

        int signPos = rawVerseText.indexOf(verseSign);
        if (signPos < 0) {
            return -1;
        }

        String afterSign = rawVerseText.substring(signPos + verseSign.length());
        String cleanAfterSign = new StripTagsTextFormatter().format(afterSign).trim();
        return getLeadingNumber(cleanAfterSign);
    }

    private int getLeadingNumber(String value) {
        if (value == null || value.isEmpty()) {
            return -1;
        }
        java.util.regex.Matcher matcher = LEADING_NUMBER_PATTERN.matcher(value);
        if (!matcher.find()) {
            return -1;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public BaseModule getCurrModule() {
        return currModule;
    }

    public ArrayList<ItemList> getCurrentModuleBooksList() throws OpenModuleException, BooksDefinitionException, BookDefinitionException {
        return getBookItemLists(currModule);
    }

    public BibleReference getCurrentOSISLink() {
        return new BibleReference(currModule, currBook, currChapterNumber, currVerseNumber);
    }

    public LinkedList<ItemList> getHistoryList() {
        return historyManager.getLinks();
    }

    public String getHumanBookLink() {
        if (currBook == null || currChapter == null) {
            return "";
        }
        return currBook.getName() + " " + currChapter.getNumber();
    }

    public String getModuleFullName() {
        if (currModule == null) {
            return "";
        }
        return currModule.getName();
    }

    public String getModuleID() {
        if (currModule == null) {
            return "";
        } else {
            return currModule.getID();
        }
    }

    /**
     * Возвращает список доступных модулей с Библиями, апокрифами, книгами
     *
     * @return возвращает ArrayList, содержащий модули с книгами Библии и апокрифами
     */
    public ArrayList<ItemList> getModulesList() {
        // Сначала отсортируем список по наименованием модулей
        TreeMap<String, BaseModule> tMap = new TreeMap<>();
        final Collection<BaseModule> baseModules = libCtrl.getModules().values();
        for (BaseModule currModule : baseModules) {
            tMap.put(currModule.getName(), currModule);
        }

        // Теперь создадим результирующий список на основе отсортированных данных
        ArrayList<ItemList> moduleList = new ArrayList<>();
        for (BaseModule currModule : tMap.values()) {
            moduleList.add(new ItemList(currModule.getID(), currModule.getName()));
        }

        return moduleList;
    }

    public ArrayList<ItemList> getBibleModulesList() {
        ArrayList<ItemList> moduleList = new ArrayList<>();
        TreeMap<String, BaseModule> sortedModules = new TreeMap<>();
        final Collection<BaseModule> baseModules = libCtrl.getModules().values();
        for (BaseModule module : baseModules) {
            if (module != null && module.isBible()) {
                sortedModules.put(module.getName(), module);
            }
        }

        for (BaseModule module : sortedModules.values()) {
            moduleList.add(new ItemList(module.getID(), module.getName()));
        }
        return moduleList;
    }

    public Map<String, String> getSearchResults() {
        return this.searchResults;
    }

    public Locale getTextLocale() {
        return currModule == null
                ? new Locale(BaseModule.DEFAULT_LANGUAGE)
                : new Locale(currModule.getLanguage());
    }

    public void setCurrentVerseNumber(int verse) {
        if (currModule != null && currBook != null && currChapter != null) {
            this.currVerseNumber = verse;
            BibleReference reference = new BibleReference(currModule, currBook, currChapter.getNumber(), currVerseNumber);
            preferenceHelper.saveString("last_read", reference.getExtendedPath());
            StaticLogger.info(this, "Store last read " + reference);
        }
    }

    public void clearHistory() {
        historyManager.clearLinks();
    }

    public void deleteHistoryItem(ItemList item) {
        historyManager.deleteLink(item);
    }

    public Book getBookByID(BaseModule module, String bookID) throws BookNotFoundException, OpenModuleException {
        IModuleController modCtrl = getModuleController(module);
        return modCtrl.getBookByID(bookID);
    }

    public String getBookFullName(String moduleID, String bookID) throws OpenModuleException {
        try {
            BaseModule module = libCtrl.getModuleByID(moduleID);
            IModuleController modCtrl = getModuleController(module);
            Book book = modCtrl.getBookByID(bookID);
            return book.getName();
        } catch (OpenModuleException e) {
            StaticLogger.error(this, e.getMessage());
        } catch (BookNotFoundException e) {
            StaticLogger.error(this, e.getMessage());
        }
        return EMPTY_OBJ;
    }

    public String getBookShortName(String moduleID, String bookID) {
        try {
            BaseModule module = libCtrl.getModuleByID(moduleID);
            IModuleController modCtrl = getModuleController(module);
            Book book = modCtrl.getBookByID(bookID);
            return book.getShortName();
        } catch (BookNotFoundException e) {
            StaticLogger.error(this, e.getMessage());
        } catch (OpenModuleException e) {
            StaticLogger.error(this, e.getMessage());
        }
        return EMPTY_OBJ;
    }

    /**
     * Возвращает список глав книги
     *
     * @throws OpenModuleException   указанные модуль не найден или произошла ошибка при его открытии
     * @throws BookNotFoundException указанная книга в модуле не найдена
     */
    public List<String> getChaptersList(String moduleID, String bookID)
            throws BookNotFoundException, OpenModuleException {
        // Получим модуль по его ID
        BaseModule module = libCtrl.getModuleByID(moduleID);
        IModuleController modCtrl = getModuleController(module);
        return modCtrl.getChapterNumbers(bookID);
    }

    public LinkedHashMap<String, BibleReference> getCrossReference(BibleReference bReference)
            throws TskNotFoundException, BibleAxisException {

        LinkedHashMap<String, BibleReference> result = new LinkedHashMap<>();
        for (BibleReference reference : tskCtrl.getLinks(bReference)) {
            Book book;
            try {
                book = getBookByID(currModule, reference.getBookID());
            } catch (OpenModuleException e) {
                StaticLogger.error(this, String.format("Error open module %1$s for link %2$s",
                        reference.getModuleID(), reference.getBookID()));
                continue;
            } catch (BookNotFoundException e) {
                StaticLogger.error(this, String.format("Not found book %1$s in module %2$s",
                        reference.getBookID(), reference.getModuleID()));
                continue;
            }
            BibleReference newReference = new BibleReference(currModule, book,
                    reference.getChapter(), reference.getFromVerse(), reference.getToVerse());
            result.put(
                    LinkConverter.getOSIStoHuman(newReference),
                    newReference);
        }

        return result;
    }

    public HashMap<BibleReference, String> getCrossReferenceContent(Collection<BibleReference> bReferences) {
        ModuleTextFormatter formatter = new ModuleTextFormatter(currModule, new StripTagsTextFormatter());
        formatter.setVisibleVerseNumbers(false);

        HashMap<BibleReference, String> crossReferenceContent = new HashMap<>();
        for (BibleReference ref : bReferences) {
            try {
                int fromVerse = ref.getFromVerse();
                int toVerse = ref.getToVerse();
                Chapter chapter = getChapterByNumber(getBookByID(currModule, ref.getBookID()), ref.getChapter());
                crossReferenceContent.put(ref, chapter.getText(fromVerse, toVerse, formatter));
            } catch (Exception e) {
                StaticLogger.error(this, e.getMessage());
            }
        }
        return crossReferenceContent;
    }


    ///////////////////////////////////////////////////////////////////////////
    // SHARE

    public ArrayList<ItemList> getModuleBooksList(String moduleID) throws OpenModuleException, BooksDefinitionException, BookDefinitionException {
        BaseModule module = libCtrl.getModuleByID(moduleID);
        return getBookItemLists(module);
    }

    public Bitmap getModuleImage(String path) {
        if (currModule == null) {
            return null;
        }

        IModuleController modCtrl = getModuleController(currModule);
        return modCtrl.getBitmap(path);
    }

    public Boolean isOSISLinkValid(BibleReference link) {
        if (link.getPath() == null) {
            return false;
        }

        try {
            libCtrl.getModuleByID(link.getModuleID());
        } catch (OpenModuleException e) {
            return false;
        }
        return true;
    }

    public void nextChapter() throws OpenModuleException {
        if (currModule == null || currBook == null) {
            return;
        }

        if (currBook.getChapterQty() > (currChapterNumber + (currModule.isChapterZero() ? 1 : 0))) {
            currChapterNumber++;
            currVerseNumber = 1;
        } else {
            IModuleController modCtrl = getModuleController(currModule);
            try {
                Book nextBook = modCtrl.getNextBook(currBook.getID());
                if (nextBook != null) {
                    currBook = nextBook;
                    currChapter = null;
                    currChapterNumber = currBook.getFirstChapterNumber();
                    currVerseNumber = 1;
                }
            } catch (BookNotFoundException e) {
                StaticLogger.error(this, e.getMessage());
            }
        }
    }

    public Chapter openChapter(BibleReference link) throws BookNotFoundException, OpenModuleException {
        StaticLogger.info(this, "Open link " + link.getPath());
        currModule = libCtrl.getModuleByID(link.getModuleID());
        IModuleController modCtrl = getModuleController(currModule);
        currBook = modCtrl.getBookByID(link.getBookID());
        currChapter = modCtrl.getChapter(link.getBookID(), link.getChapter());
        currChapterNumber = link.getChapter();
        currVerseNumber = link.getFromVerse();

        final BibleReference reference = new BibleReference(currModule, currBook, currChapterNumber, currVerseNumber);
        historyManager.addLink(reference);
        preferenceHelper.saveString("last_read", reference.getExtendedPath());

        return currChapter;
    }

    public void prevChapter() throws OpenModuleException {
        if (currModule == null || currBook == null) {
            return;
        }

        if (!currChapterNumber.equals(currBook.getFirstChapterNumber())) {
            currChapterNumber -= 1;
            currVerseNumber = 1;
        } else {
            try {
                IModuleController modCtrl = getModuleController(currModule);
                Book nextBook = modCtrl.getPrevBook(currBook.getID());
                if (nextBook != null) {
                    currBook = nextBook;
                    currChapter = null;
                    currChapterNumber = currBook.getLastChapterNumber();
                    currVerseNumber = 1;
                }
            } catch (BookNotFoundException e) {
                StaticLogger.error(this, e.getMessage());
            }
        }
    }

    public Map<String, String> search(String query, String fromBook, String toBook) throws OpenModuleException, BookNotFoundException {
        if (currModule == null) {
            searchResults = new LinkedHashMap<>();
            return searchResults;
        }
        ArrayList<String> modules = new ArrayList<>();
        modules.add(currModule.getID());
        search(query, modules, SEARCH_SCOPE_BOOK_RANGE, null, fromBook, toBook, false);
        return searchResults;
    }

    public Map<String, String> search(
            String query,
            List<String> moduleIDs,
            String searchScope,
            List<String> selectedBookIDs,
            String fromBook,
            String toBook,
            boolean wholeWordsMatch) {
        searchResults = new LinkedHashMap<>();
        if (query == null || query.trim().isEmpty() || moduleIDs == null || moduleIDs.isEmpty()) {
            return searchResults;
        }

        Set<String> selectedBooks = new HashSet<>();
        if (selectedBookIDs != null) {
            selectedBooks.addAll(selectedBookIDs);
        }

        for (String moduleID : moduleIDs) {
            try {
                BaseModule module = libCtrl.getModuleByID(moduleID);
                if (module == null || !module.isBible()) {
                    continue;
                }

                List<String> moduleBookList = getSearchBooks(module, searchScope, selectedBooks, fromBook, toBook);
                if (moduleBookList.isEmpty()) {
                    continue;
                }

                StaticLogger.info(this, String.format("Search '%s' in %s (%d books)", query, module.getID(), moduleBookList.size()));

                IModuleController moduleCtrl = getModuleController(module);
                Map<String, String> moduleResults = moduleCtrl.search(moduleBookList, query, wholeWordsMatch);

                ModuleTextFormatter formatter = new ModuleTextFormatter(module, new StripTagsTextFormatter());
                formatter.setVisibleVerseNumbers(false);
                BacklightTextFormatter textFormatter = new BacklightTextFormatter(formatter, query, "#6b0b0b", wholeWordsMatch);
                for (Map.Entry<String, String> entry : moduleResults.entrySet()) {
                    searchResults.put(entry.getKey(), textFormatter.format(entry.getValue()));
                }
            } catch (OpenModuleException e) {
                StaticLogger.error(this, e.getMessage());
            }
        }
        return searchResults;
    }

    @NonNull
    private List<String> getSearchBooks(
            @NonNull BaseModule module,
            String searchScope,
            @NonNull Set<String> selectedBooks,
            String fromBook,
            String toBook) {
        List<String> allBooks = new ArrayList<>(module.getBooks().keySet());
        if (allBooks.isEmpty()) {
            return allBooks;
        }

        if (SEARCH_SCOPE_OLD_TESTAMENT.equals(searchScope)) {
            ArrayList<String> result = new ArrayList<>();
            for (String bookID : allBooks) {
                if (OLD_TESTAMENT_BOOK_IDS.contains(bookID)) {
                    result.add(bookID);
                }
            }
            return result;
        }

        if (SEARCH_SCOPE_NEW_TESTAMENT.equals(searchScope)) {
            ArrayList<String> result = new ArrayList<>();
            for (String bookID : allBooks) {
                if (NEW_TESTAMENT_BOOK_IDS.contains(bookID)) {
                    result.add(bookID);
                }
            }
            return result;
        }

        if (SEARCH_SCOPE_BOOK.equals(searchScope)) {
            if (selectedBooks.isEmpty()) {
                return new ArrayList<>();
            }
            ArrayList<String> result = new ArrayList<>();
            for (String bookID : allBooks) {
                if (selectedBooks.contains(bookID)) {
                    result.add(bookID);
                    break;
                }
            }
            return result;
        }

        if (SEARCH_SCOPE_SELECTED_BOOKS.equals(searchScope)) {
            if (selectedBooks.isEmpty()) {
                return new ArrayList<>();
            }
            ArrayList<String> result = new ArrayList<>();
            for (String bookID : allBooks) {
                if (selectedBooks.contains(bookID)) {
                    result.add(bookID);
                }
            }
            return result;
        }

        if (SEARCH_SCOPE_BOOK_RANGE.equals(searchScope)) {
            int fromIndex = allBooks.indexOf(fromBook);
            int toIndex = allBooks.indexOf(toBook);

            if (fromIndex == -1 && toIndex == -1) {
                return allBooks;
            }
            if (fromIndex == -1) {
                return new ArrayList<>(allBooks.subList(0, toIndex + 1));
            }
            if (toIndex == -1) {
                return new ArrayList<>(allBooks.subList(fromIndex, allBooks.size()));
            }

            if (fromIndex > toIndex) {
                int tmp = fromIndex;
                fromIndex = toIndex;
                toIndex = tmp;
            }
            return new ArrayList<>(allBooks.subList(fromIndex, toIndex + 1));
        }

        return allBooks;
    }

    public void shareText(Context context, TreeSet<Integer> selectVerses, Destination dest) {
        if (getCurrChapter() == null) {
            return;
        }

        ModuleTextFormatter formatter = new ModuleTextFormatter(currModule, new StripTagsTextFormatter());
        formatter.setVisibleVerseNumbers(false);

        LinkedHashMap<Integer, String> verses = getCurrChapter().getVerses(selectVerses);
        for (Map.Entry<Integer, String> entry : verses.entrySet()) {
            verses.put(entry.getKey(), formatter.format(entry.getValue()));
        }

        ShareBuilder builder = new ShareBuilder(context, currModule, currBook, currChapter, verses);
        builder.share(dest);
    }

    @NonNull
    private ArrayList<ItemList> getBookItemLists(BaseModule module) {
        ArrayList<ItemList> result = new ArrayList<>();
        if (module == null) {
            return result;
        }

        IModuleController modCtrl = getModuleController(module);
        for (Book book : modCtrl.getBooks()) {
            result.add(new ItemList(book.getID(), book.getName()));
        }
        return result;
    }

    private Chapter getChapterByNumber(Book book, Integer chapterNumber) throws BookNotFoundException {
        IModuleController modCtrl = getModuleController(currModule);
        return modCtrl.getChapter(book.getID(), chapterNumber);
    }

    private Chapter getCurrChapter() {
        return currChapter;
    }

    private IModuleController getModuleController(BaseModule module) {
        return Injector.getModuleController(module);
    }
}
