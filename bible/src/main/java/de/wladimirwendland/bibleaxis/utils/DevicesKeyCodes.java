/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

import android.view.KeyEvent;

public final class DevicesKeyCodes {

	// additional key codes for Nook
	private static final int NOOK_KEY_PREV_LEFT = 96;
	private static final int NOOK_KEY_PREV_RIGHT = 98;
	private static final int NOOK_KEY_NEXT_RIGHT = 97;
	private static final int NOOK_KEY_SHIFT_UP = 101;
	private static final int NOOK_KEY_SHIFT_DOWN = 100;

	// nook 1 & 2
	private static final int NOOK_12_KEY_NEXT_LEFT = 95;

	// Nook touch buttons
	private static final int KEYCODE_PAGE_BOTTOMLEFT = 0x5d; // fwd = 93 (
	private static final int KEYCODE_PAGE_BOTTOMRIGHT = 158; // 0x5f; // fwd = 95
	private static final int KEYCODE_PAGE_TOPLEFT = 0x5c; // back = 92
	private static final int KEYCODE_PAGE_TOPRIGHT = 0x5e; // back = 94

	// Sony eReader 
	private static final int SONY_DPAD_UP_SCANCODE = 105;
	private static final int SONY_DPAD_DOWN_SCANCODE = 106;

	private DevicesKeyCodes() {
	}

	public static boolean keyCodeUp(int keyCode) {
		return keyCode == KeyEvent.KEYCODE_DPAD_UP
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_2
				|| keyCode == NOOK_KEY_PREV_LEFT
				|| keyCode == NOOK_KEY_PREV_RIGHT
				|| keyCode == NOOK_KEY_SHIFT_UP
				|| keyCode == KEYCODE_PAGE_BOTTOMLEFT
				|| keyCode == KEYCODE_PAGE_BOTTOMRIGHT
				|| keyCode == SONY_DPAD_UP_SCANCODE;
	}

	public static boolean keyCodeDown(int keyCode) {
		return keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				|| keyCode == KeyEvent.KEYCODE_8
				|| keyCode == NOOK_KEY_NEXT_RIGHT
				|| keyCode == NOOK_KEY_SHIFT_DOWN
				|| keyCode == NOOK_12_KEY_NEXT_LEFT
				|| keyCode == KEYCODE_PAGE_TOPLEFT
				|| keyCode == KEYCODE_PAGE_TOPRIGHT
				|| keyCode == SONY_DPAD_DOWN_SCANCODE;
	}
}
