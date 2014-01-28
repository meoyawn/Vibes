package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.Feed;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface VKApi {

    public static final String SERVER = "https://api.vk.com/method";

    @GET("/newsfeed.get?filters=post")
    public Observable<Feed.Response> getNewsFeed(@Query("offset") int offset);

    @GET("/wall.get?extended=1")
    public Observable<Feed.Response> getWall(@Query("owner_id") int ownerId,
                                             @Query("filter") String filter,
                                             @Query("offset") int offset);

    @GET("/audio.get")
    public Observable<Audio.Response> getAudios();

    @GET("/audio.search?sort=2&count=300")
    public Observable<Audio.Response> searchAudios(@Query("q") String query);

    @GET("/audio.getById")
    public Observable<Audio.UrlResponse> getAudioURL(@Query("audios") String query);
}
