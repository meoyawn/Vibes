package com.stiggpwnz.vibes.util;

import java.io.File;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.stiggpwnz.vibes.Vibes;

public class Utils {

	private static class Holder {
		private static final DisplayMetrics DISPLAY_METRICS = Vibes.getContext().getResources().getDisplayMetrics();
		private static final ConnectivityManager CONNECTIVITY_MANAGER = (ConnectivityManager) Vibes.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public static int dpToPx(float dp) {
		return (int) ((dp * Holder.DISPLAY_METRICS.density) + 0.5);
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

	public static boolean isNetworkAvailable() {
		NetworkInfo netInfo = Holder.CONNECTIVITY_MANAGER.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}
