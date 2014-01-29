package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import rx.Observable;

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
        return wrapRetryAuth(vkApiLazy.get().getAudios());
    }

    public Observable<Audio> getAudioUrl(Audio audio) {
        return wrapRetryAuth(vkApiLazy.get().getAudioURL(audio.ownerIdAidParam()));
    }

    public Observable<Audio[]> searchAudios(String query) {
        return wrapRetryAuth(vkApiLazy.get().searchAudios(query))
                .map(Audio.removeFirstItem());
    }

    public Observable<Feed> getNewsFeed(int offset) {
        return wrapRetryAuth(vkApiLazy.get().getNewsFeed(offset))
                .map(Feed.filterAudios());
    }

    public Observable<Feed> getWall(int ownerId, String filter, int offset) {
        return wrapRetryAuth(vkApiLazy.get().getWall(ownerId, filter, offset))
                .map(Feed.removeFirstItem())
                .map(Feed.filterAudios());
    }

    <T> Observable<T> wrapRetryAuth(Observable<? extends Result<T>> observable) {
        return Observable.create(new RetryAuthObservable<>(observable, vkAuthLazy));
    }
}
