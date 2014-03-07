package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Result;

import lombok.RequiredArgsConstructor;
import rx.Observable;
import rx.functions.Func0;

/**
 * Created by adel on 11/8/13
 */
@RequiredArgsConstructor(suppressConstructorProperties = true)
public class VKontakte {
    final VKApi  vkApi;
    final VKAuth vkAuth;

    public Observable<Audio[]> getAudios() {
        return vkObservableFrom(vkApi::getAudios);
    }

    public Observable<Audio> getAudioUrl(Audio audio) {
        return vkObservableFrom(() -> vkApi.getAudioURL(audio.ownerIdAidParam()))
                .map(array -> array[0]);
    }

    public Observable<Audio[]> searchAudios(String query) {
        return vkObservableFrom(() -> vkApi.searchAudios(query))
                .map(Audio.removeFirstItem());
    }

    public Observable<Feed> getNewsFeed(int offset) {
        return vkObservableFrom(() -> vkApi.getNewsFeed(offset))
                .doOnNext(Feed::filterAudios);
    }

    @SuppressWarnings("all") <T> Observable<T> vkObservableFrom(Func0<Result<T>> func) {
        return Observable.create(new VkOnSubscribe<T>(vkAuth, func));
    }

    public Observable<Feed> getWall(int ownerId, String filter, int offset) {
        return vkObservableFrom(() -> vkApi.getWall(ownerId, filter, offset))
                .doOnNext(Feed::removeFirstItem)
                .doOnNext(Feed::filterAudios);
    }
}
