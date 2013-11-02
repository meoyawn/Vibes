package com.stiggpwnz.vibes.util;

import com.roadtrippers.BuildConfig;

import java.util.Locale;

public class Log {

    private static final String TAG = "roadtrippers";

    public static void d(String msg) {
        if (BuildConfig.DEBUG && msg != null) {
            android.util.Log.d(TAG, msg);
        }
    }

    public static void d(Throwable e) {
        if (BuildConfig.DEBUG && e != null) {
            android.util.Log.d(TAG, e.getMessage(), e);
        }
    }

    public static void d(String format, Object... args) {
        if (BuildConfig.DEBUG && format != null && args != null) {
            android.util.Log.d(TAG, String.format(Locale.ENGLISH, format, args));
        }
    }

    public static void e(String msg) {
        if (BuildConfig.DEBUG && msg != null) {
            android.util.Log.e(TAG, msg);
        }
    }

    public static void e(Throwable e) {
        if (BuildConfig.DEBUG && e != null) {
            android.util.Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void e(String format, Object... args) {
        if (BuildConfig.DEBUG && format != null && args != null) {
            android.util.Log.e(TAG, String.format(Locale.ENGLISH, format, args));
        }
    }
}
