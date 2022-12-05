package com.lajos.files;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class FilesApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
