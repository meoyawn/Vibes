package com.stiggpwnz.vibes;

import static com.stiggpwnz.vibes.fragments.LoginFragment.CLIENT_ID;
import static com.stiggpwnz.vibes.fragments.LoginFragment.SCOPE;
import static com.stiggpwnz.vkauth.VKAuthenticator.ACCESS_TOKEN;
import static com.stiggpwnz.vkauth.VKAuthenticator.ERROR;
import static com.stiggpwnz.vkauth.VKAuthenticator.EXPIRES_IN;
import static com.stiggpwnz.vkauth.VKAuthenticator.USER_ID;

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.stiggpwnz.vibes.vk.AuthException;
import com.stiggpwnz.vibes.vk.AuthException.Reason;
import com.stiggpwnz.vkauth.VKAuthenticator;

public class Persistance {

	private static final String PASSWORD = "password";
	private static final String EMAIL = "email";

	private static SharedPreferences prefs;

	public static void init(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static void ensureAuth(Context context) throws IOException, AuthException {
		if (checkAuth()) {
			return;
		}

		if (getPassword() == null) {
			throw new AuthException(Reason.MISSING_CREDENTIALS);
		}

		VKAuthenticator vkAuth = new VKAuthenticator(CLIENT_ID, SCOPE, Cookies.get(context));
		final Map<String, String> result = vkAuth.auth(getEmail(), getPassword());
		if (result != null) {
			if (result.containsKey(ACCESS_TOKEN)) {
				saveVK(result);
				Cookies.save(context, vkAuth.getCookieManager().getStore());
				Cookies.release();
				return;
			} else {
				if (result.get(ERROR).equals(VKAuthenticator.ERROR_INCORRECT_CREDENTIALS)) {
					saveEmailPassword(null, null);
					throw new AuthException(Reason.MISSING_CREDENTIALS);
				} else if (result.get(ERROR).equals(VKAuthenticator.ERROR_CAPTCHA_NEEDED)) {
					throw new AuthException(Reason.CAPTCHA_NEEDED);
				}
			}
		}
		throw new AuthException(Reason.UNKNOWN_FATAL);
	}

	public static boolean saveVK(Map<String, String> result) {
		if (result == null || !result.containsKey(ACCESS_TOKEN)) {
			return false;
		}

		long expires = Long.valueOf(result.get(EXPIRES_IN)) + System.currentTimeMillis();
		result.put(EXPIRES_IN, String.valueOf(expires));

		Editor editor = prefs.edit();
		for (Map.Entry<String, String> entry : result.entrySet()) {
			editor.putString(entry.getKey(), entry.getValue());
		}
		return editor.commit();
	}

	public static String getAccessToken() {
		return prefs.getString(ACCESS_TOKEN, null);
	}

	public static int getUserId() {
		return Integer.valueOf(prefs.getString(USER_ID, null));
	}

	public static boolean checkAuth() {
		return getAccessToken() != null && System.currentTimeMillis() < Long.valueOf(prefs.getString(EXPIRES_IN, null));
	}

	public static boolean saveEmailPassword(String email, String password) {
		return prefs.edit().putString(EMAIL, email).putString(PASSWORD, password).commit();
	}

	public static String getEmail() {
		return prefs.getString(EMAIL, null);
	}

	public static String getPassword() {
		return prefs.getString(PASSWORD, null);
	}

	public static class Cookies {

		private static final String COOKIES = "cookies";

		private static SharedPreferences cookieStorage;

		private static SharedPreferences getCookieStorage(Context context) {
			if (cookieStorage == null) {
				cookieStorage = context.getSharedPreferences(COOKIES, Context.MODE_PRIVATE);
			}
			return cookieStorage;
		}

		@SuppressWarnings("unchecked")
		public static Map<String, Map<String, Map<String, String>>> get(Context context) {
			String string = getCookieStorage(context).getString(COOKIES, null);
			if (string == null) {
				return null;
			}

			try {
				return Singletons.JACKSON.readValue(string, Map.class);
			} catch (Exception e) {
				return null;
			}
		}

		public static boolean save(Context context, Map<?, ?> cookies) {
			if (cookies == null) {
				return false;
			}

			try {
				String json = Singletons.JACKSON.writeValueAsString(cookies);
				boolean result = getCookieStorage(context).edit().putString(COOKIES, json).commit();
				return result;
			} catch (Exception e) {
				return false;
			}
		}

		public static void release() {
			cookieStorage = null;
		}
	}
}
