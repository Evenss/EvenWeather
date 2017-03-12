package com.hdu.even.evenweather.gson;

/**
 * Created by Even on 2017/3/12.
 * AQI类
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
