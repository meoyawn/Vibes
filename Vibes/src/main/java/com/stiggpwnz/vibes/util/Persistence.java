package com.stiggpwnz.vibes.util;

import android.content.SharedPreferences;
import android.os.Looper;
import android.text.TextUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;

@Singleton
public class Persistence {

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


    public Observable<Boolean> clear() {
        return Observable.just(prefsLazy).map(new Func1<Lazy<SharedPreferences>, Boolean>() {

            @Override
            public Boolean call(Lazy<SharedPreferences> prefsLazy) {
                return prefsLazy.get().edit().clear().commit();
            }
        });
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
