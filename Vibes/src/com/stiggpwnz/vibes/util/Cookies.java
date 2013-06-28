package com.stiggpwnz.vibes.util;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.stiggpwnz.vibes.Vibes;

public class Cookies {

	private static final String COOKIES = "cookies";

	private static class Holder {
		private static final SharedPreferences INSTANCE = Vibes.getContext().getSharedPreferences(COOKIES, Context.MODE_PRIVATE);
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
			return Jackson.getObjectMapper().readValue(string, Map.class);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean save(Map<?, ?> cookies) {
		if (cookies == null) {
			return false;
		}

		try {
			String json = Jackson.getObjectMapper().writeValueAsString(cookies);
			boolean result = getInstance().edit().putString(COOKIES, json).commit();
			return result;
		} catch (Exception e) {
			return false;
		}
	}
}