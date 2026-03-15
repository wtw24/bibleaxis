/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.controller;

import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public interface LibraryRepository {

    void add(BaseModule module);
    List<BaseModule> modules();
    void replace(Collection<BaseModule> modules);
}
