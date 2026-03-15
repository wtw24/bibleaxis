/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.entity;

public class Tag {

    public static final String KEY_ID = "_id";
    public static final String NAME = "name";

    public long id;
    public String name;

    public Tag(int id, String name) {
        this.id = id;
        this.name = name.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return name;
    }
}
