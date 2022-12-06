package com.lajos.files;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.lang.Math;

public class Tools {

    public static double convertToGiB(long size) {

        return size / Math.pow(1024, 3);

    }

    public static String convertWithUnit(long size) {

        String unit = " B";
        double result = size;

        if (size > 1024) {
            unit = " KB";
            result = size / 1024;
        }
        if (size > Math.pow(1024, 2)) {
            unit = " MB";
            result = size / Math.pow(1024, 2);
        }
        if (size > Math.pow(1024, 3)) {
            unit = " GB";
            result = size / Math.pow(1024, 3);
        }
        if (size > Math.pow(1024, 4)) {
            unit = " TB";
            result = size / Math.pow(1024, 4);
        }

        return df.format(result) + unit;

    }

    public static String getVolumeNameForPath(Context c, String path) {
        StorageManager sm = (StorageManager) c.getSystemService(c.STORAGE_SERVICE);
        List<StorageVolume> devices = sm.getStorageVolumes();

        for (int i = 0; i < devices.size(); i++) {
            StorageVolume sv = devices.get(i);
            if (sv.getDirectory().getPath().compareTo(path) == 0) {
                return sv.getDescription(c);
            }
        }

        File f = new File(path);
        return f.getName();

    }

    public static boolean isPathRootOfVolume(Context c, String path) {
        StorageManager sm = (StorageManager) c.getSystemService(c.STORAGE_SERVICE);
        List<StorageVolume> devices = sm.getStorageVolumes();

        for (int i = 0; i < devices.size(); i++) {
            StorageVolume sv = devices.get(i);
            if (sv.getDirectory().getPath().compareTo(path) == 0) {
                return true;
            }
        }

        return false;
    }

    public static DecimalFormat df = new DecimalFormat("0.0");

    public static class Comparators {
        public static Comparator alphabetical_order = new Comparator<FileBrowserActivity.FileEntry>() {
            @Override
            public int compare(FileBrowserActivity.FileEntry f1, FileBrowserActivity.FileEntry f2) {
                return f1.toString().compareTo(f2.toString());
            }
        };
        public static Comparator date_order = new Comparator<FileBrowserActivity.FileEntry>() {
            @Override
            public int compare(FileBrowserActivity.FileEntry f1, FileBrowserActivity.FileEntry f2) {
                return f1.modified.compareTo(f2.modified);
            }
        };
        public static Comparator size_order = new Comparator<FileBrowserActivity.FileEntry>() {
            @Override
            public int compare(FileBrowserActivity.FileEntry f1, FileBrowserActivity.FileEntry f2) {
                Long s1 = f1.size;
                Long s2 = f2.size;
                return s1.compareTo(s2);
            }
        };
    }

    public static FileBrowserActivity.FileType getFileType(Context c, File file) {

        String type = null;
        final String url = file.toString();
        final String ext = MimeTypeMap.getFileExtensionFromUrl(url);

        if (ext != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        }
        if (type == null) {
            type = "?";
        }

        if (type.contains("image")) {
            return FileBrowserActivity.FileType.IMAGE;
        } else if (type.contains("audio")) {
            return FileBrowserActivity.FileType.AUDIO;
        } else if (type.contains("text")) {
            return FileBrowserActivity.FileType.TEXT;
        } else if (type.contains("android.package-archive") || url.endsWith(".apk")) {
            return FileBrowserActivity.FileType.APK;
        } else if (type.contains("zip") || url.endsWith(".zip") || url.endsWith(".7z")) {
            return FileBrowserActivity.FileType.ARCHIVE;
        } else if (type.contains("video") || url.endsWith(".mp4") || url.endsWith(".mkv") || url.endsWith(".3gp") || url.endsWith(".avi") || url.endsWith(".wmv") || url.endsWith(".mov")) {
            return FileBrowserActivity.FileType.VIDEO;
        } else if (url.endsWith("torrent")) {
            return FileBrowserActivity.FileType.TORRENT;
        }

        return FileBrowserActivity.FileType.MISC;

    };

}
