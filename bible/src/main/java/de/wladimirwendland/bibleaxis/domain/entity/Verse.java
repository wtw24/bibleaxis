/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.entity;

public class Verse {
	private Integer number;
	private String text;

	public Verse(Integer number, String text) {
		this.number = number;
		this.text = text;
	}

	public Integer getNumber() {
		return number;
	}

	public String getText() {
		return text;
	}
}
