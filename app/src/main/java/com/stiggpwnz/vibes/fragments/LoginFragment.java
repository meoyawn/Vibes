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

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.fragments.base.BaseFragment;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKAuth;

import javax.inject.Inject;

import butterknife.InjectView;
import dagger.Lazy;
import rx.subjects.PublishSubject;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class LoginFragment extends BaseFragment {

    @Inject Lazy<Persistence>       persistenceLazy;
    @Inject Lazy<CookieManager>     cookieManagerLazy;
    @Inject Lazy<CookieSyncManager> cookieSyncManagerLazy;

    PublishSubject<String> urls = PublishSubject.create();

    @InjectView(R.id.ptr_layout)    PullToRefreshLayout pullToRefreshLayout;
    @InjectView(R.id.webview_login) WebView             webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .setup(pullToRefreshLayout);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.startsWith(VKAuth.REDIRECT_URL)) {
                    urls.onNext(url);
                } else if (pullToRefreshLayout != null) {
                    pullToRefreshLayout.setRefreshing(true);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (pullToRefreshLayout != null) {
                    pullToRefreshLayout.setRefreshComplete();
                }
            }
        });
        if (webView.getUrl() == null) {
            webView.loadUrl(VKAuth.authUrl());
            pullToRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onDestroy() {
        if (persistenceLazy.get().accessToken() == null) {
            cookieManagerLazy.get().removeAllCookie();
            cookieSyncManagerLazy.get().sync();
        }
        super.onDestroy();
    }

    public PublishSubject<String> getUrls() { return urls; }
}
