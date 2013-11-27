package com.stiggpwnz.vibes.util;

import android.content.SharedPreferences;
import android.os.Looper;
import android.text.TextUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import static java.lang.System.currentTimeMillis;

@Singleton
public class Persistence {

    public static final String ACCESS_TOKEN = "access_token";

    static final String USER_ID    = "user_id";
    static final String EXPIRES_IN = "expires_in";

    Lazy<SharedPreferences> prefsLazy;
    Lazy<JacksonSerializer> jackson;

    @Inject
    public Persistence(@Named("prefs") Lazy<SharedPreferences> prefsLazy, Lazy<JacksonSerializer> jackson) {
        this.prefsLazy = prefsLazy;
        this.jackson = jackson;
    }

    private String getString(String key, String defValue) {
        return prefsLazy.get().getString(key, defValue);
    }

    public String getAccessToken() {
        String token = getString(ACCESS_TOKEN, null);
        if (token == null || currentTimeMillis() >= getExpiresIn()) {
            return null;
        }
        return token;
    }

    long getExpiresIn() {
        return prefsLazy.get().getLong(EXPIRES_IN, 0);
    }

    public boolean resetAuth() {
        return prefsLazy.get().edit()
                .remove(ACCESS_TOKEN)
                .remove(USER_ID)
                .remove(EXPIRES_IN)
                .commit();
    }

    public boolean saveAccessToken(Map<String, String> map) {
        return prefsLazy.get().edit()
                .putString(USER_ID, map.get(USER_ID))
                .putString(ACCESS_TOKEN, map.get(ACCESS_TOKEN))
                .putLong(EXPIRES_IN, Long.valueOf(map.get(EXPIRES_IN)) * 1000 + currentTimeMillis())
                .commit();
    }

    public boolean clear() {
        return prefsLazy.get().edit().clear().commit();
    }

    private <T> void save(T object, final String key) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("wrong thread, buddy, do it in background");
        }
        prefsLazy.get().edit().putString(key, jackson.get().serialize(object)).commit();
    }

    static boolean isHealthy(String string) {
        return !"null".equals(string) && !TextUtils.isEmpty(string);
    }

    private <T> Observable<T> get(final String key, final Class<T> clazz, final TypeReference<T> type) {
        return Observable.create(new Observable.OnSubscribeFunc<T>() {

            @Override
            public Subscription onSubscribe(Observer<? super T> observer) {
                try {
                    String string = prefsLazy.get().getString(key, null);
                    if (!isHealthy(string)) {
                        observer.onError(new Exception("Mjssing " + key + " object"));
                    } else {
                        T object;
                        if (type != null) {
                            object = jackson.get().deserialize(string, type);
                        } else {
                            object = jackson.get().deserialize(string, clazz);
                        }
                        observer.onNext(object);
                    }
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        });
    }

    private <T> Observable<T> get(final String key, final Class<T> clazz) {
        return get(key, clazz, null);
    }

    private <T> Observable<T> get(final String key, final TypeReference<T> type) {
        return get(key, null, type);
    }
}
