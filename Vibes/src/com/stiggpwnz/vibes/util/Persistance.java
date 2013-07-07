package com.stiggpwnz.vibes.util;

import java.io.IOException;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.stiggpwnz.vibes.Vibes;
import com.stiggpwnz.vibes.vk.AuthException;
import com.stiggpwnz.vibes.vk.VKAuthenticator;

public class Persistance {

	private static final String ACCESS_TOKEN = "access_token";
	private static final String USER_ID = "user_id";
	private static final String EXPIRES_IN = "expires_in";

	private static class Holder {
		private static final SharedPreferences INSTANCE = PreferenceManager.getDefaultSharedPreferences(Vibes.getContext());
	}

	private static SharedPreferences getInstance() {
		return Holder.INSTANCE;
	}

	private static boolean checkAuth() {
		return getAccessToken() != null && System.currentTimeMillis() < Long.valueOf(getString(EXPIRES_IN, null));
	}

	public static boolean ensureAuth() throws IOException, AuthException {
		if (checkAuth()) {
			return true;
		}

		final Map<String, String> result = VKAuthenticator.auth();
		if (result != null) {
			saveVK(result);
			return true;
		}

		return false;
	}

	public static boolean resetVk() {
		return edit().putString(ACCESS_TOKEN, null).putString(EXPIRES_IN, null).putString(USER_ID, null).commit();
	}

	private static Editor edit() {
		return getInstance().edit();
	}

	public static boolean saveVK(Map<String, String> result) {
		if (result == null || !result.containsKey(ACCESS_TOKEN)) {
			return false;
		}

		long expires = Long.valueOf(result.get(EXPIRES_IN)) + System.currentTimeMillis();
		result.put(EXPIRES_IN, String.valueOf(expires));

		Editor editor = getInstance().edit();
		for (Map.Entry<String, String> entry : result.entrySet()) {
			editor.putString(entry.getKey(), entry.getValue());
		}
		return editor.commit();
	}

	private static String getString(String key, String defValue) {
		return getInstance().getString(key, defValue);
	}

	public static String getAccessToken() {
		return getString(ACCESS_TOKEN, null);
	}

	public static int getUserId() {
		return Integer.valueOf(getString(USER_ID, null));
	}
}
