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
        return externalIsMounted() ?
                context.getExternalCacheDir() :
                context.getCacheDir();
    }

    private static boolean externalIsMounted() {return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());}

    public static File getFilesDir(Context context) {
        if (externalIsMounted()) {
            return context.getExternalFilesDir(null);
        }
        return context.getFilesDir();
    }
}
