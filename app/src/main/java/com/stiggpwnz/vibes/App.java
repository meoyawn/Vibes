package com.stiggpwnz.vibes;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.util.CrashReportingTree;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import mortar.Mortar;
import mortar.MortarContext;
import mortar.MortarScope;
import timber.log.Timber;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
public class App extends Application implements MortarContext {
    MortarScope rootScope;

    @Override public void onCreate() {
        super.onCreate();
//      Crittercism.initialize(getApplicationContext(), getString(R.string.crittercism_app_id));
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashReportingTree());
        ObjectGraph objectGraph = ObjectGraph.create(getModules().toArray());
        rootScope = Mortar.createRootScope(BuildConfig.DEBUG, objectGraph);
    }

    // we create a list containing the MyModule Dagger module
    // later we can add any other modules to it (for testing)
    @NotNull protected List<Object> getModules() {
        return new ArrayList<>(Arrays.asList(new DaggerModule(this)));
    }

    @Override public MortarScope getMortarScope() { return rootScope; }

    @Qualifier @Retention(RetentionPolicy.RUNTIME) public static @interface CacheDir {}

    /**
     * Created by adelnizamutdinov on 03/03/2014
     */
    @Module(library = true)
    public static class DaggerModule {
        final Context context;

        public DaggerModule(Context context) {this.context = context;}

        @Provides Context provideContext() {return context;}

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

        @Provides @Singleton LruCache provideLruCache(Context context) {
            return new LruCache(context);
        }

        @Provides @Singleton
        Picasso providePicasso(Context context, OkHttpClient okHttpClient, LruCache lruCache) {
            return new Picasso.Builder(context)
                    .downloader(new OkHttpDownloader(okHttpClient))
                    .memoryCache(lruCache)
                    .build();
        }
    }
}
