package com.stiggpwnz.vibes.vk;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Result;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;

@Singleton
public class VKontakte {

    public static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";

    static final String AUTH_URL  = "https://oauth.vk.com/authorize";
    static final int    CLIENT_ID = 3027476;
    static final int    SCOPE     = 2 + 8 + 8192;

    Lazy<Persistence>  persistenceLazy;
    Lazy<OkHttpClient> okHttpClientLazy;
    Lazy<VKApi>        vkApiLazy;
    Context            context;

    @Inject
    public VKontakte(Lazy<OkHttpClient> okHttpClientLazy, Lazy<Persistence> persistenceLazy, Lazy<VKApi> vkApiLazy, Context context) {
        this.persistenceLazy = persistenceLazy;
        this.okHttpClientLazy = okHttpClientLazy;
        this.vkApiLazy = vkApiLazy;
        this.context = context;
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
        connection.setRequestProperty("Cookie", CookieManager.getInstance().getCookie(url));
        connection.connect();

        if (connection.getResponseCode() == 302) {
            String cookie = connection.getHeaderField("Set-Cookie");
            CookieManager.getInstance().setCookie(url, cookie);
            String redirectUrl = connection.getHeaderField("Location");
            if (redirectUrl.startsWith(REDIRECT_URL)) {
                CookieSyncManager.createInstance(context);
                CookieSyncManager.getInstance().sync();
                return parseRedirectUrl(redirectUrl);
            }
            return authRecursive(redirectUrl);
        }
        return null;
    }

    public Observable<Feed> getNewsFeed(final int offset) {
        return Observable.create(new Observable.OnSubscribeFunc<Feed>() {

            @Override
            public Subscription onSubscribe(Observer<? super Feed> observer) {
                try {
                    Feed.Response newsFeed = vkApiLazy.get().getNewsFeed(offset);
                    process(observer, newsFeed);
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1);
    }

    public Observable<Feed> getWall(final int ownerId, final String filter, final int offset) {
        return Observable.create(new Observable.OnSubscribeFunc<Feed>() {

            @Override
            public Subscription onSubscribe(Observer<? super Feed> observer) {
                try {
                    Feed.Response newsFeed = vkApiLazy.get().getWall(ownerId, filter, offset);
                    process(observer, newsFeed);
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).map(new Func1<Feed, Feed>() {

            @Override
            public Feed call(Feed posts) {
                posts.items.remove(0);
                return posts;
            }
        }).retry(1);
    }

    private <T> void process(Observer<? super T> observer, Result<T> result) {
        if (result.isError()) {
            if (result.error.isAuthError()) {
                persistenceLazy.get().resetAuth();
            }
            observer.onError(result.error);
        } else {
            observer.onNext(result.getResponse());
        }
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
