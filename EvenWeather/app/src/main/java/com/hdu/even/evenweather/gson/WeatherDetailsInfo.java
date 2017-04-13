package com.hdu.even.evenweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Even on 2017/4/7.
 */

public class WeatherDetailsInfo {
    public String publishTime;

    @SerializedName("weather3HoursDetailsInfos")
    public List<HoursDetailsInfo> weather3HoursList;
}
