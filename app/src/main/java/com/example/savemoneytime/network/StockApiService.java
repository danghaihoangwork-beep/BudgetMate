package com.example.savemoneytime.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockApiService {

    @GET("query")
    Call<StockResponse> getDailyOHLC(
            @Query("function") String function,
            @Query("symbol")   String symbol,
            @Query("outputsize") String outputSize,
            @Query("apikey")   String apiKey
    );
}