/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.search;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.async.task.command.AsyncCommand;
import de.wladimirwendland.bibleaxis.async.task.command.StartSearch;
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent;
import de.wladimirwendland.bibleaxis.domain.AnalyticsHelper;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;
import de.wladimirwendland.bibleaxis.domain.exceptions.BooksDefinitionException;
import de.wladimirwendland.bibleaxis.domain.exceptions.ExceptionHelper;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.entity.ItemList;
import de.wladimirwendland.bibleaxis.managers.Librarian;
import de.wladimirwendland.bibleaxis.presentation.ui.base.AsyncTaskActivity;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.ItemAdapter;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.Item;
import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.SubtextItem;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;
import de.wladimirwendland.bibleaxis.utils.Task;
import de.wladimirwendland.bibleaxis.utils.modules.LinkConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class SearchActivity extends AsyncTaskActivity implements TextView.OnEditorActionListener {

    private static final String TAG = SearchActivity.class.getSimpleName();

    private static final int SCOPE_WHOLE_BIBLE = 0;
    private static final int SCOPE_OLD_TESTAMENT = 1;
    private static final int SCOPE_NEW_TESTAMENT = 2;
    private static final int SCOPE_BOOK = 3;
    private static final int SCOPE_SELECTED_BOOKS = 4;
    private static final int SCOPE_BOOK_RANGE = 5;

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

    private static final String KEY_FROM_BOOK = "fromBook";
    private static final String KEY_TO_BOOK = "toBook";
    private static final String KEY_MODULE_IDS = "searchModuleIDs";
    private static final String KEY_SCOPE = "searchScope";
    private static final String KEY_SELECTED_BOOK_IDS = "searchBookIDs";
    private static final String KEY_SINGLE_BOOK_ID = "searchSingleBookID";
    private static final String KEY_MATCH_WHOLE_WORDS = "searchWholeWordsMatch";
    private static final String KEY_SEARCH_SIGNATURE = "searchSignature";
    private static final String KEY_SEARCH_POSITION = "changeSearchPosition";

    private final PreferenceHelper preferenceHelper = BibleAxisApp.getInstance().getPrefHelper();

    @BindView(R.id.search_list) ListView resultList;
    @BindView(R.id.search_results_header) TextView searchResultsHeader;
    @BindView(R.id.search_settings_container) LinearLayout searchSettingsContainer;
    @BindView(R.id.search_text) EditText searchText;
    @BindView(R.id.search_modules_button) Button modulesButton;
    @BindView(R.id.search_scope) Spinner searchScope;
    @BindView(R.id.search_whole_words_match) CheckBox wholeWordsMatch;
    @BindView(R.id.search_single_book) Spinner singleBook;
    @BindView(R.id.search_selected_books_button) Button selectedBooksButton;
    @BindView(R.id.search_range_container) LinearLayout rangeContainer;
    @BindView(R.id.from_book) Spinner spinnerFrom;
    @BindView(R.id.to_book) Spinner spinnerTo;

    @Inject AnalyticsHelper analyticsHelper;

    private Librarian myLibrarian;
    private String progressMessage = "";
    private final ArrayList<Item> searchItems = new ArrayList<>();
    private final ArrayList<ItemList> bibleModules = new ArrayList<>();
    private final ArrayList<String> selectedModuleIds = new ArrayList<>();
    private final ArrayList<String> selectedBookIds = new ArrayList<>();
    private ArrayList<ItemList> booksForSearch = new ArrayList<>();
    private Map<String, String> searchResults = new LinkedHashMap<>();
    private int currentScope = SCOPE_WHOLE_BIBLE;
    private boolean isSearchSettingsVisible = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        myLibrarian = BibleAxisApp.getInstance().getLibrarian();
        progressMessage = getResources().getString(R.string.messageSearch);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        searchText.setOnEditorActionListener(this);
        wholeWordsMatch.setChecked(preferenceHelper.getBoolean(KEY_MATCH_WHOLE_WORDS));
        wholeWordsMatch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceHelper.saveBoolean(KEY_MATCH_WHOLE_WORDS, isChecked);
            setAdapter();
        });

        loadBibleModules();
        restoreSelectedModules();
        updateModulesButtonText();

        if (bibleModules.isEmpty()) {
            Toast.makeText(this, R.string.search_no_bible_modules, Toast.LENGTH_LONG).show();
            modulesButton.setEnabled(false);
            searchScope.setEnabled(false);
            searchText.setEnabled(false);
            wholeWordsMatch.setEnabled(false);
            selectedBooksButton.setEnabled(false);
            singleBook.setEnabled(false);
            spinnerFrom.setEnabled(false);
            spinnerTo.setEnabled(false);
        }

        spinnerInit();
        initScopeSpinner();

        if (buildSearchSignature().equals(preferenceHelper.getString(KEY_SEARCH_SIGNATURE))) {
            searchResults = new LinkedHashMap<>(myLibrarian.getSearchResults());
        }

        if (searchResults.isEmpty()) {
            searchText.requestFocus();
        }

        setAdapter();
    }

    @Override
    protected void inject(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        spinnerInit();
    }

    @Override
    public void onTaskComplete(Task task) {
        if (task.isCancelled()) {
            Toast.makeText(this, R.string.messageSearchCanceled, Toast.LENGTH_LONG).show();
        } else {
            searchResults = new LinkedHashMap<>(myLibrarian.getSearchResults());
            preferenceHelper.saveString(KEY_SEARCH_SIGNATURE, buildSearchSignature());
            setAdapter();
        }
        preferenceHelper.saveInt(KEY_SEARCH_POSITION, 0);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, @Nullable KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
            startSearch();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_bar_search_toggle:
                setSearchSettingsVisible(true);
                searchResults.clear();
                preferenceHelper.saveString(KEY_SEARCH_SIGNATURE, "");
                setAdapter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchToggle = menu.findItem(R.id.action_bar_search_toggle);
        if (searchToggle != null) {
            searchToggle.setVisible(!isSearchSettingsVisible);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @OnItemClick(R.id.search_list)
    void openLink(int position) {
        String humanLink = ((SubtextItem) resultList.getAdapter().getItem(position)).text;

        preferenceHelper.saveInt(KEY_SEARCH_POSITION, position);

        Intent intent = new Intent();
        intent.putExtra("linkOSIS", LinkConverter.getHumanToOSIS(humanLink));
        setResult(RESULT_OK, intent);

        finish();
    }

    @OnClick(R.id.search_modules_button)
    void openModulesDialog() {
        if (bibleModules.isEmpty()) {
            return;
        }

        final CharSequence[] names = new CharSequence[bibleModules.size()];
        final boolean[] checked = new boolean[bibleModules.size()];
        for (int i = 0; i < bibleModules.size(); i++) {
            ItemList module = bibleModules.get(i);
            String moduleId = module.get(ItemList.ID);
            String moduleName = module.get(ItemList.Name);
            names[i] = moduleId.toUpperCase(Locale.getDefault()) + " - " + moduleName;
            checked[i] = selectedModuleIds.contains(moduleId);
        }

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_dialog_header_logo)
                .setTitle(getString(R.string.app_name) + " - " + getString(R.string.search_modules))
                .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    ArrayList<String> newSelection = new ArrayList<>();
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) {
                            newSelection.add(bibleModules.get(i).get(ItemList.ID));
                        }
                    }
                    if (newSelection.isEmpty()) {
                        Toast.makeText(this, R.string.search_select_module_first, Toast.LENGTH_LONG).show();
                        return;
                    }

                    selectedModuleIds.clear();
                    selectedModuleIds.addAll(newSelection);
                    preferenceHelper.saveString(KEY_MODULE_IDS, joinIds(selectedModuleIds));
                    updateModulesButtonText();
                    spinnerInit();
                    setAdapter();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @OnClick(R.id.search_selected_books_button)
    void openSelectedBooksDialog() {
        if (booksForSearch.isEmpty()) {
            Toast.makeText(this, R.string.search_books_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        View content = getLayoutInflater().inflate(R.layout.dialog_search_books_picker, null);
        LinearLayout oldTestamentContainer = content.findViewById(R.id.books_old_testament_container);
        LinearLayout newTestamentContainer = content.findViewById(R.id.books_new_testament_container);
        LinearLayout otherBooksSection = content.findViewById(R.id.books_other_section);
        LinearLayout otherBooksContainer = content.findViewById(R.id.books_other_container);

        ArrayList<ItemList> oldTestamentBooks = new ArrayList<>();
        ArrayList<ItemList> newTestamentBooks = new ArrayList<>();
        ArrayList<ItemList> otherBooks = new ArrayList<>();
        for (ItemList book : booksForSearch) {
            String bookID = book.get(ItemList.ID);
            if (OLD_TESTAMENT_BOOK_IDS.contains(bookID)) {
                oldTestamentBooks.add(book);
            } else if (NEW_TESTAMENT_BOOK_IDS.contains(bookID)) {
                newTestamentBooks.add(book);
            } else {
                otherBooks.add(book);
            }
        }

        Set<String> selectedSet = new HashSet<>(selectedBookIds);
        ArrayList<BookChoiceViewHolder> choiceHolders = new ArrayList<>();
        fillBooksContainer(oldTestamentContainer, oldTestamentBooks, selectedSet, choiceHolders);
        fillBooksContainer(newTestamentContainer, newTestamentBooks, selectedSet, choiceHolders);
        fillBooksContainer(otherBooksContainer, otherBooks, selectedSet, choiceHolders);
        otherBooksSection.setVisibility(otherBooks.isEmpty() ? View.GONE : View.VISIBLE);

        AlertDialog booksDialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_dialog_header_logo)
                .setTitle(getString(R.string.app_name) + " - " + getString(R.string.search_selected_books))
                .setView(content)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    selectedBookIds.clear();
                    for (BookChoiceViewHolder holder : choiceHolders) {
                        if (holder.checkBox.isChecked()) {
                            selectedBookIds.add(holder.bookId);
                        }
                    }
                    preferenceHelper.saveString(KEY_SELECTED_BOOK_IDS, joinIds(selectedBookIds));
                    updateSelectedBooksButtonText();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        booksDialog.setOnShowListener(dialogInterface -> {
            Window window = booksDialog.getWindow();
            if (window == null) {
                return;
            }
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = Math.round(metrics.widthPixels * 0.92f);
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        });

        booksDialog.show();
    }

    @OnClick(R.id.search_start_button)
    void onSearchButtonClick() {
        startSearch();
    }

    void startSearch() {
        String query = searchText.getText().toString().trim();

        if (selectedModuleIds.isEmpty()) {
            Toast.makeText(this, R.string.search_select_module_first, Toast.LENGTH_LONG).show();
            return;
        }

        analyticsHelper.searchEvent(query, selectedModuleIds.get(0));

        String fromBookID = "";
        String toBookID = "";
        ArrayList<String> selectedBooks = new ArrayList<>();

        if (currentScope == SCOPE_BOOK) {
            int pos = singleBook.getSelectedItemPosition();
            if (pos == AdapterView.INVALID_POSITION || pos >= booksForSearch.size()) {
                return;
            }
            String bookId = booksForSearch.get(pos).get(ItemList.ID);
            selectedBooks.add(bookId);
            selectedBookIds.clear();
            selectedBookIds.add(bookId);
            preferenceHelper.saveString(KEY_SINGLE_BOOK_ID, bookId);
        } else if (currentScope == SCOPE_SELECTED_BOOKS) {
            if (selectedBookIds.isEmpty()) {
                Toast.makeText(this, R.string.search_select_book_first, Toast.LENGTH_LONG).show();
                return;
            }
            selectedBooks.addAll(selectedBookIds);
        } else if (currentScope == SCOPE_BOOK_RANGE) {
            int posFrom = spinnerFrom.getSelectedItemPosition();
            int posTo = spinnerTo.getSelectedItemPosition();
            if (posFrom == AdapterView.INVALID_POSITION || posTo == AdapterView.INVALID_POSITION
                    || posFrom >= booksForSearch.size() || posTo >= booksForSearch.size()) {
                return;
            }

            fromBookID = booksForSearch.get(posFrom).get(ItemList.ID);
            toBookID = booksForSearch.get(posTo).get(ItemList.ID);
        }

        Task mTask = new AsyncCommand(new StartSearch(
                SearchActivity.this,
                query,
                selectedModuleIds,
                getScopeKey(currentScope),
                selectedBooks,
                fromBookID,
                toBookID,
                wholeWordsMatch.isChecked()), progressMessage, false);
        mAsyncManager.setupTask(mTask, SearchActivity.this);
        setSearchSettingsVisible(false);
    }

    private void setSearchSettingsVisible(boolean visible) {
        isSearchSettingsVisible = visible;
        searchSettingsContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        updateResultsHeader();
        invalidateOptionsMenu();
    }

    private void updateResultsHeader() {
        String query = searchText.getText().toString().trim();
        boolean showHeader = !isSearchSettingsVisible && !query.isEmpty();
        if (!showHeader) {
            searchResultsHeader.setVisibility(View.GONE);
            return;
        }

        searchResultsHeader.setText(getString(R.string.search_results_for_query, query));
        searchResultsHeader.setVisibility(View.VISIBLE);
    }

    private void restoreSelectedPosition() {
        int fromBook = preferenceHelper.getInt(KEY_FROM_BOOK);
        int toBook = preferenceHelper.getInt(KEY_TO_BOOK);

        if (spinnerFrom.getCount() <= fromBook) {
            fromBook = 0;
        }
        if (spinnerTo.getCount() <= toBook) {
            toBook = Math.max(0, spinnerTo.getCount() - 1);
        }

        spinnerFrom.setSelection(fromBook);
        spinnerTo.setSelection(toBook);
    }

    private void saveSelectedPosition(int fromBook, int toBook) {
        preferenceHelper.saveInt(KEY_FROM_BOOK, fromBook);
        preferenceHelper.saveInt(KEY_TO_BOOK, toBook);
    }

    private void setAdapter() {
        updateResultsHeader();
        searchItems.clear();
        for (Map.Entry<String, String> entry : searchResults.entrySet()) {
            String humanLink;
            try {
                humanLink = LinkConverter.getOSIStoHuman(entry.getKey(), myLibrarian);
                searchItems.add(new SubtextItem(humanLink, entry.getValue()));
            } catch (BookNotFoundException e) {
                ExceptionHelper.onBookNotFoundException(e, this, TAG);
            } catch (OpenModuleException e) {
                ExceptionHelper.onOpenModuleException(e, this, TAG);
            }
        }
        ItemAdapter adapter = new ItemAdapter(this, searchItems);
        resultList.setAdapter(adapter);

        int changeSearchPosition = preferenceHelper.getInt(KEY_SEARCH_POSITION);
        if (changeSearchPosition < searchItems.size()) {
            resultList.setSelection(changeSearchPosition);
        }

        ArrayList<String> titleOptions = new ArrayList<>();
        titleOptions.add(getScopeDisplayName());
        if (wholeWordsMatch.isChecked()) {
            titleOptions.add(getString(R.string.search_whole_words_match));
        }
        StringBuilder title = new StringBuilder(getResources().getString(R.string.search));
        String optionsBlock = "";
        String resultsBlock = "";
        String modulesBlock = "";
        if (!titleOptions.isEmpty()) {
            StringBuilder optionsBuilder = new StringBuilder("[ ");
            for (int i = 0; i < titleOptions.size(); i++) {
                if (i > 0) {
                    optionsBuilder.append(" | ");
                }
                optionsBuilder.append(titleOptions.get(i));
            }
            optionsBuilder.append(" ]");
            optionsBlock = optionsBuilder.toString();
            title.append(" ").append(optionsBlock);
        }
        if (!searchResults.isEmpty()) {
            resultsBlock = String.format(Locale.getDefault(),
                    " (%d %s)", searchResults.size(), getResources().getString(R.string.results));
            title.append(resultsBlock);
        }
        if (selectedModuleIds.size() > 1) {
            modulesBlock = String.format(Locale.getDefault(), " [%d %s]", selectedModuleIds.size(), getString(R.string.modules));
            title.append(modulesBlock);
        }
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            String titleText = title.toString();
            if (optionsBlock.isEmpty() && resultsBlock.isEmpty() && modulesBlock.isEmpty()) {
                supportActionBar.setTitle(titleText);
                return;
            }

            SpannableString spannableTitle = new SpannableString(titleText);
            if (!optionsBlock.isEmpty()) {
                int optionsStart = titleText.indexOf(optionsBlock);
                if (optionsStart >= 0) {
                    spannableTitle.setSpan(
                            new RelativeSizeSpan(0.8f),
                            optionsStart,
                            optionsStart + optionsBlock.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            if (!resultsBlock.isEmpty()) {
                int resultsStart = titleText.indexOf(resultsBlock);
                if (resultsStart >= 0) {
                    spannableTitle.setSpan(
                            new RelativeSizeSpan(0.8f),
                            resultsStart,
                            resultsStart + resultsBlock.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            if (!modulesBlock.isEmpty()) {
                int modulesStart = titleText.indexOf(modulesBlock);
                if (modulesStart >= 0) {
                    spannableTitle.setSpan(
                            new RelativeSizeSpan(0.8f),
                            modulesStart,
                            modulesStart + modulesBlock.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            supportActionBar.setTitle(spannableTitle);
        }
    }

    private void initScopeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_search_spinner_selected,
                new String[]{
                        getString(R.string.search_scope_whole_bible),
                        getString(R.string.search_scope_old_testament),
                        getString(R.string.search_scope_new_testament),
                        getString(R.string.search_scope_book),
                        getString(R.string.search_scope_selected_books),
                        getString(R.string.search_scope_book_range)
                });
        adapter.setDropDownViewResource(R.layout.item_search_spinner_dropdown);
        searchScope.setAdapter(adapter);

        int scope = preferenceHelper.getInt(KEY_SCOPE);
        if (scope < SCOPE_WHOLE_BIBLE || scope > SCOPE_BOOK_RANGE) {
            scope = SCOPE_WHOLE_BIBLE;
        }
        currentScope = scope;
        searchScope.setSelection(scope);
        searchScope.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onScopeChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        applyScopeUi();
    }

    private void onScopeChanged(int scope) {
        currentScope = scope;
        preferenceHelper.saveInt(KEY_SCOPE, scope);

        if (scope == SCOPE_WHOLE_BIBLE || scope == SCOPE_OLD_TESTAMENT || scope == SCOPE_NEW_TESTAMENT) {
            clearBookSelections();
        } else if (scope == SCOPE_BOOK) {
            clearBookSelections();
            syncSingleBookSelection();
        } else if (scope == SCOPE_SELECTED_BOOKS) {
            clearBookSelections();
        } else if (scope == SCOPE_BOOK_RANGE) {
            clearBookSelections();
        }

        applyScopeUi();
        setAdapter();
    }

    private void spinnerInit() {
        booksForSearch = getBooksForSelectedModules();
        SimpleAdapter aa = new SimpleAdapter(this, booksForSearch,
                R.layout.item_search_spinner_selected,
                new String[]{ItemList.Name}, new int[]{android.R.id.text1});
        aa.setDropDownViewResource(R.layout.item_search_spinner_dropdown);
        SimpleAdapter.ViewBinder viewBinder = (view, data, textRepresentation) -> {
            TextView textView = (TextView) view;
            textView.setText(textRepresentation);
            return true;
        };
        aa.setViewBinder(viewBinder);

        spinnerFrom.setAdapter(aa);
        spinnerFrom.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                int fromBook = spinnerFrom.getSelectedItemPosition();
                int toBook = spinnerTo.getSelectedItemPosition();
                if (fromBook > toBook) {
                    spinnerTo.setSelection(fromBook);
                    toBook = fromBook;
                }
                saveSelectedPosition(fromBook, toBook);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinnerTo.setAdapter(aa);
        spinnerTo.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                int fromBook = spinnerFrom.getSelectedItemPosition();
                int toBook = spinnerTo.getSelectedItemPosition();
                if (fromBook > toBook) {
                    spinnerFrom.setSelection(toBook);
                    fromBook = toBook;
                }
                saveSelectedPosition(fromBook, toBook);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        singleBook.setAdapter(aa);
        singleBook.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= booksForSearch.size()) {
                    return;
                }
                String bookID = booksForSearch.get(position).get(ItemList.ID);
                selectedBookIds.clear();
                selectedBookIds.add(bookID);
                preferenceHelper.saveString(KEY_SINGLE_BOOK_ID, bookID);
                updateSelectedBooksButtonText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        restoreSingleBook();
        restoreSelectedBooks();
        restoreSelectedPosition();
        applyScopeUi();
    }

    private void loadBibleModules() {
        bibleModules.clear();
        bibleModules.addAll(myLibrarian.getBibleModulesList());
    }

    private void restoreSelectedModules() {
        selectedModuleIds.clear();

        Set<String> validIds = new HashSet<>();
        for (ItemList module : bibleModules) {
            validIds.add(module.get(ItemList.ID));
        }

        ArrayList<String> savedIds = splitIds(preferenceHelper.getString(KEY_MODULE_IDS));
        for (String id : savedIds) {
            if (validIds.contains(id)) {
                selectedModuleIds.add(id);
            }
        }

        if (!selectedModuleIds.isEmpty()) {
            return;
        }

        String currentId = myLibrarian.getModuleID();
        if (validIds.contains(currentId)) {
            selectedModuleIds.add(currentId);
        } else if (!bibleModules.isEmpty()) {
            selectedModuleIds.add(bibleModules.get(0).get(ItemList.ID));
        }
        preferenceHelper.saveString(KEY_MODULE_IDS, joinIds(selectedModuleIds));
    }

    private ArrayList<ItemList> getBooksForSelectedModules() {
        LinkedHashMap<String, ItemList> uniqueBooks = new LinkedHashMap<>();
        for (String moduleID : selectedModuleIds) {
            try {
                ArrayList<ItemList> moduleBooks = myLibrarian.getModuleBooksList(moduleID);
                for (ItemList book : moduleBooks) {
                    String id = book.get(ItemList.ID);
                    if (!uniqueBooks.containsKey(id)) {
                        uniqueBooks.put(id, book);
                    }
                }
            } catch (OpenModuleException e) {
                ExceptionHelper.onOpenModuleException(e, this, TAG);
            } catch (BooksDefinitionException e) {
                ExceptionHelper.onBooksDefinitionException(e, this, TAG);
            } catch (BookDefinitionException e) {
                ExceptionHelper.onBookDefinitionException(e, this, TAG);
            }
        }
        return new ArrayList<>(uniqueBooks.values());
    }

    private void restoreSingleBook() {
        String singleBookID = preferenceHelper.getString(KEY_SINGLE_BOOK_ID);
        if (singleBookID.isEmpty()) {
            return;
        }
        for (int i = 0; i < booksForSearch.size(); i++) {
            if (singleBookID.equals(booksForSearch.get(i).get(ItemList.ID))) {
                singleBook.setSelection(i);
                return;
            }
        }
    }

    private void restoreSelectedBooks() {
        selectedBookIds.clear();
        Set<String> availableIds = new HashSet<>();
        for (ItemList book : booksForSearch) {
            availableIds.add(book.get(ItemList.ID));
        }

        ArrayList<String> savedBookIds = splitIds(preferenceHelper.getString(KEY_SELECTED_BOOK_IDS));
        for (String id : savedBookIds) {
            if (availableIds.contains(id)) {
                selectedBookIds.add(id);
            }
        }

        updateSelectedBooksButtonText();
    }

    private void applyScopeUi() {
        boolean showSingleBook = currentScope == SCOPE_BOOK;
        boolean showSelectedBooks = currentScope == SCOPE_SELECTED_BOOKS;
        boolean showRange = currentScope == SCOPE_BOOK_RANGE;

        singleBook.setVisibility(showSingleBook ? View.VISIBLE : View.GONE);
        selectedBooksButton.setVisibility(showSelectedBooks ? View.VISIBLE : View.GONE);
        rangeContainer.setVisibility(showRange ? View.VISIBLE : View.GONE);
    }

    private void clearBookSelections() {
        selectedBookIds.clear();
        preferenceHelper.saveString(KEY_SELECTED_BOOK_IDS, "");
        preferenceHelper.saveString(KEY_SINGLE_BOOK_ID, "");
        updateSelectedBooksButtonText();
    }

    private void syncSingleBookSelection() {
        int pos = singleBook.getSelectedItemPosition();
        if (pos == AdapterView.INVALID_POSITION || pos >= booksForSearch.size()) {
            return;
        }
        String bookID = booksForSearch.get(pos).get(ItemList.ID);
        selectedBookIds.clear();
        selectedBookIds.add(bookID);
        preferenceHelper.saveString(KEY_SINGLE_BOOK_ID, bookID);
        updateSelectedBooksButtonText();
    }

    private void fillBooksContainer(
            LinearLayout container,
            ArrayList<ItemList> books,
            Set<String> selectedSet,
            ArrayList<BookChoiceViewHolder> choiceHolders
    ) {
        container.removeAllViews();
        for (ItemList book : books) {
            View row = getLayoutInflater().inflate(R.layout.item_dialog_search_book_checkbox, container, false);
            CheckBox checkBox = row.findViewById(R.id.search_book_checkbox);
            String bookId = book.get(ItemList.ID);
            checkBox.setText(book.get(ItemList.Name));
            checkBox.setChecked(selectedSet.contains(bookId));
            container.addView(row);
            choiceHolders.add(new BookChoiceViewHolder(bookId, checkBox));
        }
    }

    private static final class BookChoiceViewHolder {
        private final String bookId;
        private final CheckBox checkBox;

        private BookChoiceViewHolder(String bookId, CheckBox checkBox) {
            this.bookId = bookId;
            this.checkBox = checkBox;
        }
    }

    private void updateModulesButtonText() {
        if (selectedModuleIds.isEmpty()) {
            modulesButton.setText(R.string.search_modules_choose);
            return;
        }

        if (selectedModuleIds.size() == 1) {
            String id = selectedModuleIds.get(0);
            modulesButton.setText(getModuleDisplayName(id));
            return;
        }

        modulesButton.setText(getString(
                R.string.search_modules_selected,
                selectedModuleIds.size(),
                getSelectedModuleCodesText()));
    }

    private String getSelectedModuleCodesText() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < selectedModuleIds.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(selectedModuleIds.get(i).toUpperCase(Locale.getDefault()));
        }
        return builder.toString();
    }

    private void updateSelectedBooksButtonText() {
        if (selectedBookIds.isEmpty()) {
            selectedBooksButton.setText(R.string.search_selected_books_choose);
            return;
        }
        selectedBooksButton.setText(getString(R.string.search_selected_books_count, selectedBookIds.size()));
    }

    private String getModuleDisplayName(String moduleID) {
        for (ItemList module : bibleModules) {
            if (moduleID.equals(module.get(ItemList.ID))) {
                return getString(
                        R.string.search_module_single,
                        moduleID.toUpperCase(Locale.getDefault()),
                        module.get(ItemList.Name));
            }
        }
        return moduleID.toUpperCase(Locale.getDefault());
    }

    private String getScopeDisplayName() {
        switch (currentScope) {
            case SCOPE_OLD_TESTAMENT:
                return getString(R.string.search_scope_old_testament);
            case SCOPE_NEW_TESTAMENT:
                return getString(R.string.search_scope_new_testament);
            case SCOPE_BOOK:
                return getString(R.string.search_scope_book);
            case SCOPE_SELECTED_BOOKS:
                return getString(R.string.search_scope_selected_books);
            case SCOPE_BOOK_RANGE:
                return getString(R.string.search_scope_book_range);
            default:
                return getString(R.string.search_scope_whole_bible);
        }
    }

    private String getScopeKey(int scope) {
        switch (scope) {
            case SCOPE_OLD_TESTAMENT:
                return Librarian.SEARCH_SCOPE_OLD_TESTAMENT;
            case SCOPE_NEW_TESTAMENT:
                return Librarian.SEARCH_SCOPE_NEW_TESTAMENT;
            case SCOPE_BOOK:
                return Librarian.SEARCH_SCOPE_BOOK;
            case SCOPE_SELECTED_BOOKS:
                return Librarian.SEARCH_SCOPE_SELECTED_BOOKS;
            case SCOPE_BOOK_RANGE:
                return Librarian.SEARCH_SCOPE_BOOK_RANGE;
            default:
                return Librarian.SEARCH_SCOPE_WHOLE_BIBLE;
        }
    }

    private String buildSearchSignature() {
        ArrayList<String> modules = new ArrayList<>(selectedModuleIds);
        Collections.sort(modules);

        StringBuilder signature = new StringBuilder();
        signature.append("modules=").append(joinIds(modules));
        signature.append("|scope=").append(currentScope);
        signature.append("|whole=").append(wholeWordsMatch.isChecked() ? 1 : 0);

        if (currentScope == SCOPE_BOOK || currentScope == SCOPE_SELECTED_BOOKS) {
            ArrayList<String> books = new ArrayList<>(selectedBookIds);
            Collections.sort(books);
            signature.append("|books=").append(joinIds(books));
        } else if (currentScope == SCOPE_BOOK_RANGE) {
            signature.append("|from=").append(getRangeFromBookId());
            signature.append("|to=").append(getRangeToBookId());
        }

        return signature.toString();
    }

    private String getRangeFromBookId() {
        int posFrom = spinnerFrom.getSelectedItemPosition();
        if (posFrom == AdapterView.INVALID_POSITION || posFrom >= booksForSearch.size()) {
            return "";
        }
        return booksForSearch.get(posFrom).get(ItemList.ID);
    }

    private String getRangeToBookId() {
        int posTo = spinnerTo.getSelectedItemPosition();
        if (posTo == AdapterView.INVALID_POSITION || posTo >= booksForSearch.size()) {
            return "";
        }
        return booksForSearch.get(posTo).get(ItemList.ID);
    }

    private ArrayList<String> splitIds(String value) {
        ArrayList<String> result = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return result;
        }
        String[] values = value.split(",");
        for (String item : values) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private String joinIds(ArrayList<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }
}
