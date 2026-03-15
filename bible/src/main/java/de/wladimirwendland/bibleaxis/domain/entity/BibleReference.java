/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.entity;

import de.wladimirwendland.bibleaxis.entity.modules.BibleAxisModule;

public class BibleReference {

	private static final String MOD_DATASOURCE_FS = "fs";
	private static final String TAG = "LinkOSIS";
	private static final String SEP_GROUPS = "\\;";
	private static final String SEP_VALUES = "\\:";
	private static final String SEP_SHORT = "\\.";

	private String OSISLinkPath;
	private String moduleDatasource;
	private String moduleDatasourceID;
	private String moduleID;

	private String bookID = "Gen";
	private String bookName = "Genesis";
	private int chapterNumber = 1;
	private int fromVerse = 1;
	private int toVerse = 1;

	public BibleReference(String bibleLinkPath) {
		if (bibleLinkPath == null) {
			return;
		}

		String[] linkParam = bibleLinkPath.split(SEP_GROUPS);
		if (linkParam.length >= 5) {
            // BibleLinkPath extended path format = ds:fs;id:sd-card/mnt/biblequote/modules/rst;m:RST;b:MARK;ch:1;v:1
            try {
                this.moduleDatasource = linkParam[0].split(SEP_VALUES)[1];
                this.moduleDatasourceID = linkParam[1].split(SEP_VALUES)[1];
                this.moduleID = linkParam[2].split(SEP_VALUES)[1];
                this.bookID = linkParam[3].split(SEP_VALUES)[1];
                this.bookName = this.bookID;
                this.chapterNumber = 1;
                this.fromVerse = 1;
                try {
                    this.chapterNumber = Integer.parseInt(linkParam[4].split(SEP_VALUES)[1]);
                    this.fromVerse = Integer.parseInt(linkParam[5].split(SEP_VALUES)[1]);
                    this.toVerse = fromVerse;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
				e.printStackTrace();
			}

        } else {
            // BibleLinkPath short path format
            linkParam = bibleLinkPath.split(SEP_SHORT);
            if (linkParam.length >= 2) {
                this.moduleID = linkParam[0];
                this.bookID = linkParam[1];
                this.bookName = this.bookID;
                this.chapterNumber = 1;
                this.fromVerse = 1;
                try {
                    this.chapterNumber = linkParam.length >= 3 ? Integer.parseInt(linkParam[2]) : 1;
                    this.fromVerse = linkParam.length >= 4 ? Integer.parseInt(linkParam[3]) : 1;
                    this.toVerse = fromVerse;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	public BibleReference(String moduleDatasource, String moduleDatasourceID, String moduleID, String bookID, Integer chapterNumber, Integer verseNumber) {
		this.moduleDatasource = moduleDatasource;
		this.moduleDatasourceID = moduleDatasourceID;
		this.moduleID = moduleID;
		this.bookID = bookID;
		this.bookName = bookID;
		this.chapterNumber = chapterNumber;
		this.fromVerse = verseNumber;
		this.toVerse = verseNumber;
	}

	public BibleReference(BaseModule module, Book book, Integer chapterNumber, Integer verseNumber) {
		this(module, book, chapterNumber, verseNumber, verseNumber);
	}

	public BibleReference(BaseModule module, Book book, Integer chapterNumber, Integer fromVerse, Integer toVerse) {
		if (module instanceof BibleAxisModule) {
			this.moduleDatasource = MOD_DATASOURCE_FS;
		} else {
			this.moduleDatasource = null;
		}
		this.moduleDatasourceID = module == null ? null : module.getDataSourceID();
		this.moduleID = module == null ? null : module.getID();
		this.bookID = book == null ? null : book.getID();
		this.bookName = book == null ? null : book.getName();
		this.chapterNumber = chapterNumber;
		this.fromVerse = fromVerse;
		this.toVerse = toVerse;
	}

	public BibleReference(String moduleID, String bookID, int chapter, int verse) {
		this(moduleID, bookID, chapter, verse, verse);
	}

	public BibleReference(String moduleID, String bookID, int chapter, int fromVerse, int toVerse) {
		this.moduleID = moduleID;
		this.bookID = bookID;
		this.bookName = bookID;
		this.chapterNumber = chapter;
		this.fromVerse = fromVerse;
		this.toVerse = toVerse;
	}

	public String getPath() {
		if (OSISLinkPath == null) {
			if (moduleID == null || bookID == null) {
				OSISLinkPath = null;
			} else {
				OSISLinkPath = String.format("%1$s.%2$s.%3$s.%4$s", moduleID, bookID, chapterNumber, fromVerse);
			}
		}
		return OSISLinkPath;
	}

	public String getExtendedPath() {
		if (OSISLinkPath == null) {
			if (moduleID == null || bookID == null) {
				OSISLinkPath = null;
			} else {
				OSISLinkPath = String.format("ds:%1$s;id:%2$s;m:%3$s;b:%4$s;ch:%5$s;v:%6$s", moduleDatasource, moduleDatasourceID, moduleID, bookID, chapterNumber, fromVerse);
			}
		}
		return OSISLinkPath;
	}

	public String getChapterPath() {
		if (OSISLinkPath == null) {
			if (moduleID == null || bookID == null) {
				OSISLinkPath = null;
			} else {
				OSISLinkPath = String.format("%1$s.%2$s.%3$s", moduleID, bookID, chapterNumber);
			}
		}
		return OSISLinkPath;
	}

	public String getModuleDatasource() {
		return moduleDatasource;
	}

	public String getModuleDatasourceID() {
		return moduleDatasourceID;
	}

	public String getModuleID() {
		return moduleID;
	}

	public String getBookID() {
		return bookID;
	}

	public int getChapter() {
		return chapterNumber;
	}

	public int getFromVerse() {
		return fromVerse;
	}

	public int getToVerse() {
		return toVerse;
	}

	@Override
	public String toString() {
		return String.format("%1$s:%2$s %3$s:%4$s", moduleID, bookName, chapterNumber, fromVerse);
	}

	public String getBookFullName() {
		return bookName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (!(getClass() == obj.getClass())) {
			return false;
		} else {
			BibleReference tmp = (BibleReference) obj;
            return this.getPath().equals(tmp.getPath());
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
