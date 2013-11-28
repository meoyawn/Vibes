package com.stiggpwnz.vibes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.squareup.otto.Subscribe;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.base.HomeAsUpActivity;
import com.stiggpwnz.vibes.events.UnitClickedEvent;
import com.stiggpwnz.vibes.fragments.FeedFragment;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.media.PlayerService;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKAuth;

import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

public class MainActivity extends HomeAsUpActivity {

    @Inject Lazy<Persistence>       persistenceLazy;
    @Inject Lazy<CookieManager>     cookieManagerLazy;
    @Inject Lazy<CookieSyncManager> cookieSyncManagerLazy;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (cookieManagerLazy.get().getCookie("vk.com") == null) {
            login(savedInstanceState);
        } else {
            init(savedInstanceState);
        }
    }

    void login(final Bundle savedInstanceState) {
        final LoginFragment loginFragment = new LoginFragment();

        loginFragment.getUrlPublishSubject()
                .map(new Func1<String, Boolean>() {

                    @Override
                    public Boolean call(String s) {
                        cookieSyncManagerLazy.get().sync();
                        Map<String, String> map = VKAuth.parseRedirectUrl(s);
                        return !map.containsKey("error") && persistenceLazy.get().saveAccessToken(map);
                    }
                })
                .subscribeOn(Schedulers.threadPoolForIO())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {

                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            getSupportFragmentManager().beginTransaction()
                                    .remove(loginFragment)
                                    .commit();
                            init(savedInstanceState);
                        } else {
                            finish();
                        }
                    }
                });

        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, loginFragment)
                .commit();
    }

    @Subscribe
    public void onUnitClicked(UnitClickedEvent event) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new FeedFragment(event.ownerId))
                .commit();
    }

    void init(Bundle savedInstanceState) {
        setContentView(R.layout.main_root);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.left_drawer, new NavigationFragment())
                    .replace(R.id.content_frame, new FeedFragment(0))
                    .commit();
        }

        startService(new Intent(this, PlayerService.class));
    }
}
