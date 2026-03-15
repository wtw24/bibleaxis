/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.managers;

import de.wladimirwendland.bibleaxis.dal.controller.BibleAxisModuleController;
import de.wladimirwendland.bibleaxis.dal.repository.BibleAxisModuleRepository;
import de.wladimirwendland.bibleaxis.domain.controller.IModuleController;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.entity.modules.BibleAxisModule;
import de.wladimirwendland.bibleaxis.utils.FsUtilsWrapper;

/**
 *
 */
final class Injector {

    static IModuleController getModuleController(BaseModule module) {
        if (module instanceof BibleAxisModule) {
            return new BibleAxisModuleController((BibleAxisModule) module, new BibleAxisModuleRepository(new FsUtilsWrapper()));
        }
        return null;
    }
}
