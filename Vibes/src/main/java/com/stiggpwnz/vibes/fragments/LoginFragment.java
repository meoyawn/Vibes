package com.stiggpwnz.vibes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.stiggpwnz.vibes.activities.MainActivity;
import com.stiggpwnz.vibes.fragments.base.BaseFragment;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKontakte;

import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.concurrency.Schedulers;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

public class LoginFragment extends BaseFragment {

    @Inject Lazy<Persistence> persistenceLazy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new WebView(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebView webView = (WebView) view;
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.startsWith(VKontakte.REDIRECT_URL)) {
                    Observable.just(url).map(new Func1<String, Boolean>() {

                        @Override
                        public Boolean call(String url) {
                            Map<String, String> map = VKontakte.parseRedirectUrl(url);
                            String cookie = CookieManager.getInstance().getCookie(url);
                            return persistenceLazy.get().saveCookie(cookie) && persistenceLazy.get().saveAccessToken(map);
                        }
                    }).subscribeOn(Schedulers.threadPoolForIO()).subscribe(new Action1<Boolean>() {

                        @Override
                        public void call(Boolean aBoolean) {
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        }
                    });
                }
            }
        });
        if (webView.getUrl() == null) {
            webView.loadUrl(VKontakte.authUrl());
        }
    }

    public WebView getWebView() {
        return (WebView) getView();
    }
}
