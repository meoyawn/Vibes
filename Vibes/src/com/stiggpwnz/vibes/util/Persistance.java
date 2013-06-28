package com.stiggpwnz.vibes.util;

import static com.stiggpwnz.vibes.fragments.LoginFragment.CLIENT_ID;
import static com.stiggpwnz.vibes.fragments.LoginFragment.SCOPE;
import static com.stiggpwnz.vkauth.VKAuthenticator.ACCESS_TOKEN;
import static com.stiggpwnz.vkauth.VKAuthenticator.ERROR;
import static com.stiggpwnz.vkauth.VKAuthenticator.EXPIRES_IN;
import static com.stiggpwnz.vkauth.VKAuthenticator.USER_ID;

import java.io.IOException;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.stiggpwnz.vibes.Vibes;
import com.stiggpwnz.vibes.vk.AuthException;
import com.stiggpwnz.vibes.vk.AuthException.Reason;
import com.stiggpwnz.vkauth.VKAuthenticator;

public class Persistance {

	private static final String PASSWORD = "password";
	private static final String EMAIL = "email";

	private static class Holder {
		private static final SharedPreferences INSTANCE = PreferenceManager.getDefaultSharedPreferences(Vibes.getContext());
	}

	private static SharedPreferences getInstance() {
		return Holder.INSTANCE;
	}

	public static void ensureAuth() throws IOException, AuthException {
		if (checkAuth()) {
			return;
		}

		if (getPassword() == null) {
			throw new AuthException(Reason.MISSING_CREDENTIALS);
		}

		VKAuthenticator vkAuth = new VKAuthenticator(CLIENT_ID, SCOPE, Cookies.get());
		final Map<String, String> result = vkAuth.auth(getEmail(), getPassword());
		if (result != null) {
			if (result.containsKey(ACCESS_TOKEN)) {
				saveVK(result);
				Cookies.save(vkAuth.getCookieManager().getStore());
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

		Editor editor = getInstance().edit();
		for (Map.Entry<String, String> entry : result.entrySet()) {
			editor.putString(entry.getKey(), entry.getValue());
		}
		return editor.commit();
	}

	public static String getAccessToken() {
		return getString(ACCESS_TOKEN, null);
	}

	public static int getUserId() {
		return Integer.valueOf(getString(USER_ID, null));
	}

	public static boolean checkAuth() {
		return getAccessToken() != null && System.currentTimeMillis() < Long.valueOf(getString(EXPIRES_IN, null));
	}

	public static boolean saveEmailPassword(String email, String password) {
		return getInstance().edit().putString(EMAIL, email).putString(PASSWORD, password).commit();
	}

	public static String getEmail() {
		return getString(EMAIL, null);
	}

	public static String getPassword() {
		return getString(PASSWORD, null);
	}

	private static String getString(String key, String defValue) {
		return getString(key, defValue);
	}
}
