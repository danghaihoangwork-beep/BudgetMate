package com.example.savemoneytime.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    @GET("search")
    Call<NewsResponse> getFinanceNews(
            @Query("q")      String query,
            @Query("lang")   String language,
            @Query("max")    int    maxResults,
            @Query("apikey") String apiKey
    );
}