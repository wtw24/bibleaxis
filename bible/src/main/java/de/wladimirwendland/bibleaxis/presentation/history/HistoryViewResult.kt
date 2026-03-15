/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.history

import de.wladimirwendland.bibleaxis.entity.ItemList

sealed class HistoryViewResult {

    data class HistoryList(val list: List<ItemList>) : HistoryViewResult()
    data class OpenLink(val link: String) : HistoryViewResult()

}