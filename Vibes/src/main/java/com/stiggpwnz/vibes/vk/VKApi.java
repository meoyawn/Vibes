package com.stiggpwnz.vibes.vk;

import com.stiggpwnz.vibes.vk.models.NewsFeed;

import retrofit.http.GET;
import retrofit.http.Query;

public interface VKApi {

    public static final String SERVER = "https://api.vk.com/method";

    @GET("/newsfeed.get?filters=post&count=100")
    public NewsFeed.Result getNewsFeed(@Query("offset") int offset);
}
