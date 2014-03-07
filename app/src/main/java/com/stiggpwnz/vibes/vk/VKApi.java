package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Result;

import retrofit.http.GET;
import retrofit.http.Query;

public interface VKApi {
    public static final String SERVER = "https://api.vk.com/method";

    @GET("/newsfeed.get?filters=post")
    public Result<Feed> getNewsFeed(@Query("offset") int offset);

    @GET("/wall.get?extended=1")
    public Result<Feed> getWall(@Query("owner_id") int ownerId,
                                @Query("filter") String filter,
                                @Query("offset") int offset);

    @GET("/audio.get")
    public Result<Audio[]> getAudios();

    @GET("/audio.search?sort=2&count=300")
    public Result<Audio[]> searchAudios(@Query("q") String query);

    @GET("/audio.getById")
    public Result<Audio[]> getAudioURL(@Query("audios") String query);
}
