package com.stiggpwnz.vibes.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class DiskUtils {

    public static File cacheDirNamed(Context context, String name) {
        File target = new File(getCacheDir(context), name);
        if (!target.exists()) {
            target.mkdirs();
        }
        return target;
    }

    public static File getCacheDir(Context context) {
        boolean mounted = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        return mounted ? context.getExternalCacheDir() : context.getCacheDir();
    }

    public static File getFilesDir(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return context.getExternalFilesDir(null);
        }
        return context.getFilesDir();
    }
}
