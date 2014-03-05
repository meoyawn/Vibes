package com.stiggpwnz.vibes.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.observables.ConnectableObservable;

/**
 * Created by adel on 1/29/14
 */

@Singleton
public class ReactiveJobQueue {

    Map<Class<?>, ConnectableObservable<?>> observables   = new ConcurrentHashMap<>();
    Map<Class<?>, Subscription>             subscriptions = new ConcurrentHashMap<>();

    @Inject
    public ReactiveJobQueue() {}

    public <T> ConnectableObservable<T> connect(final Class<?> clazz, Observable<T> observable, boolean reload) {
        if (reload) {
            remove(clazz);
            return createNew(clazz, observable);
        } else {
            return connect(clazz, observable);
        }
    }

    <T> ConnectableObservable<T> connect(final Class<?> clazz, Observable<T> observable) {
        ConnectableObservable<T> connectableObservable = (ConnectableObservable<T>) observables.get(clazz);
        if (connectableObservable == null) {
            connectableObservable = createNew(clazz, observable);
        }
        return connectableObservable;
    }

    public ConnectableObservable<?> get(Class<?> clazz) {
        return observables.get(clazz);
    }

    <T> ConnectableObservable<T> createNew(final Class<?> clazz, Observable<T> observable) {
        ConnectableObservable<T> connectableObservable = observable.replay();
        Subscription connect = connectableObservable.connect();
        subscriptions.put(clazz, connect);
        observables.put(clazz, connectableObservable);
        connectableObservable.subscribe(new Observer<Object>() {
            @Override
            public void onCompleted() {
                remove(clazz);
            }

            @Override
            public void onError(Throwable e) {
                remove(clazz);
            }

            @Override
            public void onNext(Object args) {
                remove(clazz);
            }
        });
        return connectableObservable;
    }

    void remove(Class<?> clazz) {
        subscriptions.remove(clazz);
        observables.remove(clazz);
    }
}
