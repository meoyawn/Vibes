package com.stiggpwnz.vibes.vk;

import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.util.Persistence;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import de.devland.esperandro.SharedPreferenceActions;
import lombok.RequiredArgsConstructor;
import retrofit.RequestInterceptor;
import rx.Observable;
import rx.Subscriber;

@RequiredArgsConstructor(suppressConstructorProperties = true)
public class VKAuth implements RequestInterceptor {
    public static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";
    static final        String AUTH_URL     = "https://oauth.vk.com/authorize";

    static final String ACCESS_TOKEN = "access_token";
    static final String USER_ID      = "user_id";
    static final String EXPIRES_IN   = "expires_in";

    static final int CLIENT_ID = 3027476;
    static final int SCOPE     = 2 + 8 + 8192;

    final OkHttpClient      okHttpClient;
    final CookieSyncManager cookieSyncManager;
    final CookieManager     cookieManager;
    final Persistence       persistence;

    public static String authUrl() {
        return String.format(Locale.US,
                "%s?client_id=%d&scope=%s&redirect_uri=%s&display=touch&response_type=token",
                AUTH_URL, CLIENT_ID, SCOPE, REDIRECT_URL);
    }

    public static void assertBgThread() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new RuntimeException("Wrong thread, buddy");
        }
    }

    String authTailRecursive(String url) throws Exception {
        while (true) {
            HttpURLConnection connection = okHttpClient.open(new URL(url));
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Cookie", cookieManager.getCookie(url));
            connection.connect();

            if (connection.getResponseCode() == 302) {
                cookieManager.setCookie(url, connection.getHeaderField("Set-Cookie"));
                String redirectUrl = connection.getHeaderField("Location");
                if (redirectUrl.startsWith(REDIRECT_URL)) {
                    cookieSyncManager.sync();
                    return saveAndGetAccessToken(redirectUrl, System.currentTimeMillis());
                } else {
                    url = redirectUrl;
                }
            } else {
                throw new Exception("auth flow incomplete");
            }
        }
    }

    public Observable<String> saveAccessToken(String redirectUrl, long now) {
        return Observable.create((Subscriber<? super String> subscriber) -> {
            subscriber.onNext(saveAndGetAccessToken(redirectUrl, now));
            subscriber.onCompleted();
        });
    }

    private String saveAndGetAccessToken(String redirectUrl, long now) {
        assertBgThread();

        // not extracted because of the very rare use
        @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
        String[] params = redirectUrl.split("#")[1].split("&");

        for (String param : params) {
            @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
            String[] pairs = param.split("=");

            String value = pairs[1];
            switch (pairs[0]) {
                case ACCESS_TOKEN:
                    persistence.accessToken(value);
                    break;

                case EXPIRES_IN:
                    persistence.expiresIn((1000 * Long.valueOf(value)) + now);
                    break;

                case USER_ID:
                    persistence.userId(Integer.valueOf(value));
                    break;

                default:
                    throw new RuntimeException("redirected to a wrong url");
            }
        }
        return persistence.accessToken();
    }

    public void resetAuth() {
        assertBgThread();

        SharedPreferenceActions actions = (SharedPreferenceActions) persistence;
        actions.remove("accessToken");
        actions.remove("expiresIn");
        actions.remove("userId");
    }

    String getAccessToken(long now) throws Exception {
        assertBgThread();

        String accessToken = persistence.accessToken();
        if (accessToken == null || now >= persistence.expiresIn()) {
            return authTailRecursive(authUrl());
        }
        return accessToken;
    }

    @Override
    public void intercept(RequestFacade request) {
        try {
            request.addQueryParam(ACCESS_TOKEN, getAccessToken(System.currentTimeMillis()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }
}
