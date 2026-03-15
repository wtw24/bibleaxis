/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.search;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.repository.IModuleRepository;

import java.io.File;

class MockModuleRepository implements IModuleRepository<BaseModule> {

    @Override
    public Bitmap getBitmap(BaseModule module, String path) {
        return null;
    }

    @Override
    public Chapter loadChapter(BaseModule module, String bookID, int chapter) {
        return null;
    }

    @Override
    public BaseModule loadModule(File path) {
        return null;
    }

    @NonNull
    @Override
    public String getBookContent(BaseModule module, String bookID) {
        return "";
    }
}
