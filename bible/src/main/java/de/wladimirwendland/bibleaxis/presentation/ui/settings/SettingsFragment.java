/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.ui.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.content.res.Resources;
import android.provider.DocumentsContract;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.R;
import de.wladimirwendland.bibleaxis.data.backup.HighlightsBackupManager;
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseAppActivity;
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper;

import java.text.DateFormat;
import java.util.Date;

public class SettingsFragment extends PreferenceFragment {

    private static final int REQUEST_CODE_PICK_HIGHLIGHTS_BACKUP = 701;
    private static final int REQUEST_CODE_PICK_BACKUP_STORAGE = 702;

    private PreferenceHelper prefHelper;
    private Preference restoreHighlightsBackup;
    private Preference chooseBackupStorage;
    private Preference lastBackupStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        prefHelper = BibleAxisApp.getInstance().getPrefHelper();

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

        chooseBackupStorage = findPreference("choose_highlights_backup_storage");
        if (chooseBackupStorage != null) {
            chooseBackupStorage.setOnPreferenceClickListener(preference -> {
                openStoragePicker();
                return true;
            });
        }

        lastBackupStatus = findPreference("highlights_last_backup_status");

        restoreHighlightsBackup = findPreference("restore_highlights_backup");
        if (restoreHighlightsBackup != null) {
            restoreHighlightsBackup.setOnPreferenceClickListener(preference -> {
                if (!hasBackupStorage()) {
                    Toast.makeText(getActivity(), R.string.highlights_restore_storage_required, Toast.LENGTH_LONG).show();
                    return true;
                }
                openBackupPicker();
                return true;
            });
        }

        updateBackupPreferencesState();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBackupPreferencesState();
    }

    private void openBackupPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/json");

        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_HIGHLIGHTS_BACKUP);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.highlights_restore_picker_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void openStoragePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_BACKUP_STORAGE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.modules_storage_picker_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }

        if (requestCode == REQUEST_CODE_PICK_BACKUP_STORAGE) {
            onStorageFolderPicked(data);
            return;
        }

        if (requestCode != REQUEST_CODE_PICK_HIGHLIGHTS_BACKUP) {
            return;
        }

        Uri backupUri = data.getData();
        if (backupUri == null) {
            Toast.makeText(getActivity(), R.string.highlights_restore_file_required, Toast.LENGTH_LONG).show();
            return;
        }

        BibleAxisApp app = BibleAxisApp.getInstance();
        Toast.makeText(getActivity(), R.string.highlights_restore_started, Toast.LENGTH_SHORT).show();
        app.getAppTaskRunner().runOnIo(() -> {
            HighlightsBackupManager.RestoreResult result = app.getHighlightsBackupManager().restoreFromUri(backupUri);
            app.getAppTaskRunner().runOnMain(() -> {
                if (!isAdded() || getActivity() == null) {
                    return;
                }

                if (result.failedCount > 0 && result.addedCount == 0) {
                    Toast.makeText(getActivity(), R.string.highlights_restore_failed, Toast.LENGTH_LONG).show();
                    return;
                }

                String message = getString(
                        R.string.highlights_restore_result,
                        result.addedCount,
                        result.skippedCount,
                        result.failedCount
                );
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            });
        });
    }

    private void onStorageFolderPicked(Intent data) {
        Uri treeUri = data.getData();
        if (treeUri == null) {
            Toast.makeText(getActivity(), R.string.modules_storage_location_required, Toast.LENGTH_LONG).show();
            return;
        }

        int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            activity.getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
        } catch (SecurityException ex) {
            Toast.makeText(getActivity(), R.string.modules_storage_access_denied, Toast.LENGTH_LONG).show();
            return;
        }

        prefHelper.setModulesSourceTreeUri(treeUri.toString());
        Toast.makeText(getActivity(), R.string.modules_storage_location_saved, Toast.LENGTH_SHORT).show();
        updateBackupPreferencesState();
    }

    private void updateBackupPreferencesState() {
        if (prefHelper == null) {
            return;
        }

        String storageUri = prefHelper.getModulesSourceTreeUri();
        boolean storageSelected = !TextUtils.isEmpty(storageUri);

        if (chooseBackupStorage != null) {
            chooseBackupStorage.setSummary(storageSelected
                    ? getStorageDisplayPath(storageUri)
                    : getString(R.string.highlights_backup_storage_not_selected));
        }

        if (restoreHighlightsBackup != null) {
            restoreHighlightsBackup.setEnabled(storageSelected);
            restoreHighlightsBackup.setSummary(storageSelected
                    ? getString(R.string.highlights_restore_summary)
                    : getString(R.string.highlights_restore_storage_required));
        }

        if (lastBackupStatus != null) {
            long lastBackupAt = prefHelper.getHighlightsLastBackupAt();
            if (lastBackupAt <= 0L) {
                lastBackupStatus.setSummary(getString(R.string.highlights_last_backup_never));
            } else {
                DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                lastBackupStatus.setSummary(format.format(new Date(lastBackupAt)));
            }
        }
    }

    private boolean hasBackupStorage() {
        return prefHelper != null && !TextUtils.isEmpty(prefHelper.getModulesSourceTreeUri());
    }

    private String getStorageDisplayPath(String uriText) {
        try {
            Uri uri = Uri.parse(uriText);
            String documentId = DocumentsContract.getTreeDocumentId(uri);
            if (!TextUtils.isEmpty(documentId)) {
                return formatTreeDocumentPath(documentId);
            }
        } catch (Exception ignored) {
        }
        return uriText;
    }

    private String formatTreeDocumentPath(String documentId) {
        int separator = documentId.indexOf(':');
        if (separator < 0) {
            return documentId;
        }

        String volumeId = documentId.substring(0, separator);
        String relativePath = separator < documentId.length() - 1
                ? documentId.substring(separator + 1)
                : "";

        String rootLabel;
        if ("primary".equalsIgnoreCase(volumeId)) {
            rootLabel = getString(R.string.highlights_storage_root_primary);
        } else {
            rootLabel = getString(R.string.highlights_storage_root_external, volumeId);
        }

        String backupRootPath = ensureBibleAxisPath(relativePath);
        if (TextUtils.isEmpty(backupRootPath)) {
            return rootLabel;
        }

        return rootLabel + "/" + backupRootPath;
    }

    private String ensureBibleAxisPath(String relativePath) {
        if (TextUtils.isEmpty(relativePath)) {
            return "BibleAxis";
        }

        String normalized = relativePath.replace('\\', '/');

        if (normalized.endsWith("/BibleAxis/modules")) {
            return normalized.substring(0, normalized.length() - "/modules".length());
        }

        if ("BibleAxis/modules".equalsIgnoreCase(normalized)) {
            return "BibleAxis";
        }

        if (normalized.endsWith("/BibleAxis") || "BibleAxis".equalsIgnoreCase(normalized)) {
            return normalized;
        }

        return normalized + "/BibleAxis";
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
