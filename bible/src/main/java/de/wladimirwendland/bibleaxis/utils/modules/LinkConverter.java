/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils.modules;

import de.wladimirwendland.bibleaxis.BibleAxisApp;
import de.wladimirwendland.bibleaxis.domain.controller.ILibraryController;
import de.wladimirwendland.bibleaxis.domain.entity.BaseModule;
import de.wladimirwendland.bibleaxis.domain.entity.BibleReference;
import de.wladimirwendland.bibleaxis.domain.entity.Book;
import de.wladimirwendland.bibleaxis.domain.exceptions.BookNotFoundException;
import de.wladimirwendland.bibleaxis.domain.exceptions.OpenModuleException;
import de.wladimirwendland.bibleaxis.managers.BibleBooksID;
import de.wladimirwendland.bibleaxis.managers.Librarian;

public final class LinkConverter {

	private LinkConverter() throws InstantiationException {
		throw new InstantiationException("This class is not for instantiation");
	}

	public static String getHumanToOSIS(String humanLink) {
		// Получим имя модуля
		int position = humanLink.indexOf(':');
		if (position == -1) {
			return "";
		}
		String linkOSIS = humanLink.substring(0, position).trim();
		humanLink = humanLink.substring(position + 1).trim();
		if (humanLink.isEmpty()) {
			return "";
		}

		// Получим имя книги
		position = humanLink.indexOf(' ');
		if (position == -1) {
			return "";
		}
		linkOSIS += "." + BibleBooksID.getID(humanLink.substring(0, position).trim());
		humanLink = humanLink.substring(position).trim();
		if (humanLink.isEmpty()) {
			return linkOSIS + ".1";
		}

		// Получим номер главы
		position = humanLink.indexOf(':');
		if (position == -1) {
			return "";
		}
		linkOSIS += "." + humanLink.substring(0, position).trim().replaceAll("\\D", "");
		humanLink = humanLink.substring(position).trim().replaceAll("\\D", "");
		if (humanLink.isEmpty()) {
			return linkOSIS;
		} else {
			// Оставшийся кусок - номер стиха
			return linkOSIS + "." + humanLink;
		}
	}

	public static String getOSIStoHuman(BibleReference reference) {
		if (reference.getFromVerse() != reference.getToVerse()) {
			return String.format("%1$s %2$s:%3$s-%4$s",
					reference.getBookFullName(), reference.getChapter(),
					reference.getFromVerse(), reference.getToVerse());
		} else {
			return String.format("%1$s %2$s:%3$s",
					reference.getBookFullName(), reference.getChapter(), reference.getFromVerse());
		}
	}

	public static String getOSIStoHuman(String linkOSIS, Librarian librarian) throws BookNotFoundException, OpenModuleException {
		String[] param = linkOSIS.split("\\.");
		if (param.length < 3) {
			return "";
		}

		String moduleID = param[0];
		String bookID = param[1];
		String chapter = param[2];

        BaseModule currModule;
        try {
            final ILibraryController libCtrl = BibleAxisApp.getInstance().getLibraryController();
            currModule = libCtrl.getModuleByID(moduleID);
		} catch (OpenModuleException e) {
			return "";
		}
		Book currBook = librarian.getBookByID(currModule, bookID);
		if (currBook == null) {
			return "";
		}

		String humanLink = moduleID + ": " + currBook.getShortName() + " " + chapter;
		if (param.length > 3) {
			humanLink += ":" + param[3];
		}

		return humanLink;
	}

}
