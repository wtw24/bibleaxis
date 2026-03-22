/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.reader;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import de.wladimirwendland.bibleaxis.data.backup.HighlightsBackupManager;
import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.entity.Highlight;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;
import de.wladimirwendland.bibleaxis.domain.exceptions.ExceptionHelper;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.domain.repository.IHighlightsRepository;
import de.wladimirwendland.bibleaxis.domain.threading.AppTaskRunner;
import de.wladimirwendland.bibleaxis.domain.textFormatters.ITextFormatter;
import de.wladimirwendland.bibleaxis.entity.TextAppearance;
import de.wladimirwendland.bibleaxis.managers.Librarian;
import de.wladimirwendland.bibleaxis.presentation.ui.about.AboutActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.bookmarks.BookmarksActivity;
import de.wladimirwendland.bibleaxis.presentation.dialogs.DialogUiHelper;
import de.wladimirwendland.bibleaxis.presentation.ui.help.HelpActivity;
import de.wladimirwendland.bibleaxis.presentation.history.HistoryActivity;
import de.wladimirwendland.bibleaxis.presentation.imagepreview.ImagePreviewActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.library.LibraryActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.search.SearchActivity;
import de.wladimirwendland.bibleaxis.presentation.ui.settings.SettingsActivity;
import de.wladimirwendland.bibleaxis.presentation.widget.Mode;
import de.wladimirwendland.bibleaxis.presentation.widget.ReaderWebView;
import de.wladimirwendland.bibleaxis.utils.DevicesKeyCodes;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONTokener;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import javax.inject.Inject;

import de.wladimirwendland.bibleaxis.domain.config.FeatureToggle;

public class ReaderActivity extends BaseActivity<ReaderViewPresenter>
        implements ReaderView, IReaderViewListener, ReaderWebView.OnFindInPageListener {

    @Inject
    FeatureToggle featureToggle;
    @Inject
    Librarian librarian;
    @Inject
    IHighlightsRepository highlightsRepository;
    @Inject
    AppTaskRunner appTaskRunner;
    @Inject
    HighlightsBackupManager highlightsBackupManager;

    private static final String KEY_LINK_OSIS = "linkOSIS";
    private static final String TAG = ReaderActivity.class.getSimpleName();

    private final ActivityResultLauncher<Intent> openLinkResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK) {
                    return;
                }
                Intent data = result.getData();
                if (data == null) {
                    return;
                }
                String osisLink = data.getStringExtra(KEY_LINK_OSIS);
                if (!TextUtils.isEmpty(osisLink)) {
                    presenter.openLink(osisLink);
                }
            });
    private final ActivityResultLauncher<Intent> settingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> presenter.onChangeSettings());

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ReaderWebView readerView;
    private SwitchCompat highlightsSwitch;
    private SwitchCompat nightModeSwitch;
    private SwitchCompat findInPageSwitch;
    private SwitchCompat strongNumbersSwitch;
    private LinearLayout highlightPalette;
    private LinearLayout findInPagePanel;
    private EditText findInPageInput;
    private TextView findInPageCounter;
    private ImageButton findInPagePrev;
    private ImageButton findInPageNext;
    private ImageButton findInPageClose;

    private ActionMode currActionMode;
    private boolean exitToBackKey;
    private boolean isHighlightsVisible = true;
    private boolean isFindInPageVisible;
    private boolean isUpdatingHighlightsSwitch;
    private boolean isUpdatingNightModeSwitch;
    private boolean isUpdatingFindInPageSwitch;
    private boolean isUpdatingStrongNumbersSwitch;
    private JSONObject pendingSelection;
    private String pendingSelectionSignature;
    private final List<String> highlightColors = Arrays.asList(
            "#fff59d",
            "#ffcc80",
            "#ef9a9a",
            "#ce93d8",
            "#80deea",
            "#a5d6a7"
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        readerView = findViewById(R.id.readerView);
        highlightPalette = findViewById(R.id.highlight_palette);
        findInPagePanel = findViewById(R.id.find_in_page_panel);
        findInPageInput = findViewById(R.id.find_in_page_input);
        findInPageCounter = findViewById(R.id.find_in_page_counter);
        findInPagePrev = findViewById(R.id.find_in_page_prev);
        findInPageNext = findViewById(R.id.find_in_page_next);
        findInPageClose = findViewById(R.id.find_in_page_close);
        initHighlightPalette();
        setupFindInPagePanel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(v -> {
            presenter.onClickChooseChapter();
            analyticsHelper.clickEvent("choose_ch");
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            menuItem.setChecked(false);
            boolean handled = onNavigationItemSelected(menuItem);
            if (handled
                    && menuItem.getItemId() != R.id.drawer_night_mode
                    && menuItem.getItemId() != R.id.drawer_highlights
                    && menuItem.getItemId() != R.id.drawer_find_in_page
                    && menuItem.getItemId() != R.id.drawer_strong_numbers) {
                drawerLayout.closeDrawers();
            }
            return handled;
        });
        setupHighlightsSwitch();
        setupNightModeSwitch();
        setupFindInPageSwitch();
        setupStrongNumbersSwitch();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        actionBarDrawerToggle.syncState();

        readerView.setOnReaderViewListener(this);
        readerView.setOnFindInPageListener(this);

        presenter.openLastLink();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void attachView() {
        presenter.attachView(this);
    }

    @Override
    protected int getRootLayout() {
        return R.layout.activity_reader;
    }

    @Override
    protected void inject(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reader, menu);
        MenuItem findInPageItem = menu.findItem(R.id.action_bar_find_in_page);
        if (findInPageItem != null) {
            findInPageItem.setVisible(presenter.isFindInPageEnabled());
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem findInPageItem = menu.findItem(R.id.action_bar_find_in_page);
        if (findInPageItem != null) {
            findInPageItem.setVisible(presenter.isFindInPageEnabled());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_find_in_page:
                showFindInPagePanel();
                analyticsHelper.clickEvent("find_in_page");
                break;
            case R.id.action_bar_search:
                openSearchActivity();
                analyticsHelper.clickEvent("search");
                break;
            case R.id.action_bar_history:
                openHistoryActivity();
                analyticsHelper.clickEvent("history");
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isFindInPageVisible) {
            hideFindInPagePanel();
            return;
        }
        if (exitToBackKey) {
            presenter.onPause();
            super.onBackPressed();
        } else {
            exitToBackKey = true;
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> exitToBackKey = false, 3000);
        }
    }

    @Override
    public boolean onSearchRequested() {
        openSearchActivity();
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP && presenter.isVolumeButtonsToScroll())
                || DevicesKeyCodes.keyCodeUp(keyCode)) {
            readerView.pageUp(false);
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && presenter.isVolumeButtonsToScroll())
                || DevicesKeyCodes.keyCodeDown(keyCode)) {
            readerView.pageDown(false);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
        syncHighlightsSwitch();
        syncNightModeSwitch();
        syncFindInPageSwitch();
        syncStrongNumbersSwitch();
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public void onReaderViewChange(ChangeCode code) {
        switch (code) {
            case onChangeReaderMode:
                updateActivityMode();
                break;
            case onChangeSelection:
                if (pendingSelection != null) {
                    clearPendingSelection();
                }
                TreeSet<Integer> selVerses = readerView.getSelectedVerses();
                if (selVerses.size() == 0) {
                    disableActionMode();
                } else if (currActionMode == null) {
                    currActionMode = startSupportActionMode(new SelectTextHandler(this, readerView));
                }
                break;
            case onLongPress:
                if (readerView.getReaderMode() == Mode.Read) {
                    presenter.onClickChooseChapter();
                }
                break;
            case onUpNavigation:
                readerView.pageUp(false);
                break;
            case onDownNavigation:
                readerView.pageDown(false);
                break;
            case onLeftNavigation:
                presenter.prevChapter();
                break;
            case onRightNavigation:
                presenter.nextChapter();
                break;
        }
    }

    @Override
    public void onReaderClickImage(String path) {
        openImageViewActivity(path);
    }

    @Override
    public void onReaderClickHighlight(String highlightId) {
        try {
            final long id = Long.parseLong(highlightId);
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.highlight)
                    .setView(DialogUiHelper.createMessageView(this, getString(R.string.highlight_delete_question)))
                    .setPositiveButton(R.string.delete, (dialog, which) -> deleteHighlight(id))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } catch (NumberFormatException ignored) {
            // ignore invalid id
        }
    }

    @Override
    public void onReaderClickStrong(String strongCode) {
        Toast.makeText(this, getString(R.string.strong_click_placeholder, strongCode), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReaderTextSelection(String payload) {
        try {
            if (TextUtils.isEmpty(payload)) {
                clearPendingSelection();
                return;
            }

            JSONObject json = parseSelectionPayload(payload);
            if (!json.has("startVerse") || !json.has("endVerse")) {
                clearPendingSelection();
                return;
            }

            String quote = json.optString("quote");
            if (TextUtils.isEmpty(quote) || TextUtils.isEmpty(quote.trim())) {
                clearPendingSelection();
                return;
            }

            String signature = String.format(
                    "%d:%d-%d:%d:%s",
                    json.optInt("startVerse"),
                    json.optInt("startOffset"),
                    json.optInt("endVerse"),
                    json.optInt("endOffset"),
                    quote
            );
            if (signature.equals(pendingSelectionSignature)) {
                return;
            }

            pendingSelectionSignature = signature;
            pendingSelection = json;
            showHighlightPalette();
        } catch (Exception ex) {
            clearPendingSelection();
        }
    }

    @Override
    public void setCurrentOrientation(boolean disableAutoRotation) {
        if (!disableAutoRotation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            return;
        }

        Display display = getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        int height;
        int width;

        Point size = new Point();
        display.getSize(size);
        height = size.y;
        width = size.x;

        switch (rotation) {
            case Surface.ROTATION_90:
                if (width > height) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                }
                break;
            case Surface.ROTATION_180:
                if (height > width) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
                break;
            case Surface.ROTATION_270:
                if (width > height) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
            case Surface.ROTATION_0:
                if (height > width) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            default:
                // nothing
        }
    }

    @Override
    public void openLibraryActivity() {
        openLinkResultLauncher.launch(LibraryActivity.createIntent(this));
    }

    @Override
    public void setKeepScreen(boolean isKeepScreen) {
        readerView.setKeepScreenOn(isKeepScreen);
    }

    @Override
    public void setReaderMode(Mode mode) {
        readerView.setMode(mode);
        if (mode != Mode.Study) {
            clearPendingSelection();
        }
        updateActivityMode();
    }

    @Override
    public void onOpenChapterFailure(Throwable ex) {
        if (ex instanceof OpenModuleException) {
            ExceptionHelper.onOpenModuleException((OpenModuleException) ex, this, TAG);
        } else if (ex instanceof BookNotFoundException) {
            ExceptionHelper.onBookNotFoundException((BookNotFoundException) ex, this, TAG);
        } else {
            ExceptionHelper.onException(ex, this, TAG);
        }
        hideProgress();
    }

    @Override
    public void setContent(String baseUrl, Chapter chapter, int verse, boolean isBible, List<Highlight> highlights) {
        clearPendingSelection();
        readerView.setContent(baseUrl, chapter, verse, isBible, highlights);
        readerView.setHighlightsVisible(isHighlightsVisible);
        resetFindInPageState();
    }

    @Override
    public void onFindResult(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
        runOnUiThread(() -> {
            if (findInPageCounter == null || !isFindInPageVisible) {
                return;
            }
            updateFindInPageCounter(activeMatchOrdinal, numberOfMatches);
        });
    }

    @Override
    public void setTextAppearance(TextAppearance textAppearance) {
        readerView.setTextAppearance(textAppearance);
    }

    @Override
    public void setTitle(String moduleName, String link) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String title = TextUtils.isEmpty(moduleName)
                    ? link
                    : moduleName + " - " + link;
            actionBar.setTitle(title);
            actionBar.setSubtitle(null);
        }
    }

    @Override
    public void updateActivityMode() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (readerView.getReaderMode() == Mode.Read) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        }
    }

    @Override
    public void updateContent() {
        readerView.update();
    }

    @Override
    public void setTextFormatter(@NonNull ITextFormatter formatter) {
        readerView.setFormatter(formatter);
    }

    @Override
    public void disableActionMode() {
        if (currActionMode != null) {
            currActionMode.finish();
            currActionMode = null;
        }
    }

    @Override
    public int getCurrVerse() {
        return readerView.getCurrVerse();
    }

    private boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.drawer_library:
                openLibraryActivity();
                return true;
            case R.id.drawer_bookmarks:
                openBookmarkActivity();
                return true;
            case R.id.drawer_tags:
                openTagsActivity();
                return true;
            case R.id.drawer_settings:
                openSettingsActivity();
                return true;
            case R.id.drawer_night_mode:
                if (nightModeSwitch != null) {
                    nightModeSwitch.setChecked(!nightModeSwitch.isChecked());
                } else {
                    presenter.inverseNightMode();
                    analyticsHelper.clickEvent("night_mode");
                }
                return true;
            case R.id.drawer_highlights:
                if (highlightsSwitch != null) {
                    highlightsSwitch.setChecked(!highlightsSwitch.isChecked());
                } else {
                    setHighlightsVisible(!isHighlightsVisible);
                }
                return true;
            case R.id.drawer_find_in_page:
                if (findInPageSwitch != null) {
                    findInPageSwitch.setChecked(!findInPageSwitch.isChecked());
                } else {
                    setFindInPageEnabled(!presenter.isFindInPageEnabled());
                }
                return true;
            case R.id.drawer_strong_numbers:
                if (strongNumbersSwitch != null) {
                    strongNumbersSwitch.setChecked(!strongNumbersSwitch.isChecked());
                } else {
                    setStrongNumbersEnabled(!presenter.isStrongNumbersEnabled());
                }
                return true;
            case R.id.drawer_help:
                openHelpActivity();
                return true;
            case R.id.drawer_about:
                openAboutActivity();
                return true;
            default:
                return false;
        }
    }

    private void openAboutActivity() {
        Intent intentAbout = new Intent().setClass(this, AboutActivity.class);
        startActivity(intentAbout);
    }

    private void openBookmarkActivity() {
        Intent intentBookmarks = new Intent()
                .setClass(this, BookmarksActivity.class)
                .putExtra(BookmarksActivity.EXTRA_MODE, BookmarksActivity.MODE_BOOKMARKS);
        openLinkResultLauncher.launch(intentBookmarks);
    }

    private void openHelpActivity() {
        Intent intentHelp = new Intent(this, HelpActivity.class);
        startActivity(intentHelp);
    }

    private void openHistoryActivity() {
        Intent intentHistory = new Intent().setClass(this, HistoryActivity.class);
        openLinkResultLauncher.launch(intentHistory);
    }

    private void openImageViewActivity(String imagePath) {
        startActivity(ImagePreviewActivity.getIntent(this, imagePath));
    }

    private void openSearchActivity() {
        Intent intentSearch = new Intent().setClass(this, SearchActivity.class);
        openLinkResultLauncher.launch(intentSearch);
    }

    private void setupFindInPagePanel() {
        if (findInPageInput == null || findInPageCounter == null) {
            return;
        }

        findInPageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString();
                if (TextUtils.isEmpty(query)) {
                    readerView.clearFindInPage();
                    findInPageCounter.setText(getString(R.string.find_in_page_matches_format, 0, 0));
                    return;
                }
                readerView.findInPage(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nothing
            }
        });

        if (findInPagePrev != null) {
            findInPagePrev.setOnClickListener(v -> readerView.findNextInPage(false));
        }
        if (findInPageNext != null) {
            findInPageNext.setOnClickListener(v -> readerView.findNextInPage(true));
        }
        if (findInPageClose != null) {
            findInPageClose.setOnClickListener(v -> hideFindInPagePanel());
        }

        updateFindInPageCounter(-1, 0);
    }

    private void showFindInPagePanel() {
        if (findInPagePanel == null || findInPageInput == null) {
            return;
        }
        findInPagePanel.setVisibility(View.VISIBLE);
        isFindInPageVisible = true;
        findInPageInput.requestFocus();
    }

    private void hideFindInPagePanel() {
        if (findInPagePanel == null || findInPageInput == null) {
            return;
        }
        findInPagePanel.setVisibility(View.GONE);
        isFindInPageVisible = false;
        findInPageInput.setText("");
        findInPageInput.clearFocus();
        readerView.clearFindInPage();
        updateFindInPageCounter(-1, 0);
    }

    private void resetFindInPageState() {
        if (findInPageInput != null) {
            findInPageInput.setText("");
            findInPageInput.clearFocus();
        }
        if (findInPagePanel != null) {
            findInPagePanel.setVisibility(View.GONE);
        }
        isFindInPageVisible = false;
        readerView.clearFindInPage();
        updateFindInPageCounter(-1, 0);
    }

    private void updateFindInPageCounter(int activeMatchOrdinal, int numberOfMatches) {
        if (findInPageCounter == null) {
            return;
        }

        if (numberOfMatches <= 0) {
            String query = findInPageInput == null ? "" : findInPageInput.getText().toString();
            if (TextUtils.isEmpty(query)) {
                findInPageCounter.setText(getString(R.string.find_in_page_matches_format, 0, 0));
            } else {
                findInPageCounter.setText(R.string.find_in_page_no_matches);
            }
            return;
        }

        int currentMatch = Math.max(1, activeMatchOrdinal + 1);
        findInPageCounter.setText(getString(R.string.find_in_page_matches_format, currentMatch, numberOfMatches));
    }

    private void openSettingsActivity() {
        Intent intentSettings = new Intent(this, SettingsActivity.class);
        settingsResultLauncher.launch(intentSettings);
    }

    private void openTagsActivity() {
        Intent intentBookmarks = new Intent()
                .setClass(this, BookmarksActivity.class)
                .putExtra(BookmarksActivity.EXTRA_MODE, BookmarksActivity.MODE_TAGS);
        openLinkResultLauncher.launch(intentBookmarks);
    }

    void openCrossReferenceActivity(String osisLink) {
        Intent intentParallels = new Intent(this, de.wladimirwendland.bibleaxis.presentation.ui.crossreference.CrossReferenceActivity.class);
        intentParallels.putExtra(KEY_LINK_OSIS, osisLink);
        openLinkResultLauncher.launch(intentParallels);
    }

    private void saveHighlight(JSONObject selectionJson, String color) {
        try {
            int startVerse = selectionJson.getInt("startVerse");
            int startOffset = selectionJson.getInt("startOffset");
            int endVerse = selectionJson.getInt("endVerse");
            int endOffset = selectionJson.getInt("endOffset");
            String quote = selectionJson.optString("quote");

            if (TextUtils.isEmpty(quote)) {
                Toast.makeText(this, R.string.highlight_select_text_first, Toast.LENGTH_SHORT).show();
                return;
            }

            final de.wladimirwendland.bibleaxis.domain.entity.BibleReference current = librarian.getCurrentOSISLink();
            final Highlight highlight = new Highlight(
                    current.getModuleID(),
                    current.getBookID(),
                    current.getChapter(),
                    startVerse,
                    startOffset,
                    endVerse,
                    endOffset,
                    color,
                    quote
            );

            appTaskRunner.runOnIo(() -> {
                long result = highlightsRepository.add(highlight);
                appTaskRunner.runOnMain(() -> {
                    if (result == -1) {
                        Toast.makeText(this, R.string.highlight_save_failed, Toast.LENGTH_SHORT).show();
                    } else {
                        highlightsBackupManager.scheduleAutoBackup();
                        if (!isHighlightsVisible) {
                            setHighlightsVisible(true);
                        }
                        readerView.clearTextSelection();
                        clearPendingSelection();
                        presenter.openLink(librarian.getCurrentOSISLink().getPath());
                    }
                });
            });
        } catch (Exception ex) {
            Toast.makeText(this, R.string.highlight_select_text_first, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteHighlight(long highlightId) {
        appTaskRunner.runOnIo(() -> {
            boolean deleted = highlightsRepository.delete(highlightId);
            appTaskRunner.runOnMain(() -> {
                if (!deleted) {
                    Toast.makeText(this, R.string.highlight_delete_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                highlightsBackupManager.scheduleAutoBackup();
                readerView.clearTextSelection();
                clearPendingSelection();
                presenter.openLink(librarian.getCurrentOSISLink().getPath());
            });
        });
    }

    private JSONObject parseSelectionPayload(String payload) throws Exception {
        Object parsed = new JSONTokener(payload).nextValue();
        if (parsed instanceof JSONObject) {
            return (JSONObject) parsed;
        }
        if (parsed instanceof String) {
            return new JSONObject((String) parsed);
        }
        throw new IllegalArgumentException("Unsupported payload format");
    }

    private void showHighlightPalette() {
        if (highlightPalette == null) {
            return;
        }
        if (highlightPalette.getVisibility() != View.VISIBLE) {
            highlightPalette.setVisibility(View.VISIBLE);
        }
    }

    private void clearPendingSelection() {
        pendingSelection = null;
        pendingSelectionSignature = null;
        if (highlightPalette != null && highlightPalette.getVisibility() != View.GONE) {
            highlightPalette.setVisibility(View.GONE);
        }
    }

    private void initHighlightPalette() {
        if (highlightPalette == null) {
            return;
        }

        highlightPalette.removeAllViews();

        for (String color : highlightColors) {
            View chip = new View(this);
            int size = dpToPx(34);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            int margin = dpToPx(4);
            params.setMargins(margin, margin, margin, margin);
            chip.setLayoutParams(params);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(android.graphics.Color.parseColor(color));
            bg.setStroke(dpToPx(2), 0xFFFFFFFF);
            chip.setBackground(bg);
            chip.setContentDescription(color);
            chip.setOnClickListener(v -> {
                if (pendingSelection != null) {
                    saveHighlight(pendingSelection, color);
                }
            });

            highlightPalette.addView(chip);
        }

        TextView closeButton = new TextView(this);
        int closeSize = dpToPx(34);
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(closeSize, closeSize);
        int closeMargin = dpToPx(4);
        closeParams.setMargins(closeMargin, closeMargin, closeMargin, closeMargin);
        closeButton.setLayoutParams(closeParams);
        closeButton.setText("×");
        closeButton.setTextSize(20);
        closeButton.setGravity(Gravity.CENTER);
        closeButton.setIncludeFontPadding(false);
        closeButton.setTextColor(0xFFFFFFFF);
        closeButton.setContentDescription(getString(R.string.highlight_close_palette));

        GradientDrawable closeBg = new GradientDrawable();
        closeBg.setShape(GradientDrawable.OVAL);
        closeBg.setColor(0xFF444444);
        closeBg.setStroke(dpToPx(1), 0xFFBBBBBB);
        closeButton.setBackground(closeBg);
        closeButton.setOnClickListener(v -> clearPendingSelection());

        highlightPalette.addView(closeButton);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private void setupNightModeSwitch() {
        if (navigationView == null) {
            return;
        }
        MenuItem item = navigationView.getMenu().findItem(R.id.drawer_night_mode);
        if (item == null) {
            return;
        }

        View actionView = item.getActionView();
        if (actionView == null) {
            return;
        }

        nightModeSwitch = actionView.findViewById(R.id.drawer_night_mode_switch);
        if (nightModeSwitch == null) {
            return;
        }

        nightModeSwitch.setClickable(true);
        nightModeSwitch.setFocusable(false);
        actionView.setOnClickListener(v -> nightModeSwitch.setChecked(!nightModeSwitch.isChecked()));
        nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingNightModeSwitch) {
                return;
            }
            presenter.setNightMode(isChecked);
            analyticsHelper.clickEvent("night_mode");
        });
        syncNightModeSwitch();
    }

    private void setupFindInPageSwitch() {
        if (navigationView == null) {
            return;
        }
        MenuItem item = navigationView.getMenu().findItem(R.id.drawer_find_in_page);
        if (item == null) {
            return;
        }

        View actionView = item.getActionView();
        if (actionView == null) {
            return;
        }

        findInPageSwitch = actionView.findViewById(R.id.drawer_find_in_page_switch);
        if (findInPageSwitch == null) {
            return;
        }

        findInPageSwitch.setClickable(true);
        findInPageSwitch.setFocusable(false);
        actionView.setOnClickListener(v -> findInPageSwitch.setChecked(!findInPageSwitch.isChecked()));
        findInPageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingFindInPageSwitch) {
                return;
            }
            setFindInPageEnabled(isChecked);
        });
        syncFindInPageSwitch();
    }

    private void setupHighlightsSwitch() {
        if (navigationView == null) {
            return;
        }
        MenuItem item = navigationView.getMenu().findItem(R.id.drawer_highlights);
        if (item == null) {
            return;
        }

        View actionView = item.getActionView();
        if (actionView == null) {
            return;
        }

        highlightsSwitch = actionView.findViewById(R.id.drawer_highlights_switch);
        if (highlightsSwitch == null) {
            return;
        }

        highlightsSwitch.setClickable(true);
        highlightsSwitch.setFocusable(false);
        actionView.setOnClickListener(v -> highlightsSwitch.setChecked(!highlightsSwitch.isChecked()));
        highlightsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingHighlightsSwitch) {
                return;
            }
            setHighlightsVisible(isChecked);
        });
        syncHighlightsSwitch();
    }

    private void setupStrongNumbersSwitch() {
        if (navigationView == null) {
            return;
        }
        MenuItem item = navigationView.getMenu().findItem(R.id.drawer_strong_numbers);
        if (item == null) {
            return;
        }

        View actionView = item.getActionView();
        if (actionView == null) {
            return;
        }

        strongNumbersSwitch = actionView.findViewById(R.id.drawer_strong_numbers_switch);
        if (strongNumbersSwitch == null) {
            return;
        }

        strongNumbersSwitch.setClickable(true);
        strongNumbersSwitch.setFocusable(false);
        actionView.setOnClickListener(v -> strongNumbersSwitch.setChecked(!strongNumbersSwitch.isChecked()));
        strongNumbersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingStrongNumbersSwitch) {
                return;
            }
            setStrongNumbersEnabled(isChecked);
        });
        syncStrongNumbersSwitch();
    }

    private void syncHighlightsSwitch() {
        if (highlightsSwitch == null) {
            return;
        }
        if (highlightsSwitch.isChecked() == isHighlightsVisible) {
            return;
        }
        isUpdatingHighlightsSwitch = true;
        highlightsSwitch.setChecked(isHighlightsVisible);
        isUpdatingHighlightsSwitch = false;
    }

    private void syncNightModeSwitch() {
        if (nightModeSwitch == null) {
            return;
        }
        boolean enabled = presenter.isNightModeEnabled();
        if (nightModeSwitch.isChecked() == enabled) {
            return;
        }
        isUpdatingNightModeSwitch = true;
        nightModeSwitch.setChecked(enabled);
        isUpdatingNightModeSwitch = false;
    }

    private void syncFindInPageSwitch() {
        if (findInPageSwitch == null) {
            return;
        }
        boolean enabled = presenter.isFindInPageEnabled();
        if (findInPageSwitch.isChecked() == enabled) {
            return;
        }
        isUpdatingFindInPageSwitch = true;
        findInPageSwitch.setChecked(enabled);
        isUpdatingFindInPageSwitch = false;
    }

    private void syncStrongNumbersSwitch() {
        if (strongNumbersSwitch == null) {
            return;
        }
        boolean enabled = presenter.isStrongNumbersEnabled();
        if (strongNumbersSwitch.isChecked() == enabled) {
            return;
        }
        isUpdatingStrongNumbersSwitch = true;
        strongNumbersSwitch.setChecked(enabled);
        isUpdatingStrongNumbersSwitch = false;
    }

    private void setHighlightsVisible(boolean visible) {
        isHighlightsVisible = visible;
        readerView.setHighlightsVisible(visible);
        if (!visible) {
            clearPendingSelection();
        }
        syncHighlightsSwitch();
        analyticsHelper.clickEvent(visible ? "highlights_show" : "highlights_hide");
    }

    private void setFindInPageEnabled(boolean enabled) {
        presenter.setFindInPageEnabled(enabled);
        if (!enabled) {
            hideFindInPagePanel();
        }
        syncFindInPageSwitch();
        invalidateOptionsMenu();
        analyticsHelper.clickEvent(enabled ? "find_in_page_enabled" : "find_in_page_disabled");
    }

    private void setStrongNumbersEnabled(boolean enabled) {
        presenter.setStrongNumbersEnabled(enabled);
        syncStrongNumbersSwitch();
        analyticsHelper.clickEvent(enabled ? "strong_numbers_enabled" : "strong_numbers_disabled");
    }
}
