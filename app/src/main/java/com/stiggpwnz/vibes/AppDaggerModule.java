package com.stiggpwnz.vibes;

import android.content.Context;
import android.os.Environment;

import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.qualifiers.CacheDir;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
@Module(library = true)
public class AppDaggerModule {
    final Context context;

    public AppDaggerModule(Context context) { this.context = context; }

    @Provides Context provideContext() { return context; }

    @Provides @CacheDir File provideCacheDir(Context context) {
        boolean mounted = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        return mounted ? context.getExternalCacheDir() : context.getCacheDir();
    }

    @Provides @Singleton OkHttpClient provideOkHttpClient(@CacheDir File cacheDir) {
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            okHttpClient.setResponseCache(new HttpResponseCache(cacheDir, 20 * 1024 * 1024));
        } catch (IOException ignored) {
        }
        return okHttpClient;
    }

    @Provides @Singleton LruCache provideLruCache(Context context) { return new LruCache(context); }

    @Provides @Singleton
    Picasso providePicasso(Context context, OkHttpClient okHttpClient, LruCache lruCache) {
        return new Picasso.Builder(context)
                .downloader(new OkHttpDownloader(okHttpClient))
                .memoryCache(lruCache)
                .build();
    }
}
