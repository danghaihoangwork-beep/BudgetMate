package com.example.savemoneytime.network;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class StockResponse {

    @SerializedName("Time Series (Daily)")
    private Map<String, DayData> timeSeries;

    public Map<String, DayData> getTimeSeries() { return timeSeries; }

    public static class DayData {
        @SerializedName("1. open")   public String open;
        @SerializedName("2. high")   public String high;
        @SerializedName("3. low")    public String low;
        @SerializedName("4. close")  public String close;
        @SerializedName("5. volume") public String volume;
    }
}