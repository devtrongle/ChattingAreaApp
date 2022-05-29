package com.example.chattingarea;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import timber.log.Timber;

public class ReleaseTree extends Timber.Tree{
    public static final String TAG = ReleaseTree.class.getSimpleName();

    @Override
    protected void log(int i, @Nullable String s, @NonNull String s1,
            @Nullable Throwable throwable) {

    }
}
