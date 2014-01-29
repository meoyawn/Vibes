package com.stiggpwnz.vibes.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
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

import butterknife.InjectView;
import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;

public class MainActivity extends BaseActivity {

    @Inject Lazy<VKAuth>            vkAuthLazy;
    @Inject Lazy<CookieManager>     cookieManagerLazy;
    @Inject Lazy<CookieSyncManager> cookieSyncManagerLazy;

    @InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (cookieManagerLazy.get().getCookie("vk.com") == null) {
            login(savedInstanceState);
        } else {
            init(savedInstanceState);
        }
    }

    @Override
    public void onAttachFragment(final Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof LoginFragment) {
            ((LoginFragment) fragment).getUrls()
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(final String string) {
                            cookieSyncManagerLazy.get().sync();
                            return Observable.create(new Observable.OnSubscribeFunc<String>() {
                                @Override
                                public Subscription onSubscribe(Observer<? super String> observer) {
                                    try {
                                        observer.onNext(vkAuthLazy.get().saveAuth(string, System.currentTimeMillis()));
                                    } catch (Exception e) {
                                        observer.onError(e);
                                    }
                                    return Subscriptions.empty();
                                }
                            }).subscribeOn(Schedulers.io());
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            finish();
                        }

                        @Override
                        public void onNext(String s) {
                            getFragmentManager().beginTransaction()
                                    .remove(fragment)
                                    .commit();
                            init(null);
                        }
                    });
        }
    }

    void login(final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new LoginFragment())
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
