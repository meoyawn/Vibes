package com.stiggpwnz.vibes.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.companyname.appname.fragments.MainFragment;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.fragments.base.BaseFragment;
import com.stiggpwnz.vibes.qualifiers.IOThreadPool;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKAuth;

import javax.inject.Inject;

import butterknife.InjectView;
import rx.Scheduler;
import rx.android.observables.AndroidObservable;
import rx.subjects.PublishSubject;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class LoginFragment extends BaseFragment {
    @Inject               Persistence       persistence;
    @Inject               CookieManager     cookieManager;
    @Inject               CookieSyncManager cookieSyncManager;
    @Inject               VKAuth            vkAuth;
    @Inject @IOThreadPool Scheduler         ioThreadPool;

    final PublishSubject<String> urls = PublishSubject.create();

    @InjectView(R.id.ptr_layout)    PullToRefreshLayout pullToRefreshLayout;
    @InjectView(R.id.webview_login) WebView             webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .setup(pullToRefreshLayout);

        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                pullToRefreshLayout.setRefreshing(true);
            }

            @Override public void onPageFinished(WebView view, String url) {
                pullToRefreshLayout.setRefreshComplete();
            }
        });

        if (savedInstanceState == null) {
            loadInitialUrl();
        } else {
            webView.restoreState(savedInstanceState);
        }

        AndroidObservable.fromFragment(this, urls.filter(url -> url.startsWith(VKAuth.REDIRECT_URL))
                .doOnNext(url -> cookieSyncManager.sync())
                .map(url -> vkAuth.saveAndGetAccessToken(url, System.currentTimeMillis()))
                .subscribeOn(ioThreadPool))
                .subscribe(s -> {
                    if (getFragmentManager() != null) {
                        getFragmentManager().beginTransaction()
                                .replace(android.R.id.content, new MainFragment())
                                .commit();
                    }
                }, e -> loadInitialUrl());
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    void loadInitialUrl() {
        webView.loadUrl(VKAuth.authUrl());
        pullToRefreshLayout.setRefreshing(true);
    }

    @Override public void onDestroy() {
        if (persistence.accessToken() == null) {
            cookieManager.removeAllCookie();
            cookieSyncManager.sync();
        }
        super.onDestroy();
    }
}
