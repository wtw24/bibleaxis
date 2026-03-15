/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.entity;

import com.google.auto.value.AutoValue;

/**
 */

@AutoValue
public abstract class TextAppearance {

    public abstract String getBackground();
    public abstract int getLineSpacing();
    public abstract String getSelectedBackgroung();
    public abstract String getSelectedTextColor();
    public abstract String getTextAlign();
    public abstract String getTextColor();
    public abstract String getTextSize();
    public abstract String getTypeface();
    public abstract boolean isNightMode();

    public static Builder builder() {
        return new AutoValue_TextAppearance.Builder();
    }

    @SuppressWarnings("WeakerAccess")
    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder background(String value);
        public abstract Builder lineSpacing(int value);
        public abstract Builder selectedBackgroung(String value);
        public abstract Builder selectedTextColor(String value);
        public abstract Builder textAlign(String value);
        public abstract Builder textColor(String value);
        public abstract Builder textSize(String value);
        public abstract Builder typeface(String value);
        public abstract Builder nightMode(boolean value);
        public abstract TextAppearance build();
    }
}
