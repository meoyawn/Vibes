package com.stiggpwnz.vibes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;

import com.stiggpwnz.vibes.VibesApplication;
import com.stiggpwnz.vibes.activities.MainActivity;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKAuth;

import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;
import timber.log.Timber;

public class LoginFragment extends WebViewFragment {

    @Inject Lazy<Persistence>       persistenceLazy;
    @Inject Lazy<CookieSyncManager> cookieSyncManagerLazy;

    PublishSubject<String> urlPublishSubject = PublishSubject.create();
    Subscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VibesApplication.from(getActivity()).inject(this);
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
        }).flatMap(new Func1<String, Observable<Boolean>>() {

            @Override
            public Observable<Boolean> call(final String url) {
                return Observable.create(new Observable.OnSubscribeFunc<Boolean>() {

                    @SuppressWarnings("all")
                    @Override
                    public Subscription onSubscribe(Observer<? super Boolean> observer) {
                        cookieSyncManagerLazy.get().sync();
                        Map<String, String> map = VKAuth.parseRedirectUrl(url);
                        if (map.containsKey("error")) {
                            observer.onError(null);
                        } else {
                            observer.onNext(persistenceLazy.get().saveAccessToken(map));
                        }
                        return Subscriptions.empty();
                    }
                });
            }
        }).subscribeOn(Schedulers.threadPoolForIO()).subscribe(new Observer<Boolean>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "fail");
                getActivity().finish();
            }

            @Override
            public void onNext(Boolean args) {
                startActivity(new Intent(getActivity(), MainActivity.class));
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
