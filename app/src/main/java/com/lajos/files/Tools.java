package com.lajos.files;

import java.text.DecimalFormat;

public class Tools {

    public static double convertToGiB(long size) {

        return size / Math.pow(1024, 3);

    }

    public static DecimalFormat df = new DecimalFormat("0.0");

}
