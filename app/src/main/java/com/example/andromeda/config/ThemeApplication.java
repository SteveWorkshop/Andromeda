package com.example.andromeda.config;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class ThemeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
