package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Post;
import com.stiggpwnz.vibes.vk.models.Result;

import java.util.ArrayList;
import java.util.List;

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
        return Observable.create(new Observable.OnSubscribeFunc<Audio[]>() {

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
        return Observable.create(new Observable.OnSubscribeFunc<Audio>() {
            @Override
            public Subscription onSubscribe(Observer<? super Audio> observer) {
                try {
                    Audio.UrlResponse response = vkApiLazy.get().getAudioURL(audio.getAudios());
                    process(observer, response);
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1);
    }

    public Observable<Audio[]> searchAudios(final String query) {
        return Observable.create(new Observable.OnSubscribeFunc<Audio[]>() {
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
        }).map(new Func1<Audio[], Audio[]>() {
            @Override
            public Audio[] call(Audio[] audios) {
                Audio[] copy = new Audio[audios.length - 1];
                System.arraycopy(audios, 1, copy, 0, audios.length - 1);
                return copy;
            }
        }).retry(1);
    }


    <T> void process(Observer<? super T> observer, Result<T> result) {
        if (result.isError()) {
            if (result.error.isAuthError()) {
                vkAuthLazy.get().resetAuth();
            } else {
                Timber.e(result.error, "VK error code %d", result.error.error_code);
            }
            observer.onError(result.error);
        } else {
            observer.onNext(result.response);
        }
    }

    public Observable<Audio> getAudioById(final Audio audio) {
        return Observable.create(new Observable.OnSubscribeFunc<Audio[]>() {

            @Override
            public Subscription onSubscribe(Observer<? super Audio[]> observer) {
                try {
                    process(observer, vkApiLazy.get().getAudioById(audio.getAudios()));
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1).map(new Func1<Audio[], Audio>() {

            @Override
            public Audio call(Audio[] audios) {
                Audio audio = audios[0];
                Audio.URL_CACHE.put(audio, audio.url);
                return audio;
            }
        });
    }

    public Observable<Feed> getNewsFeed(final int offset) {
        return Observable.create(new Observable.OnSubscribeFunc<Feed>() {

            @Override
            public Subscription onSubscribe(Observer<? super Feed> observer) {
                try {
                    process(observer, vkApiLazy.get().getNewsFeed(offset));
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1).map(filterAudios());
    }

    public Observable<Feed> getWall(final int ownerId, final String filter, final int offset) {
        return Observable.create(new Observable.OnSubscribeFunc<Feed>() {

            @Override
            public Subscription onSubscribe(Observer<? super Feed> observer) {
                try {
                    process(observer, vkApiLazy.get().getWall(ownerId, filter, offset));
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1).map(new Func1<Feed, Feed>() {

            @Override
            public Feed call(Feed posts) {
                posts.items.remove(0);
                return posts;
            }
        }).map(filterAudios());
    }

    private Func1<Feed, Feed> filterAudios() {
        return new Func1<Feed, Feed>() {

            @Override
            public Feed call(Feed feed) {
                List<Post> posts = new ArrayList<Post>();
                for (Post post : feed.items) {
                    if (post.calculateAudiosAndPhotos()) {
                        feed.assignUnit(post);
                        posts.add(post);
                    }
                }
                feed.items = posts;
                return feed;
            }
        };
    }
}
