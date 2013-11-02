package com.stiggpwnz.vibes.vk;

import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.util.Persistence;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;

@Singleton
public class VKontakte {

    public static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";

    static final String AUTH_URL  = "https://oauth.vk.com/authorize";
    static final int    CLIENT_ID = 3027476;
    static final int    SCOPE     = 2 + 8 + 8192;

    Lazy<Persistence>  persistenceLazy;
    Lazy<OkHttpClient> okHttpClientLazy;

    @Inject
    public VKontakte(Lazy<OkHttpClient> okHttpClientLazy, Lazy<Persistence> persistenceLazy) {
        this.persistenceLazy = persistenceLazy;
        this.okHttpClientLazy = okHttpClientLazy;
    }

    public Map<String, String> auth() throws IOException {
        return authRecursive(authUrl());
    }

    public static String authUrl() {
        return String.format("%s?client_id=%s&scope=%s&redirect_uri=%s&display=touch&response_type=token", AUTH_URL, CLIENT_ID, SCOPE, REDIRECT_URL);
    }

    private Map<String, String> authRecursive(String url) throws IOException {
        HttpURLConnection connection = okHttpClientLazy.get().open(new URL(url));
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Cookie", persistenceLazy.get().getCookie());
        connection.connect();

        if (connection.getResponseCode() == 302) {
            persistenceLazy.get().saveCookie(connection.getHeaderField("Set-Cookie"));
            String redirectUrl = connection.getHeaderField("Location");
            if (redirectUrl.startsWith(REDIRECT_URL)) {
                return parseRedirectUrl(redirectUrl);
            }
            return authRecursive(redirectUrl);
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
