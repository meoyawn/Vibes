package com.stiggpwnz.vibes.util;

import java.io.IOException;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.BuildConfig;
import com.stiggpwnz.vibes.util.Jackson.JacksonConverter;
import com.stiggpwnz.vibes.vk.VKApi;

public class Rest {

	private static class Holder {

		public static final int CACHE_SIZE = 5 * 1024 * 1024; // 20 MB

		public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
		public static final VKApi VKONTAKTE = new RestAdapter.Builder().setClient(new OkClient(HTTP_CLIENT)).setServer(VKApi.SERVER)
				.setConverter(new JacksonConverter(Jackson.getObjectMapper())).setDebug(BuildConfig.DEBUG).build().create(VKApi.class);

		static {
			try {
				HTTP_CLIENT.setResponseCache(new HttpResponseCache(Utils.getCacheDir("http"), CACHE_SIZE));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static VKApi vkontakte() {
		return Holder.VKONTAKTE;
	}

	public static OkHttpClient getHttpClient() {
		return Holder.HTTP_CLIENT;
	}
}
