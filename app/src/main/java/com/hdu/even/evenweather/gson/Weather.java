package com.hdu.even.evenweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Even on 2017/3/12.
 * 总的天气类
 */

public class Weather {
    public String status;

    public AQI aqi;

    public Basic basic;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
