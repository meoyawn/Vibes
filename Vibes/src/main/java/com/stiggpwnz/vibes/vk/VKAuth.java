package com.stiggpwnz.vibes.vk;

import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.BuildConfig;
import com.stiggpwnz.vibes.util.Persistence;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import de.devland.esperandro.SharedPreferenceActions;

@Singleton
public class VKAuth {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";

    static final String USER_ID    = "user_id";
    static final String EXPIRES_IN = "expires_in";
    static final String AUTH_URL   = "https://oauth.vk.com/authorize";
    static final int    CLIENT_ID  = 3027476;
    static final int    SCOPE      = 2 + 8 + 8192;

    public static String authUrl() {
        return String.format("%s?client_id=%d&scope=%s&redirect_uri=%s&display=touch&response_type=token",
                AUTH_URL, CLIENT_ID, SCOPE, REDIRECT_URL);
    }

    public static void checkThread() {
        if (BuildConfig.DEBUG) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                throw new RuntimeException("Wrong thread, buddy");
            }
        }
    }

    Lazy<OkHttpClient>      okHttpClientLazy;
    Lazy<CookieSyncManager> cookieSyncManagerLazy;
    Lazy<CookieManager>     cookieManagerLazy;
    Lazy<Persistence>       persistenceLazy;

    @Inject
    public VKAuth(Lazy<OkHttpClient> okHttpClientLazy,
                  Lazy<CookieSyncManager> cookieSyncManagerLazy,
                  Lazy<CookieManager> cookieManagerLazy,
                  Lazy<Persistence> persistenceLazy) {
        this.okHttpClientLazy = okHttpClientLazy;
        this.cookieSyncManagerLazy = cookieSyncManagerLazy;
        this.cookieManagerLazy = cookieManagerLazy;
        this.persistenceLazy = persistenceLazy;
    }

    void authRecursive(String url) throws IOException {
        HttpURLConnection connection = okHttpClientLazy.get().open(new URL(url));
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Cookie", cookieManagerLazy.get().getCookie(url));
        connection.connect();

        if (connection.getResponseCode() == 302) {
            cookieManagerLazy.get().setCookie(url, connection.getHeaderField("Set-Cookie"));
            String redirectUrl = connection.getHeaderField("Location");
            if (redirectUrl.startsWith(REDIRECT_URL)) {
                cookieSyncManagerLazy.get().sync();
                saveAuth(redirectUrl, System.currentTimeMillis());
            } else {
                authRecursive(redirectUrl);
            }
        }
    }

    public boolean saveAuth(String redirectUrl, long now) {
        checkThread();

        String[] params = redirectUrl.split("#")[1].split("&");
        Persistence persistence = persistenceLazy.get();
        for (String param : params) {
            String[] pairs = param.split("=");
            String value = pairs[1];

            switch (pairs[0]) {
                case ACCESS_TOKEN:
                    persistence.accessToken(value);
                    break;

                case EXPIRES_IN:
                    persistence.expiresIn(1000 * Long.valueOf(value) + now);
                    break;

                case USER_ID:
                    persistence.userId(Integer.valueOf(value));
                    break;

                default:
                    return false;
            }
        }
        return true;
    }

    public void resetAuth() {
        checkThread();

        SharedPreferenceActions actions = (SharedPreferenceActions) persistenceLazy.get();
        actions.remove("accessToken");
        actions.remove("expiresIn");
        actions.remove("userId");
    }

    public String getAccessToken(long now) throws IOException {
        checkThread();

        String accessToken = persistenceLazy.get().accessToken();
        if (accessToken == null || now >= persistenceLazy.get().expiresIn()) {
            authRecursive(authUrl());
            return getAccessToken(System.currentTimeMillis());
        }
        return accessToken;
    }
}
