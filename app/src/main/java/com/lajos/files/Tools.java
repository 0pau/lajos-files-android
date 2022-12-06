package com.lajos.files;

import java.text.DecimalFormat;
import java.util.Comparator;

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

    public static DecimalFormat df = new DecimalFormat("0.0");

    public static class Comparators {
        public static Comparator alphabetical_order = new Comparator<FileBrowserActivity.FileEntry>() {
            @Override
            public int compare(FileBrowserActivity.FileEntry f1, FileBrowserActivity.FileEntry f2) {
                return f1.toString().compareTo(f2.toString());
            }
        };
        public static Comparator folders_before_files = new Comparator<FileBrowserActivity.FileEntry>() {
            @Override
            public int compare(FileBrowserActivity.FileEntry f1, FileBrowserActivity.FileEntry f2) {

                if (f1.type == FileBrowserActivity.FileType.DIRECTORY && f2.type != FileBrowserActivity.FileType.DIRECTORY) {
                    return 0;
                } else if (f1.type != FileBrowserActivity.FileType.DIRECTORY && f2.type == FileBrowserActivity.FileType.DIRECTORY) {
                    return 1;
                }

                return 1;
            }
        };
    }

}
