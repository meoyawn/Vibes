package com.stiggpwnz.vibes.util;

import java.io.File;

import android.os.Environment;
import android.util.DisplayMetrics;

import com.stiggpwnz.vibes.Vibes;

public class Utils {

	private static class Holder {
		private static final DisplayMetrics INSTANCE = Vibes.getContext().getResources().getDisplayMetrics();
	}

	public static int dpToPx(float dp) {
		return (int) ((dp * Holder.INSTANCE.density) + 0.5);
	}

	public static File getCacheDir(String name) {
		File cacheDir = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ? Vibes.getContext().getExternalCacheDir() : Vibes.getContext()
				.getCacheDir();
		File target = new File(cacheDir, name);
		if (!target.exists()) {
			target.mkdirs();
		}
		return target;
	}
}
