package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.BooleanSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;
import timber.log.Timber;

import static rx.Observable.OnSubscribeFunc;

/**
 * Created by adel on 11/8/13
 */
@Singleton
public class VKontakte {

    Lazy<VKApi>  vkApiLazy;
    Lazy<VKAuth> vkAuthLazy;

    @Inject
    public VKontakte(Lazy<VKAuth> vkAuthLazy, Lazy<VKApi> vkApiLazy) {
        this.vkAuthLazy = vkAuthLazy;
        this.vkApiLazy = vkApiLazy;
    }

    public Observable<Audio[]> getAudios() {
        return Observable.create(new OnSubscribeFunc<Audio[]>() {

            @Override
            public Subscription onSubscribe(Observer<? super Audio[]> observer) {
                try {
                    Audio.Response response = vkApiLazy.get().getAudios();
                    process(observer, response);
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1);
    }

    public Observable<Audio> getUrl(final Audio audio) {
        return Observable.create(new OnSubscribeFunc<Audio>() {
            @Override
            public Subscription onSubscribe(Observer<? super Audio> observer) {
                try {
                    Audio.UrlResponse response = vkApiLazy.get().getAudioURL(audio.ownerIdAidParam());
                    process(observer, response);
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1);
    }

    public Observable<Audio[]> searchAudios(final String query) {
        return Observable.create(new OnSubscribeFunc<Audio[]>() {
            @Override
            public Subscription onSubscribe(Observer<? super Audio[]> observer) {
                BooleanSubscription subscription = new BooleanSubscription();
                try {
                    Audio.Response response = vkApiLazy.get().searchAudios(query);
                    if (!subscription.isUnsubscribed()) {
                        process(observer, response);
                    }
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return subscription;
            }
        }).map(Audio.removeFirstItem()).retry(1);
    }

    <T> void process(Observer<? super T> observer, Result<T> result) {
        if (result.isError()) {
            if (result.error.isAuthError()) {
                vkAuthLazy.get().resetAuth();
            } else {
                Timber.e(result.error, "VK error code %d", result.error.getErrorCode());
            }
            observer.onError(result.error);
        } else {
            observer.onNext(result.response);
        }
    }

    public Observable<Audio> getAudioById(final Audio audio) {
        return Observable.create(new OnSubscribeFunc<Audio[]>() {
            @Override
            public Subscription onSubscribe(Observer<? super Audio[]> observer) {
                try {
                    process(observer, vkApiLazy.get().getAudioById(audio.ownerIdAidParam()));
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1).map(new Func1<Audio[], Audio>() {
            @Override
            public Audio call(Audio[] audios) {
                Audio audio = audios[0];
                // TODO insert into URL cache
                return audio;
            }
        });
    }

    public Observable<Feed> getNewsFeed(final int offset) {
        return Observable.create(new OnSubscribeFunc<Feed>() {

            @Override
            public Subscription onSubscribe(Observer<? super Feed> observer) {
                try {
                    process(observer, vkApiLazy.get().getNewsFeed(offset));
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1).map(Feed.filterAudios());
    }

    public Observable<Feed> getWall(final int ownerId, final String filter, final int offset) {
        return Observable.create(new OnSubscribeFunc<Feed>() {

            @Override
            public Subscription onSubscribe(Observer<? super Feed> observer) {
                try {
                    process(observer, vkApiLazy.get().getWall(ownerId, filter, offset));
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1).map(Feed.removeFirstItem()).map(Feed.filterAudios());
    }
}
