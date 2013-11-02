package com.stiggpwnz.vibes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.activities.LoginActivity;
import com.stiggpwnz.vibes.activities.MainActivity;
import com.stiggpwnz.vibes.adapters.NewsFeedAdapter;
import com.stiggpwnz.vibes.fragments.LoginFragment;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.fragments.NewsFeedFragment;
import com.stiggpwnz.vibes.util.DiskUtils;
import com.stiggpwnz.vibes.util.Persistence;
import com.stiggpwnz.vibes.vk.VKApi;
import com.stiggpwnz.vibes.vk.VKontakte;

import java.io.IOException;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
import timber.log.Timber;

import static com.stiggpwnz.vibes.util.Persistence.ACCESS_TOKEN;


/**
 * Created by adelnizamutdinov on 18/09/2013
 */
@Module(injects = {
        // activities
        LoginActivity.class, MainActivity.class,

        // adapters
        NewsFeedAdapter.class,

        // fragments
        LoginFragment.class, NavigationFragment.class, NewsFeedFragment.class})
public class DependenciesModule {

    Context context;

    DependenciesModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    LruCache provideLruCache() {
        return new LruCache(context);
    }

    @Provides
    @Singleton
    Picasso providePicasso(OkHttpClient okHttpClient, LruCache lruCache) {
        return new Picasso.Builder(context)
                .downloader(new OkHttpDownloader(okHttpClient))
                .memoryCache(lruCache)
                .debugging(BuildConfig.DEBUG)
                .build();
    }

    @Provides
    @Singleton
    @Named("prefs")
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }

    @Provides
    @Singleton
    RestAdapter.Log provideRetrofitLog() {
        return new RestAdapter.Log() {

            @Override
            public void log(String arg0) {
                Timber.d(arg0);
            }
        };
    }

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    Converter provideJacksonConverter(ObjectMapper objectMapper) {
        return new JacksonConverter(objectMapper);
    }

    @Provides
    @Singleton
    OkHttpClient provideHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            int maxSize = 20 * 1024 * 1024; // 20 MB
            okHttpClient.setResponseCache(new HttpResponseCache(DiskUtils.cacheDirNamed(context, "http"), maxSize));
        } catch (IOException e) {
            Timber.e(e, "Failed to create a cache folder for OkHttpClient");
        }
        return okHttpClient;
    }

    @Provides
    @Singleton
    Client provideClient(OkHttpClient okHttpClient) {
        return new OkClient(okHttpClient);
    }


    @Provides
    @Singleton
    VKApi provideRoadtrippers(Client client, Converter converter, RestAdapter.Log log, final Persistence persistence, final VKontakte vKontakte) {
        return new RestAdapter.Builder()
                .setServer(VKApi.SERVER)
                .setClient(client)
                .setConverter(converter)
                .setRequestInterceptor(new RequestInterceptor() {

                    @Override
                    public void intercept(RequestFacade requestFacade) {
                        String accessToken = persistence.getAccessToken();
                        if (accessToken == null) {
                            try {
                                Map<String, String> map = vKontakte.auth();
                                persistence.saveAccessToken(map);
                                accessToken = map.get(ACCESS_TOKEN);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to refresh access token", e);
                            }
                        }
                        requestFacade.addQueryParam(ACCESS_TOKEN, accessToken);
                    }
                })
                .setLog(log)
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build()
                .create(VKApi.class);
    }
}
