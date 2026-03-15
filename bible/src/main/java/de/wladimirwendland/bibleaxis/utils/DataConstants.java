/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;

public final class DataConstants {

    private static final String MODULE_DIR_NAME = "modules";

    private DataConstants() {
    }

    @NonNull
    public static File getLibraryPath(@NonNull Context context) {
        return new File(context.getFilesDir(), MODULE_DIR_NAME);
    }
}
