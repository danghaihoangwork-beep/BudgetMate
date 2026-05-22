package com.example.savemoneytime.network;

import com.example.savemoneytime.model.CandleData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StockRetrofitClient {

    private static final String BASE_URL = "https://www.alphavantage.co/";
    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static StockApiService getStockService() {
        return getInstance().create(StockApiService.class);
    }

    public static List<CandleData> parseOHLC(StockResponse response) {
        List<CandleData> candles = new ArrayList<>();
        if (response == null || response.getTimeSeries() == null) return candles;

        Map<String, StockResponse.DayData> series = response.getTimeSeries();

        List<String> dates = new ArrayList<>(series.keySet());
        java.util.Collections.sort(dates);
        if (dates.size() > 30) {
            dates = dates.subList(dates.size() - 30, dates.size());
        }

        for (String date : dates) {
            StockResponse.DayData day = series.get(date);
            if (day != null) {
                candles.add(new CandleData(
                        date,
                        parseFloat(day.open),
                        parseFloat(day.high),
                        parseFloat(day.low),
                        parseFloat(day.close),
                        parseLong(day.volume)
                ));
            }
        }
        return candles;
    }

    private static float parseFloat(String s) {
        try { return Float.parseFloat(s != null ? s.trim() : "0"); }
        catch (Exception e) { return 0f; }
    }

    private static long parseLong(String s) {
        try { return Long.parseLong(s != null ? s.trim() : "0"); }
        catch (Exception e) { return 0L; }
    }
}