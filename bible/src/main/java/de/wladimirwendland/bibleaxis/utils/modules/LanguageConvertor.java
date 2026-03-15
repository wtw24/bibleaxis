/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils.modules;

/**
 * User: Vladimir
 * Date: 10.02.13
 * Time: 0:35
 */
public final class LanguageConvertor {
	
	private LanguageConvertor() throws InstantiationException {
		throw new InstantiationException("This class is not for instantiation");
	}

	public static String getISOLanguage(String language) {
		language = language.toLowerCase();
		if (language.equals("русский") || language.equals("russian")) {
			return "ru_RU";
		} else if (language.equals("английский") || language.equals("english")) {
			return "en_US";
		} else if (language.equals("немецкий") || language.equals("deutsch") || language.equals("germany")) {
			return "de_DE";
		} else {
			return language;
		}
	}
}
