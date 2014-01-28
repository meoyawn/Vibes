package com.stiggpwnz.vibes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.base.BaseActivity;
import com.stiggpwnz.vibes.fragments.FeedFragmentBuilder;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.media.PlayerService;
import com.stiggpwnz.vibes.vk.VKAuth;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

public class MainActivity extends BaseActivity {

    @Inject Lazy<VKAuth>            vkAuthLazy;
    @Inject Lazy<CookieManager>     cookieManagerLazy;
    @Inject Lazy<CookieSyncManager> cookieSyncManagerLazy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (cookieManagerLazy.get().getCookie("vk.com") == null) {
            login(savedInstanceState);
        } else {
            init(savedInstanceState);
        }
    }

    void login(final Bundle savedInstanceState) {
        final LoginFragment loginFragment = new LoginFragment();

        loginFragment.getUrls()
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(final String string) {
                        return Observable.create(new Observable.OnSubscribeFunc<Boolean>() {
                            @Override
                            public Subscription onSubscribe(Observer<? super Boolean> observer) {
                                cookieSyncManagerLazy.get().sync();
                                observer.onNext(vkAuthLazy.get().saveAuth(string, System.currentTimeMillis()));
                                return Subscriptions.empty();
                            }
                        }).subscribeOn(Schedulers.io());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            getFragmentManager().beginTransaction()
                                    .remove(loginFragment)
                                    .commit();
                            init(savedInstanceState);
                        } else {
                            finish();
                        }
                    }
                });

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, loginFragment)
                    .commit();
        }
    }

    public void onUnitClicked() {
        // TODO FUCK
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, FeedFragmentBuilder.newFeedFragment(5))
                .commit();
    }

    void init(Bundle savedInstanceState) {
        setContentView(R.layout.main_root);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.left_drawer, new NavigationFragment())
                    .replace(R.id.content_frame, FeedFragmentBuilder.newFeedFragment(0))
                    .commit();
        }

        startService(new Intent(this, PlayerService.class));
    }
}
