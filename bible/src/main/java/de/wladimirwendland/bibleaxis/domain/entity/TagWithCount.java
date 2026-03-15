/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.entity;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class TagWithCount {

    public abstract Tag tag();

    public abstract String count();

    public static TagWithCount create(Tag tag, String count) {
        return new AutoValue_TagWithCount(tag, count);
    }

    public static List<TagWithCount> create(Map<Tag, String> tags) {
        List<TagWithCount> result = new ArrayList<>();
        for (Map.Entry<Tag, String> entry : tags.entrySet()) {
            result.add(create(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
