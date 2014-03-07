package com.companyname.appname.dagger;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.fragments.FeedFragment;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.media.PlayerService;
import com.stiggpwnz.vibes.qualifiers.IOThreadPool;
import com.stiggpwnz.vibes.qualifiers.MainThread;
import com.stiggpwnz.vibes.util.JacksonSerializer;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKApi;
import com.stiggpwnz.vibes.vk.VKAuth;
import com.stiggpwnz.vibes.vk.models.Attachment;
import com.stiggpwnz.vibes.widget.AudioView;
import com.stiggpwnz.vibes.widget.PhotoView;
import com.stiggpwnz.vibes.widget.PostView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.devland.esperandro.Esperandro;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
@Module(addsTo = AppScopeDaggerModule.class, injects = {
        // services
        PlayerService.class,

        // fragments
        LoginFragment.class, NavigationFragment.class, FeedFragment.class,

        // holders
        PostView.class,

        // views
        PhotoView.class, AudioView.class})
public class UiScopeDaggerModule {

    @Provides @Singleton ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule simpleModule = new SimpleModule("attachment");
        simpleModule.addDeserializer(Attachment.Type.class, new Attachment.TypeDeserializer());
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }

    @Provides @IOThreadPool Scheduler provideIOThreadPool() { return Schedulers.io(); }

    @Provides @MainThread Scheduler provideMainThread() { return AndroidSchedulers.mainThread(); }

    @Provides @Singleton JacksonSerializer provideJacksonSerializer(ObjectMapper objectMapper) {
        return new JacksonSerializer(objectMapper);
    }

    @Provides @Singleton
    Persistence providePersistence(JacksonSerializer jacksonSerializer, Context context) {
        Esperandro.setSerializer(jacksonSerializer);
        return Esperandro.getPreferences(Persistence.class, context);
    }

    @Provides @Singleton CookieSyncManager provideCookieSyncManager(Context context) {
        return CookieSyncManager.createInstance(context);
    }

    @Provides
    CookieManager provideCookieManager(@SuppressWarnings("UnusedParameters") CookieSyncManager cookieSyncManager) {
        return CookieManager.getInstance();
    }

    @Provides @Singleton Handler provideHandler() { return new Handler(Looper.getMainLooper()); }

    @Provides @Singleton Converter provideJacksonConverter(ObjectMapper objectMapper) {
        return new JacksonConverter(objectMapper);
    }

    @Provides @Singleton VKAuth provideVkAuth(OkHttpClient okHttpClient,
                                              CookieSyncManager cookieSyncManager,
                                              CookieManager cookieManager, Persistence persistence) {
        return new VKAuth(okHttpClient, cookieSyncManager, cookieManager, persistence);
    }

    @Provides @Singleton
    VKApi provideVkApi(OkHttpClient okHttpClient, Converter converter, VKAuth vkAuth) {
        return new RestAdapter.Builder()
                .setEndpoint(VKApi.SERVER)
                .setClient(new OkClient(okHttpClient))
                .setConverter(converter)
                .setRequestInterceptor(vkAuth)
                .setLog(arg0 -> {
                    Timber.d(arg0);
                })
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build()
                .create(VKApi.class);
    }
}
