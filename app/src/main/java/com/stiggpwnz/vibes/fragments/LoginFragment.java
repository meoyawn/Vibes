package com.stiggpwnz.vibes.fragments;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.stiggpwnz.vibes.Dagger;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKAuth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import butterknife.InjectView;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class LoginFragment extends BaseFragment {
    @Inject Persistence       persistence;
    @Inject CookieManager     cookieManager;
    @Inject CookieSyncManager cookieSyncManager;
    @Inject VKAuth            vkAuth;

    @InjectView(R.id.ptr_layout)    PullToRefreshLayout pullToRefreshLayout;
    @InjectView(R.id.webview_login) WebView             webView;

    @NotNull final PublishSubject<String> urls = PublishSubject.create();
    @NotNull Subscription urlSubscription;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dagger.inject(this);
    }

    @Override protected void configure(@NotNull ActionBar actionBar) {
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(getString(R.string.login));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login, container, false);
    }

    @Override public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .setup(pullToRefreshLayout);

        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                urls.onNext(url);
                if (pullToRefreshLayout != null) {
                    pullToRefreshLayout.setRefreshing(true);
                }
            }

            @Override public void onPageFinished(WebView view, String url) {
                if (pullToRefreshLayout != null) {
                    pullToRefreshLayout.setRefreshComplete();
                }
            }
        });

        urlSubscription = AndroidObservable.fromFragment(this, urls
                .filter(s -> s.startsWith(VKAuth.REDIRECT_URL))
                .flatMap(s -> vkAuth.saveAccessToken(s, System.currentTimeMillis())
                        .subscribeOn(Schedulers.io())))
                .subscribe(s -> {
                    getFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new MainFragment())
                            .commit();
                }, e -> loadInitialUrl());

        if (savedInstanceState == null) {
            loadInitialUrl();
        } else {
            webView.restoreState(savedInstanceState);
        }
    }


    void loadInitialUrl() {
        webView.stopLoading();
        webView.loadUrl(VKAuth.authUrl());
        if (pullToRefreshLayout != null) {
            pullToRefreshLayout.setRefreshing(true);
        }
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override public void onDestroy() {
        urlSubscription.unsubscribe();
        if (persistence.accessToken() == null) {
            cookieManager.removeAllCookie();
        }
        cookieSyncManager.sync();
        super.onDestroy();
    }
}
