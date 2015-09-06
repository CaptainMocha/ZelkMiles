package com.aktsa.zelkmiles.DailymileAPI;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by cheek on 8/14/2015.
 */
public interface DailymileService {
    @GET("/people/{username}/entries.json")
    Stream streamUser(@Path("username") String username, @Query("oauth_token") String oauth_token, @Query("page") int page);

    @GET("/entries/friends.json")
    Stream streamFriends(@Query("oauth_token") String oauth_token, @Query("page") int page);

    @GET("/entries.json")
    Stream streamPublic(@Query("page") int page);

    @GET("/entries/nearby/{lat},{lon}.json")
    Stream streamNearby(@Path("lat") double latitude, @Path("lon") double longitude, @Query("page") int page);

    @GET("/entries/popular.json")
    Stream streamPopular(@Query("page") int page);

    @POST("/entries.json")
    void postWorkout(@Body PostEntry entry, @Query("oauth_token") String oauth_token, Callback<Entry> cb);

    @POST("/people/me.json")
    void getSelf(@Query("oauth_token") String oauth_token, Callback<TokenOwner> cb);
}
