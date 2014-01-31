package com.stiggpwnz.vibes.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.stiggpwnz.vibes.App;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.util.ScopedContext;
import com.stiggpwnz.vibes.util.Utils;
import com.stiggpwnz.vibes.vk.VKAuth;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.Lazy;
import flow.Flow;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/**
 * Created by adel on 1/31/14
 */
public class LoginView extends PullToRefreshLayout {

    @Inject      Lazy<Persistence>       persistenceLazy;
    @Inject      Lazy<CookieManager>     cookieManagerLazy;
    @Inject      Lazy<CookieSyncManager> cookieSyncManagerLazy;
    @Inject      Lazy<VKAuth>            vkAuthLazy;
    @Inject @App Flow                    flow;

    @InjectView(R.id.webview_login) WebView webView;

    public LoginView(Context context) {
        super(context);
        init(context);
    }

    public LoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoginView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return;
        }

        Utils.inject(context, this);

        ScopedContext scopedContext = (ScopedContext) context;
        ActionBarPullToRefresh.from((Activity) scopedContext.getBaseContext())
                .allChildrenArePullable()
                .setup(this);

        ButterKnife.inject(this);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, final String url, Bitmap favicon) {
                if (url.startsWith(VKAuth.REDIRECT_URL)) {
                    cookieSyncManagerLazy.get().sync();
                    Observable.create(new Observable.OnSubscribeFunc<String>() {
                        @Override
                        public Subscription onSubscribe(Observer<? super String> observer) {
                            try {
                                observer.onNext(vkAuthLazy.get().saveAuth(url, System.currentTimeMillis()));
                            } catch (Exception e) {
                                observer.onError(e);
                            }
                            return Subscriptions.empty();
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<String>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    // TODO error, reload
                                }

                                @Override
                                public void onNext(String s) {
                                    flow.goTo(new App.NavigationScreen());
                                }
                            });
                } else {
                    setRefreshing(true);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setRefreshComplete();
            }
        });
        if (webView.getUrl() == null) {
            webView.loadUrl(VKAuth.authUrl());
            setRefreshing(true);
        }
    }
}
