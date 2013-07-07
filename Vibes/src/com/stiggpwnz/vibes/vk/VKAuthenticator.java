package com.stiggpwnz.vibes.vk;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.stiggpwnz.vibes.util.Rest;

public class VKAuthenticator {

	public static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";

	private static final String AUTH_URL = "https://oauth.vk.com/authorize";

	private static final int CLIENT_ID = 3027476;
	private static final int SCOPE = 2 + 8 + 8192;

	public static Map<String, String> auth() throws IOException {
		return auth(authUrl());
	}

	public static String authUrl() {
		return String.format("%s?client_id=%s&scope=%s&redirect_uri=%s&display=touch&response_type=token", AUTH_URL, CLIENT_ID, SCOPE, REDIRECT_URL);
	}

	private static Map<String, String> auth(String url) throws IOException {
		CookieManager cookieManager = CookieManager.getInstance();

		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection connection = Rest.getHttpClient().open(new URL(url));
		connection.setRequestProperty("Cookie", cookieManager.getCookie(url));
		connection.connect();

		if (connection.getResponseCode() == 302) {
			cookieManager.setCookie(url, connection.getHeaderField("Set-Cookie"));

			String redirect = connection.getHeaderField("Location");

			if (redirect.startsWith(REDIRECT_URL)) {
				CookieSyncManager.getInstance().sync();
				return parseRedirectUrl(redirect);
			}

			return auth(redirect);
		}
		return null;
	}

	public static Map<String, String> parseRedirectUrl(String redirect) {
		String[] params = redirect.split("#")[1].split("&");
		Map<String, String> result = new HashMap<String, String>();
		for (String param : params) {
			String[] pairs = param.split("=");
			result.put(pairs[0], pairs[1]);
		}
		return result;
	}
}