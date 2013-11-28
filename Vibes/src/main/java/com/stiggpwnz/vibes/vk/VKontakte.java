package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.util.Persistence;
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
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;

/**
 * Created by adel on 11/8/13
 */
@Singleton
public class VKontakte {

    Lazy<Persistence> persistenceLazy;
    Lazy<VKApi>       vkApiLazy;

    @Inject
    public VKontakte(Lazy<Persistence> persistenceLazy, Lazy<VKApi> vkApiLazy) {
        this.persistenceLazy = persistenceLazy;
        this.vkApiLazy = vkApiLazy;
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

    <T> void process(Observer<? super T> observer, Result<T> result) {
        if (result.isError()) {
            if (result.error.isAuthError()) {
                persistenceLazy.get().resetAuth();
            }
            observer.onError(result.error);
        } else {
            observer.onNext(result.response);
        }
    }
}
