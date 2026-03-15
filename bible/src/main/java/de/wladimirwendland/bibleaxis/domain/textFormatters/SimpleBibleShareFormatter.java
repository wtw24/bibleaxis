/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.textFormatters;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleBibleShareFormatter implements IShareTextFormatter {
	private LinkedHashMap<Integer, String> verses;

	public SimpleBibleShareFormatter(LinkedHashMap<Integer, String> verses) {
		this.verses = verses;
	}

	@Override
	public String format() {
		StringBuilder shareText = new StringBuilder();

		Integer prevVerseNumber = 0;
		for (Map.Entry<Integer, String> entry : verses.entrySet()) {
			if (prevVerseNumber == 0) {
				prevVerseNumber = entry.getKey();
			}

			if (entry.getKey() - prevVerseNumber > 1) {
				shareText.append(" ... ");
			} else if (shareText.length() != 0) {
				shareText.append(" ");
			}
			shareText.append(entry.getValue().trim());
			prevVerseNumber = entry.getKey();
		}

		return shareText.toString();
	}

}
