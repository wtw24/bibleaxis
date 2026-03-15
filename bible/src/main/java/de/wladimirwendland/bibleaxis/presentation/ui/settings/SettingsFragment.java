/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.annotation.Nullable;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseAppActivity;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        PreferenceHelper prefHelper = BibleAxisApp.getInstance().getPrefHelper();

        Preference historySize = findPreference("HistorySize");
        historySize.setOnPreferenceChangeListener((preference1, newValue1) -> {
            setHistorySummary(preference1, (String) newValue1);
            return true;
        });
        setHistorySummary(historySize, Integer.toString(prefHelper.getHistorySize()));

        Preference fontFamily = findPreference("font_family");
        fontFamily.setOnPreferenceChangeListener((preference, newValue) -> {
            setFontFamilySummary(preference, (String) newValue);
            return true;
        });
        setFontFamilySummary(fontFamily, prefHelper.getTextAppearance().getTypeface());

        Preference autoHideNavigationBar = findPreference("auto_hide_navigation_bar");
        if (autoHideNavigationBar != null) {
            autoHideNavigationBar.setOnPreferenceChangeListener((preference, newValue) -> {
                prefHelper.setAutoHideNavigationBarEnabled((Boolean) newValue);
                if (getActivity() instanceof BaseAppActivity) {
                    ((BaseAppActivity) getActivity()).refreshSystemBarsVisibility();
                }
                return true;
            });
        }
    }

    private void setFontFamilySummary(Preference fontFamily, String newValue) {
        String summary;
        if (newValue.equalsIgnoreCase("serif")) {
            summary = "Droid Serif";
        } else if (newValue.equalsIgnoreCase("monospace")) {
            summary = "Droid Sans Mono";
        } else {
            summary = "Droid Sans";
        }

        fontFamily.setSummary(summary);
    }

    private void setHistorySummary(Preference historySize, String value) {
        try {
            String summary = getResources().getString(R.string.category_reader_other_history_size_summary);
            historySize.setSummary(String.format(summary, value));
        } catch (NumberFormatException | Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }
}
