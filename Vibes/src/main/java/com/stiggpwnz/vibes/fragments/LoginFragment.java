package com.stiggpwnz.vibes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;

import com.stiggpwnz.vibes.activities.MainActivity;
import com.stiggpwnz.vibes.util.Injector;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKAuth;

import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subjects.PublishSubject;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

public class LoginFragment extends WebViewFragment {

    @Inject Lazy<Persistence>       persistenceLazy;
    @Inject Lazy<CookieSyncManager> cookieSyncManagerLazy;

    PublishSubject<String> urlPublishSubject = PublishSubject.create();
    Subscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebView webView = getWebView();
        if (webView != null) {
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {
                    urlPublishSubject.onNext(url);
                }
            });
            if (webView.getUrl() == null) {
                webView.loadUrl(VKAuth.authUrl());
            }
        }

        subscription = urlPublishSubject.filter(new Func1<String, Boolean>() {

            @Override
            public Boolean call(String url) {
                return url.startsWith(VKAuth.REDIRECT_URL);
            }
        }).map(new Func1<String, Boolean>() {

            @Override
            public Boolean call(String s) {
                cookieSyncManagerLazy.get().sync();
                Map<String, String> map = VKAuth.parseRedirectUrl(s);
                return !map.containsKey("error") && persistenceLazy.get().saveAccessToken(map);
            }
        }).subscribeOn(Schedulers.threadPoolForIO()).subscribe(new Action1<Boolean>() {

            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean) {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                }
                getActivity().finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }
}
