package com.stiggpwnz.vibes;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.fragments.BaseFragment;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.fragments.MainFragment;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.qualifiers.IOThreadPool;
import com.stiggpwnz.vibes.util.JacksonSerializer;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKAuth;
import com.stiggpwnz.vibes.vk.models.Attachment;
import com.stiggpwnz.vibes.widget.AudioView;
import com.stiggpwnz.vibes.widget.PhotoView;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.devland.esperandro.Esperandro;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import mortar.Blueprint;
import mortar.Mortar;
import mortar.MortarActivityScope;
import mortar.MortarContext;
import mortar.MortarScope;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends FragmentActivity implements MortarContext {
    static {
        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf");
    }

    @Inject  CookieManager       cookieManager;
    @NotNull MortarActivityScope activityScope;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityScope = Mortar.requireActivityScope(Mortar.getScope(getApplicationContext()), getBlueprint());
        activityScope.onCreate(savedInstanceState);

        Mortar.inject(this, this);

        if (savedInstanceState == null) {
            Fragment fragment = cookieManager.getCookie("vk.com") == null ?
                    new LoginFragment() :
                    new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        }
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        activityScope.onSaveInstanceState(outState);
    }

    static Blueprint getBlueprint() {
        return new Blueprint() {
            @Override public String getMortarScopeName() { return MainActivity.class.getName(); }

            @Override public Object getDaggerModule() { return new DaggerModule(); }
        };
    }

    @Override public void onBackPressed() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof BaseFragment) {
                if (((BaseFragment) fragment).onBackPressed()) {
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override public void onStart() {
//        super.onStart();
//        EasyTracker.getInstance(this).activityStart(this);
//    }
//
//    @Override public void onStop() {
//        super.onStop();
//        EasyTracker.getInstance(this).activityStop(this);
//    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @Override
    protected void onDestroy() {
        Crouton.cancelAllCroutons();
        if (isFinishing()) {
            activityScope.destroy();
        }
        super.onDestroy();
    }

    @Override public MortarScope getMortarScope() { return activityScope; }

    /**
     * Created by adelnizamutdinov on 03/03/2014
     */
    @Module(addsTo = App.DaggerModule.class, injects = {
            // activities
            MainActivity.class,

            // fragments
            LoginFragment.class, MainFragment.class, NavigationFragment.class,

//            // holders
//            PostView.class,

            // views
            PhotoView.class, AudioView.class})
    public static class DaggerModule {
        @Provides @Singleton ObjectMapper provideObjectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            SimpleModule simpleModule = new SimpleModule("");
            simpleModule.addDeserializer(Attachment.Type.class, new Attachment.TypeDeserializer());
            objectMapper.registerModule(simpleModule);

            return objectMapper;
        }

        @Provides @IOThreadPool Scheduler provideIOThreadPool() { return Schedulers.io(); }


        @Provides @Singleton CookieSyncManager provideCookieSyncManager(Context context) {
            return CookieSyncManager.createInstance(context);
        }

        @Provides
        CookieManager provideCookieManager(@SuppressWarnings("UnusedParameters") CookieSyncManager cookieSyncManager) {
            return CookieManager.getInstance();
        }

//        @Provides @Singleton
//        Handler provideHandler() { return new Handler(Looper.getMainLooper()); }

        @Provides @Singleton JacksonSerializer provideJacksonSerializer(ObjectMapper objectMapper) {
            return new JacksonSerializer(objectMapper);
        }

        @Provides @Singleton
        Persistence providePersistence(JacksonSerializer jacksonSerializer, Context context) {
            Esperandro.setSerializer(jacksonSerializer);
            return Esperandro.getPreferences(Persistence.class, context);
        }

        @Provides @Singleton VKAuth provideVkAuth(OkHttpClient okHttpClient,
                                                  CookieSyncManager cookieSyncManager,
                                                  CookieManager cookieManager, Persistence persistence) {
            return new VKAuth(okHttpClient, cookieSyncManager, cookieManager, persistence);
        }
    }
}