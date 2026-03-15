/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository;

import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;

import de.wladimirwendland.bibleaxis.domain.exceptions.BibleAxisException;
import de.wladimirwendland.bibleaxis.domain.exceptions.TskNotFoundException;
import de.wladimirwendland.bibleaxis.domain.repository.ITskRepository;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class XmlTskRepository implements ITskRepository {

	private static final String TAG = "XmlTskRepository";

	private static final String DOCUMENT = "tsk";
	private static final String BOOK = "book";
	private static final String CHAPTER = "chapter";
	private static final String VERSE = "verse";
	private final File mTskFile;

	public XmlTskRepository(File tskFile) {
		mTskFile = tskFile;
	}

	@Override
	public String getReferences(String book, String chapter, String verse) throws TskNotFoundException, BibleAxisException {

		String references = "";

		XmlPullParser parser;
		try {
			parser = getParser();
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.toString());
			throw new BibleAxisException("Unsupported encoding in cross-references file! " + e.getMessage());
		} catch (XmlPullParserException e) {
			Log.e(TAG, e.toString());
			throw new BibleAxisException("Error get data in cross-references! " + e.getMessage());
		}

		try {
			int eventType = parser.getEventType();
			boolean done = false;
			boolean bookFind = false;
			boolean chapterFind = false;
			while (eventType != XmlResourceParser.END_DOCUMENT && !done) {
				String name;
				switch (eventType) {
					case XmlResourceParser.START_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase(BOOK)) {
							if (parser.getAttributeCount() == 0) {
								break;
							}
							String value = parser.getAttributeValue(0);
							bookFind = value.equalsIgnoreCase(book);
						} else if (name.equalsIgnoreCase(CHAPTER) && bookFind) {
							if (parser.getAttributeCount() == 0) {
								break;
							}
							String value = parser.getAttributeValue(0);
							chapterFind = value.equalsIgnoreCase(chapter);
						} else if (name.equalsIgnoreCase(VERSE) && chapterFind) {
							if (parser.getAttributeCount() == 0) {
								break;
							}
							String value = parser.getAttributeValue(0);
							if (value.equalsIgnoreCase(verse)) {
								references = parser.nextText();
								done = true;
							}
						}
						break;
					case XmlResourceParser.END_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase(DOCUMENT)) {
							done = true;
						}
						break;
					default:
						// nothing
				}
				eventType = parser.next();
			}
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			throw new BibleAxisException("Error read data from cross-references! " + e.getMessage());
		} catch (XmlPullParserException e) {
			Log.e(TAG, e.toString());
			throw new BibleAxisException("Error get data in cross-references! " + e.getMessage());
		}

		return references;
	}

	private XmlPullParser getParser() throws XmlPullParserException, UnsupportedEncodingException, TskNotFoundException {
		InputStreamReader iReader;
		try {
			iReader = new InputStreamReader(new FileInputStream(mTskFile), StandardCharsets.UTF_8);
		} catch (FileNotFoundException e) {
			throw new TskNotFoundException();
		}
		BufferedReader buf = new BufferedReader(iReader);
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(buf);
		return parser;
	}
}
