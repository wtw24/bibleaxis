/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.widget.listview.itemview;

import de.wladimirwendland.bibleaxis.presentation.widget.listview.item.Item;

public interface ItemView {

	/**
     * Called by the {@link de.wladimirwendland.bibleaxis.presentation.widget.listview.ItemAdapter} the first time the ItemView is created.
     * This is usually a good time to keep references on sub-Views.
     */
    void prepareItemView();

	/**
     * Called by the {@link de.wladimirwendland.bibleaxis.presentation.widget.listview.ItemAdapter} whenever an ItemView is displayed on
     * screen. This may occur at the first display time or when the ItemView is
     * reused by the ListView.
     *
     * @param item The {@link de.wladimirwendland.bibleaxis.presentation.widget.listview.item.Item} containing date used to populate this
     *             ItemView
     */
    void setObject(Item item);

}
