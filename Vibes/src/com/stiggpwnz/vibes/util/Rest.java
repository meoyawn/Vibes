package com.stiggpwnz.vibes.util;

import java.io.IOException;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.BuildConfig;
import com.stiggpwnz.vibes.Vibes;
import com.stiggpwnz.vibes.util.Jackson.JacksonConverter;
import com.stiggpwnz.vibes.vk.Vkontakte;

public class Rest {

	private static class Holder {

		public static final int CACHE_SIZE = 5 * 1024 * 1024; // 20 MB

		public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
		public static final Vkontakte VKONTAKTE = new RestAdapter.Builder().setClient(new OkClient(HTTP_CLIENT)).setServer(Vkontakte.SERVER)
				.setConverter(new JacksonConverter(Jackson.getObjectMapper())).setDebug(BuildConfig.DEBUG).build().create(Vkontakte.class);

		static {
			try {
				HTTP_CLIENT.setResponseCache(new HttpResponseCache(Vibes.getCacheDir("http"), CACHE_SIZE));
			} catch (IOException e) {
				Log.e(e);
			}
		}
	}

	public static Vkontakte vkontakte() {
		return Holder.VKONTAKTE;
	}
}
