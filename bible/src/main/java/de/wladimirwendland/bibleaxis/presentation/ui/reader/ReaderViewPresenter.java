/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.reader;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.di.scope.PerActivity;
import de.wladimirwendland.bibleaxis.domain.AnalyticsHelper;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.entity.Highlight;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.domain.repository.IHighlightsRepository;
import de.wladimirwendland.bibleaxis.domain.threading.AppTaskRunner;
import de.wladimirwendland.bibleaxis.domain.textFormatters.ModuleTextFormatter;
import de.wladimirwendland.bibleaxis.managers.Librarian;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BasePresenter;
import de.wladimirwendland.bibleaxis.presentation.widget.Mode;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

import javax.inject.Inject;

import java.util.List;

@PerActivity
public class ReaderViewPresenter extends BasePresenter<ReaderView> {

    @NonNull
    private final AnalyticsHelper analyticsHelper;
    @NonNull
    private final Librarian librarian;
    @NonNull
    private final PreferenceHelper preferenceHelper;
    @NonNull
    private final IHighlightsRepository highlightsRepository;
    @NonNull
    private final AppTaskRunner appTaskRunner;

    @Inject
    ReaderViewPresenter(@NonNull Librarian librarian,
                        @NonNull PreferenceHelper prefHelper,
                        @NonNull IHighlightsRepository highlightsRepository,
                        @NonNull AppTaskRunner appTaskRunner,
                        @NonNull AnalyticsHelper helper) {
        this.librarian = librarian;
        this.preferenceHelper = prefHelper;
        this.highlightsRepository = highlightsRepository;
        this.appTaskRunner = appTaskRunner;
        this.analyticsHelper = helper;
    }

    @Override
    public void onViewCreated() {
        initView();
    }

    void inverseNightMode() {
        boolean nightMode = !preferenceHelper.getTextAppearance().isNightMode();
        setNightMode(nightMode);
    }

    void setNightMode(boolean nightMode) {
        preferenceHelper.setNightMode(nightMode);
        getViewAndExecute(view -> view.setTextAppearance(preferenceHelper.getTextAppearance()));
    }

    boolean isNightModeEnabled() {
        return preferenceHelper.getTextAppearance().isNightMode();
    }

    boolean isFindInPageEnabled() {
        return preferenceHelper.isFindInPageEnabled();
    }

    void setFindInPageEnabled(boolean enabled) {
        preferenceHelper.setFindInPageEnabled(enabled);
    }

    boolean isVolumeButtonsToScroll() {
        return preferenceHelper.volumeButtonsToScroll();
    }

    void nextChapter() {
        try {
            librarian.nextChapter();
            viewCurrentChapter();
        } catch (OpenModuleException e) {
            getViewAndExecute(view -> view.onOpenChapterFailure(e));
        }
    }

    void onChangeSettings() {
        initView();
        getViewAndExecute(ReaderView::updateContent);
    }

    void onPause() {
        librarian.setCurrentVerseNumber(getView().getCurrVerse());
    }

    void onResume() {
        getViewAndExecute(view -> {
            view.setKeepScreen(preferenceHelper.getBoolean("DisableTurnScreen"));
            view.setCurrentOrientation(preferenceHelper.getBoolean("DisableAutoScreenRotation"));
        });
    }

    void openLastLink() {
        openChapterFromLink(new BibleReference(preferenceHelper.getLastRead()));
    }

    void openLink(String link) {
        openChapterFromLink(new BibleReference(link));
    }

    void prevChapter() {
        try {
            librarian.prevChapter();
            viewCurrentChapter();
        } catch (OpenModuleException e) {
            getViewAndExecute(view -> view.onOpenChapterFailure(e));
        }
    }

    public void onClickChooseChapter() {
        getViewAndExecute(ReaderView::openLibraryActivity);
    }

    private void initView() {
        getViewAndExecute(view -> {
            view.setTextAppearance(preferenceHelper.getTextAppearance());
            view.setReaderMode(preferenceHelper.isReadModeByDefault() ? Mode.Read : Mode.Study);
            view.setKeepScreen(preferenceHelper.getBoolean("DisableTurnScreen"));
            view.setCurrentOrientation(preferenceHelper.getBoolean("DisableAutoScreenRotation"));
            view.updateActivityMode();
        });
    }

    private void openChapterFromLink(BibleReference osisLink) {
        if (!librarian.isOSISLinkValid(osisLink)) {
            getViewAndExecute(view -> view.openLibraryActivity());
            return;
        }

        analyticsHelper.moduleEvent(osisLink);
        getView().showProgress(false);
        appTaskRunner.runOnIo(() -> {
            try {
                ChapterData chapterData = new ChapterData(
                        librarian.openChapter(osisLink),
                        highlightsRepository.getByChapter(osisLink.getModuleID(), osisLink.getBookID(), osisLink.getChapter())
                );
                appTaskRunner.runOnMain(() -> getViewAndExecute(view -> {
                    BaseModule module = librarian.getCurrModule();
                    view.setTextFormatter(new ModuleTextFormatter(module, preferenceHelper));
                    view.setContent(
                            librarian.getBaseUrl(),
                            chapterData.chapter,
                            osisLink.getFromVerse(),
                            module.isBible(),
                            chapterData.highlights
                    );
                    view.setTitle(osisLink.getModuleID(), librarian.getHumanBookLink());
                    view.hideProgress();
                }));
            } catch (Throwable throwable) {
                appTaskRunner.runOnMain(() -> getViewAndExecute(view -> {
                    view.hideProgress();
                    view.onOpenChapterFailure(throwable);
                }));
            }
        });
    }

    private void viewCurrentChapter() {
        getViewAndExecute(ReaderView::disableActionMode);
        openChapterFromLink(librarian.getCurrentOSISLink());
    }

    private static final class ChapterData {
        private final Chapter chapter;
        private final List<Highlight> highlights;

        private ChapterData(Chapter chapter, List<Highlight> highlights) {
            this.chapter = chapter;
            this.highlights = highlights;
        }
    }
}
