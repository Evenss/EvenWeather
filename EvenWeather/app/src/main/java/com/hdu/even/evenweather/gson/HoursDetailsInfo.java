package com.hdu.even.evenweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Even on 2017/4/7.
 * 3小时内天气的详细情况
 */

public class HoursDetailsInfo {
    public String startTime;

    public String endTime;

    public String highestTemperature;

    public String lowerestTemperature;

    public String weather;

    @SerializedName("wd")
    public String windDirection;

    @SerializedName("ws")
    public String windSeries;
}
