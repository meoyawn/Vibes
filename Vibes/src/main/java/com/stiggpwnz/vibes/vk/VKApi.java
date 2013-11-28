package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.Feed;

import retrofit.http.GET;
import retrofit.http.Query;

public interface VKApi {

    public static final String SERVER = "https://api.vk.com/method";

    @GET("/newsfeed.get?filters=post")
    public Feed.Response getNewsFeed(@Query("offset") int offset);

    @GET("/wall.get?extended=1")
    public Feed.Response getWall(@Query("owner_id") int ownerId, @Query("filter") String filter, @Query("offset") int offset);

    @GET("/audio.getById")
    public Audio.Response getAudioById(@Query("audios") String audios);
}
