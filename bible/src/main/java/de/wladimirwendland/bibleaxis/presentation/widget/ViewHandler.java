/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.widget;

import android.os.Handler;
import android.os.Message;

import de.wladimirwendland.bibleaxis.presentation.ui.reader.IReaderViewListener;

import java.lang.ref.WeakReference;

class ViewHandler extends Handler {

    static final int MSG_OTHER = 1;
    static final int MSG_ON_CLICK_IMAGE = 2;
    static final int MSG_ON_TEXT_SELECTION = 3;
    static final int MSG_ON_CLICK_HIGHLIGHT = 4;

    private WeakReference<IReaderViewListener> weakListener;

    void setListener(IReaderViewListener listener) {
        this.weakListener = new WeakReference<>(listener);
    }

    @Override
    public void handleMessage(Message msg) {
        IReaderViewListener listener = weakListener.get();
        if (listener == null) {
            return;
        }

        switch (msg.what) {
            case MSG_ON_CLICK_IMAGE:
                listener.onReaderClickImage((String) msg.obj);
                break;
            case MSG_ON_TEXT_SELECTION:
                listener.onReaderTextSelection((String) msg.obj);
                break;
            case MSG_ON_CLICK_HIGHLIGHT:
                listener.onReaderClickHighlight((String) msg.obj);
                break;
            default:
                listener.onReaderViewChange((IReaderViewListener.ChangeCode) msg.obj);
        }
        super.handleMessage(msg);
    }
}
