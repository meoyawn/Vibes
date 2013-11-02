package com.stiggpwnz.vibes.fragments.base;

import android.os.Bundle;

import com.roadtrippers.api.Roadtrippers;
import com.roadtrippers.api.models.Category;
import com.roadtrippers.util.Persistence;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public abstract class RetainedProgressFragment extends BaseProgressFragment {

    @Inject Lazy<Roadtrippers> roadtrippers;
    @Inject Lazy<Persistence>  persistence;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    protected Observable<List<Category>> getBucketsFromServer() {
        return Observable.create(new Observable.OnSubscribeFunc<List<Category>>() {

            @Override
            public Subscription onSubscribe(Observer<? super List<Category>> observer) {
                try {
                    List<Category> buckets = roadtrippers.get().getBuckets();
                    observer.onNext(buckets);
                    persistence.get().saveBuckets(buckets);
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        });
    }
}
