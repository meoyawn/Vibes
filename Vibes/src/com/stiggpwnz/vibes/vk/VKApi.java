package com.stiggpwnz.vibes.vk;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface VKApi {

	public static final String SERVER = "https://api.vk.com/method";

	@GET("/newsfeed.get?filters=post")
	public NewsFeed.Result getNewsFeed(@Query("offset") int offset, @Query("access_token") String accessToken);

	@GET("/newsfeed.get?filters=post")
	public void getNewsFeed(@Query("offset") int offset, @Query("access_token") String accessToken, Callback<NewsFeed.Result> cb);
}
