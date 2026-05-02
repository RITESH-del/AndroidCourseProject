package com.example.snackstream;

import android.app.Application;

import com.cloudinary.android.MediaManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MediaManager.init(this);
    }
}