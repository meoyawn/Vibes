package com.stiggpwnz.vibes;

import java.io.File;
import java.io.IOException;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import android.content.Context;
import android.util.DisplayMetrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.stiggpwnz.vibes.vk.Vkontakte;

public class Singletons {

	public static final ObjectMapper JACKSON = new ObjectMapper();
	public static final Bus OTTO = new Bus(ThreadEnforcer.ANY);
	public static final int CACHE_SIZE = 20 * 1024 * 1024;
	public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

	public static Vkontakte vkontakte;
	private static DisplayMetrics displayMetrics;

	public static void init(Context context) throws IOException {
		displayMetrics = context.getResources().getDisplayMetrics();

		File cacheDir = VibesApplication.getCacheDir(context, "http");
		HTTP_CLIENT.setResponseCache(new HttpResponseCache(cacheDir, CACHE_SIZE));

		vkontakte = new RestAdapter.Builder().setClient(new OkClient(HTTP_CLIENT)).setServer(Vkontakte.SERVER).setConverter(new JacksonConverter(JACKSON))
				.setDebug(BuildConfig.DEBUG).build().create(Vkontakte.class);
	}

	public static int dpToPx(float dp) {
		return (int) ((dp * displayMetrics.density) + 0.5);
	}
}
