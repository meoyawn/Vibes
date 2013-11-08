package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.util.Persistence;
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

    public Observable<Feed> getNewsFeed(final int offset) {
        return Observable.create(new Observable.OnSubscribeFunc<Feed>() {

            @Override
            public Subscription onSubscribe(Observer<? super Feed> observer) {
                try {
                    Feed.Response newsFeed = vkApiLazy.get().getNewsFeed(offset);
                    process(observer, newsFeed);
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
                return Subscriptions.empty();
            }
        }).retry(1).map(filterAndPrepareFeed);
    }

    public Observable<Feed> getWall(final int ownerId, final String filter, final int offset) {
        return Observable.create(new Observable.OnSubscribeFunc<Feed>() {

            @Override
            public Subscription onSubscribe(Observer<? super Feed> observer) {
                try {
                    Feed.Response newsFeed = vkApiLazy.get().getWall(ownerId, filter, offset);
                    process(observer, newsFeed);
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
        }).map(filterAndPrepareFeed);
    }

    final Func1<Feed, Feed> filterAndPrepareFeed = new Func1<Feed, Feed>() {

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

    <T> void process(Observer<? super T> observer, Result<T> result) {
        if (result.isError()) {
            if (result.error.isAuthError()) {
                persistenceLazy.get().resetAuth();
            }
            observer.onError(result.error);
        } else {
            observer.onNext(result.getResponse());
        }
    }
}
