package com.hdu.even.evenweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Even on 2017/4/7.
 * 当前温度
 */

public class RealTime {
    public String sendibleTemp;

    public String temp;

    public String time;//更新时间

    public String weather;

    @SerializedName("wD")
    public String windDirection;

    @SerializedName("wS")
    public String windSeries;
}
