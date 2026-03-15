/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.dal.repository;

import android.content.Context;
import android.util.Log;

import de.wladimirwendland.bibleaxis.domain.exceptions.DataAccessException;
import de.wladimirwendland.bibleaxis.domain.repository.IHistoryRepository;
import de.wladimirwendland.bibleaxis.entity.ItemList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

public class FsHistoryRepository implements IHistoryRepository {

    private static final String TAG = FsHistoryRepository.class.getSimpleName();
    private static final String historyFileName = "history.dat";

    private File dirPath;

    public FsHistoryRepository(Context context) {
        this.dirPath = context.getExternalCacheDir();
        if (this.dirPath == null || !dirPath.exists()) {
            this.dirPath = context.getCacheDir();
        }
    }

	public void save(LinkedList<ItemList> list) {
		try {
			FileOutputStream fStr = new FileOutputStream(new File(dirPath, historyFileName));
			ObjectOutputStream out = new ObjectOutputStream(fStr);
			out.writeObject(list);
			out.close();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	@SuppressWarnings("unchecked")
	public LinkedList<ItemList> load() throws DataAccessException {
		try {
			FileInputStream fStr = new FileInputStream(new File(dirPath, historyFileName));
			ObjectInputStream out = new ObjectInputStream(fStr);
			LinkedList<ItemList> list = (LinkedList<ItemList>) out.readObject();
			out.close();
			return list;
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.toString());
			throw new DataAccessException(e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			throw new DataAccessException(e.getMessage());
		}
	}
}
