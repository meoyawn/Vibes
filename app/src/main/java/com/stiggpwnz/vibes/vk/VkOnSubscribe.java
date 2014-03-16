package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Result;

import lombok.RequiredArgsConstructor;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

/**
 * Created by adel on 08/03/14
 */
@RequiredArgsConstructor(suppressConstructorProperties = true)
public class VkOnSubscribe<T> implements Observable.OnSubscribe<T> {
    final VKAuth           vkAuth;
    final Func0<Result<T>> func;

    @Override public void call(Subscriber<? super T> subscriber) {
        Result<T> result = func.call();
        if (result.getError() != null) {
            if (result.getError().isAuthError()) {
                vkAuth.resetAuth();
                if (!subscriber.isUnsubscribed()) {
                    call(subscriber);
                }
            } else {
                subscriber.onError(result.getError());
            }
        } else {
            subscriber.onNext(result.getResponse());
            subscriber.onCompleted();
        }
    }
}
