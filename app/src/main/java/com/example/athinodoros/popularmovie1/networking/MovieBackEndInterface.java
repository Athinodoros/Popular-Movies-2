package com.example.athinodoros.popularmovie1.networking;

import com.example.athinodoros.popularmovie1.model.FullResponseObject;
import com.example.athinodoros.popularmovie1.model.ReviewResponse;
import com.example.athinodoros.popularmovie1.model.VideosResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Athinodoros on 3/25/2017.
 */

public interface MovieBackEndInterface {

    @GET("/3/movie/{ordering}")
    Call<FullResponseObject> getMovies(
            @Path("ordering") String ordering, @Query("api_key") String apiKey,
            @Query("page") int page);

    @GET("/3/movie/{id}/videos")
    Call<VideosResponse> getVideos(
            @Path("id") int id,@Query("api_key") String apiKey
    );

    @GET("/3/movie/{id}/reviews")
    Call<ReviewResponse> getReviews(
            @Path("id") int id, @Query("api_key") String apiKey
    );
}
