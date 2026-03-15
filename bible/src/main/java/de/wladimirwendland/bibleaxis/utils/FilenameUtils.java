/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import java.io.File;

public class FilenameUtils {

    public static String getFileName(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // DocumentProvider
            if (isExternalStorageDocument(uri)) {
                // ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                if (split.length < 2) {
                    return getDisplayName(context, uri);
                }
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                return getDisplayName(context, uri);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDisplayName(context, uri);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // File
            return new File(uri.getPath()).getName();
        }
        return null;
    }

    public static String getExtension(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        int dotPos = path.lastIndexOf(".");
        int dirPos = path.lastIndexOf("/");
        if (dirPos > dotPos || dotPos == -1) {
            return null;
        } else {
            return path.substring(dotPos + 1);
        }
    }

    private static String getDisplayName(Context context, Uri uri) {
        final String column = OpenableColumns.DISPLAY_NAME;
        final String[] projection = {column};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
}
