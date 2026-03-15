/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.entity;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class ModuleList extends ArrayList<BaseModule> {

    public ModuleList() {
        super();
    }

    public ModuleList(Collection<BaseModule> values) {
        super(values);
    }
}
