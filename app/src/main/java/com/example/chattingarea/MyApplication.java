package com.example.chattingarea;

import android.app.Application;

import timber.log.Timber;


public class MyApplication extends Application {
    public static final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Timber.plant(new ReleaseTree());
        }
    }
}
