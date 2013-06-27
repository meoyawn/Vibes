package com.stiggpwnz.vibes;

import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class Log {

	private static final String TAG = "vibes";
	private static final Handler HANDLER = new Handler(Looper.getMainLooper());

	public static void d(String msg) {
		if (BuildConfig.DEBUG && msg != null) {
			android.util.Log.d(TAG, msg);
		}
	}

	public static void d(String msg, long time) {
		if (BuildConfig.DEBUG && msg != null) {
			d(String.format(Locale.ENGLISH, "%s in %d ms", msg, System.currentTimeMillis() - time));
		}
	}

	public static void d(Throwable e) {
		if (BuildConfig.DEBUG && e != null) {
			android.util.Log.d(TAG, e.getMessage(), e);
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

	public static void toast(final Context context, final String msg) {
		if (BuildConfig.DEBUG && msg != null) {
			HANDLER.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
