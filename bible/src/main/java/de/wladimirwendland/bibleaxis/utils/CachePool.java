/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class CachePool<T> extends LinkedHashMap<String, T> {
	private static final long serialVersionUID = -366362500655090729L;
	private static final int MAX_POOL_SIZE = 20;

	public CachePool() {
		super(MAX_POOL_SIZE, .75f, true);
	}

	public CachePool(int maxPoolSize) {
		super(maxPoolSize, .75f, true);
	}

	protected boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest) {
		return size() > MAX_POOL_SIZE;
	}
}
