package com.stiggpwnz.vibes.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.stiggpwnz.vibes.fragments.base.RetainedProgressFragment;
import com.stiggpwnz.vibes.util.Injector;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKAuth;

import javax.inject.Inject;

import dagger.Lazy;
import rx.subjects.PublishSubject;

public class LoginFragment extends RetainedProgressFragment {

    @Inject Lazy<Persistence>       persistenceLazy;
    @Inject Lazy<CookieManager>     cookieManagerLazy;
    @Inject Lazy<CookieSyncManager> cookieSyncManagerLazy;

    final PublishSubject<String> urlPublishSubject = PublishSubject.create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WebView webView = new WebView(getActivity());
        webView.setId(987654);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setContentShown(false);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.startsWith(VKAuth.REDIRECT_URL)) {
                    urlPublishSubject.onNext(url);
                } else {
                    setContentShown(true);
                }
            }
        });
        setContentView(webView);
        if (webView.getUrl() == null) {
            webView.loadUrl(VKAuth.authUrl());
        }
    }

    @Override
    public void onDestroy() {
        if (persistenceLazy.get().getAccessToken() == null) {
            cookieManagerLazy.get().removeAllCookie();
            cookieSyncManagerLazy.get().sync();
        }
        super.onDestroy();
    }

    public PublishSubject<String> getUrlPublishSubject() {
        return urlPublishSubject;
    }
}
