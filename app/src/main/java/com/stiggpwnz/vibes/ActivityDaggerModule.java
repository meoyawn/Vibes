package com.stiggpwnz.vibes;

import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.squareup.okhttp.OkHttpClient;
import com.stiggpwnz.vibes.db.DatabaseHelper;
import com.stiggpwnz.vibes.fragments.FeedFragment;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.fragments.MainFragment;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.util.JacksonSerializer;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKApi;
import com.stiggpwnz.vibes.vk.VKAuth;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Attachment;
import com.stiggpwnz.vibes.vk.models.Unit;
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
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
@Module(addsTo = AppDaggerModule.class,
        injects = {
                MainActivity.class,
                LoginFragment.class,
                MainFragment.class,
                NavigationFragment.class,
                AudioView.class,

                FeedFragment.class,
                PostView.class,
                PhotoView.class
        })
public class ActivityDaggerModule {
    /*
     * ACTIVITY
     */

    @Provides @Singleton ObjectMapper provideObjectMapper(Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule simpleModule = new SimpleModule("");
        simpleModule.addDeserializer(Attachment.Type.class, new Attachment.TypeDeserializer());
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }

    @Provides @Singleton CookieSyncManager provideCookieSyncManager(Context context) {
        return CookieSyncManager.createInstance(context);
    }

    @Provides
    CookieManager provideCookieManager(@SuppressWarnings("UnusedParameters") CookieSyncManager cookieSyncManager) {
        return CookieManager.getInstance();
    }

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

    /*
    * FEED FRAGMENT
    */

    @Provides @Singleton Converter provideJacksonConverter(ObjectMapper objectMapper) {
        return new JacksonConverter(objectMapper);
    }

    @Provides @Singleton
    VKApi provideVkApi(OkHttpClient okHttpClient, Converter converter, VKAuth vkAuth) {
        Timber.d("creating vkapi");
        return new RestAdapter.Builder()
                .setEndpoint(VKApi.SERVER)
                .setClient(new OkClient(okHttpClient))
                .setConverter(converter)
                .setRequestInterceptor(vkAuth)
                .setLog(Timber::d)
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build()
                .create(VKApi.class);
    }

    @Provides @Singleton DatabaseHelper provideDatabaseHelper(Context context) {
        return new DatabaseHelper(context);
    }

    @Provides @Singleton
    VKontakte provideVKontakte(VKApi vkApi, VKAuth vkAuth, DatabaseHelper databaseHelper) {
        return new VKontakte(vkApi, vkAuth, databaseHelper);
    }

    @Provides @Singleton PublishSubject<Unit> provideUnitClicks() {
        return PublishSubject.create();
    }

    @Provides @Singleton PublishSubject<Bitmap> provideLoadedBitmaps() {
        return PublishSubject.create();
    }
}
