package com.hdu.even.evenweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Even on 2017/4/7.
 * 每天的天气预报总览，含今天
 */

public class Weathers {
    public String date;

    public String weather;

    public String week;

    @SerializedName("temp_day_c")
    public String highestTemp;

    @SerializedName("temp_night_c")
    public String lowerestTemp;

    @SerializedName("wd")
    public String windDirection;

    @SerializedName("ws")
    public String windSeries;
}
