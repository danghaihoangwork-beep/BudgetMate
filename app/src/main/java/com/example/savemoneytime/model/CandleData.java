package com.example.savemoneytime.model;

/**
 * OHLC data for one candle
 * Open, High, Low, Close + Volume
 */
public class CandleData {

    private final String date;
    private final float  open;
    private final float  high;
    private final float  low;
    private final float  close;
    private final long   volume;

    public CandleData(String date, float open, float high,
                      float low, float close, long volume) {
        this.date   = date;
        this.open   = open;
        this.high   = high;
        this.low    = low;
        this.close  = close;
        this.volume = volume;
    }

    public String getDate()   { return date; }
    public float  getOpen()   { return open; }
    public float  getHigh()   { return high; }
    public float  getLow()    { return low; }
    public float  getClose()  { return close; }
    public long   getVolume() { return volume; }

    public boolean isBullish() { return close >= open; }
}