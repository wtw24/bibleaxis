/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.controller;

import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.exceptions.BibleAxisException;
import de.wladimirwendland.bibleaxis.domain.exceptions.TskNotFoundException;

import java.util.Set;

/**
 *
 */
public interface ITSKController {
    Set<BibleReference> getLinks(BibleReference reference) throws TskNotFoundException, BibleAxisException;
}
