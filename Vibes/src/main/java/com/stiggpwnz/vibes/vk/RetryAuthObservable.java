package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Result;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.MultipleAssignmentSubscription;
import rx.util.functions.Func2;

/**
 * Created by adel on 1/29/14
 */
public class RetryAuthObservable<T> implements Observable.OnSubscribeFunc<T> {

    private final Observable<? extends Result<T>> source;
    private final CompositeSubscription subscription = new CompositeSubscription();
    private final Lazy<VKAuth> vkAuthLazy;

    public RetryAuthObservable(Observable<? extends Result<T>> source, Lazy<VKAuth> vkAuthLazy) {
        this.source = source;
        this.vkAuthLazy = vkAuthLazy;
    }

    @Override
    public Subscription onSubscribe(Observer<? super T> observer) {
        MultipleAssignmentSubscription rescursiveSubscription = new MultipleAssignmentSubscription();
        subscription.add(Schedulers.currentThread().schedule(rescursiveSubscription, attemptSubscription(observer)));
        subscription.add(rescursiveSubscription);
        return subscription;
    }

    private Func2<Scheduler, MultipleAssignmentSubscription, Subscription> attemptSubscription(final Observer<? super T> observer) {
        return new Func2<Scheduler, MultipleAssignmentSubscription, Subscription>() {
            @Override
            public Subscription call(final Scheduler scheduler, final MultipleAssignmentSubscription rescursiveSubscription) {
                return source.subscribe(new Observer<Result<T>>() {
                    @Override
                    public void onCompleted() {
                        observer.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onNext(Result<T> result) {
                        if (result.isError()) {
                            if (result.error.isAuthError()) {
                                vkAuthLazy.get().resetAuth();
                                rescursiveSubscription.setSubscription(scheduler.schedule(rescursiveSubscription, attemptSubscription(observer)));
                            } else {
                                observer.onError(result.error);
                            }
                        } else {
                            observer.onNext(result.response);
                        }
                    }
                });
            }
        };
    }
}
