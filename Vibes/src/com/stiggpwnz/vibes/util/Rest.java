package com.stiggpwnz.vibes.util;

import java.io.IOException;

import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;

public class Rest {

	private static class Holder {

		public static final int CACHE_SIZE = 5 * 1024 * 1024; // 5 MB

		public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

		static {
			try {
				HTTP_CLIENT.setResponseCache(new HttpResponseCache(Utils.getCacheDir("http"), CACHE_SIZE));
			} catch (IOException e) {
				Log.e(e);
			}
		}
	}

	public static OkHttpClient getHttpClient() {
		return Holder.HTTP_CLIENT;
	}
}
