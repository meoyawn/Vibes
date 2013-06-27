package com.stiggpwnz.vibes.util;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.stiggpwnz.vibes.VibesApplication;

public class Cookies {

	private static final String COOKIES = "cookies";

	private static class Holder {
		private static final SharedPreferences INSTANCE = VibesApplication.getContext().getSharedPreferences(COOKIES, Context.MODE_PRIVATE);
	}

	private static SharedPreferences getInstance() {
		return Holder.INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Map<String, Map<String, String>>> get() {
		String string = getInstance().getString(COOKIES, null);
		if (string == null) {
			return null;
		}

		try {
			return Singletons.JACKSON.readValue(string, Map.class);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean save(Map<?, ?> cookies) {
		if (cookies == null) {
			return false;
		}

		try {
			String json = Singletons.JACKSON.writeValueAsString(cookies);
			boolean result = getInstance().edit().putString(COOKIES, json).commit();
			return result;
		} catch (Exception e) {
			return false;
		}
	}
}