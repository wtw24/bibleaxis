/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.entity;

import java.io.Serializable;
import java.util.HashMap;

public class ItemList extends HashMap<String, String> implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String ID = "ID";
	public static final String Name = "Name";

	public ItemList(String id, String name) {
		super();
		super.put(ID, id);
		super.put(Name, name);
	}
}
