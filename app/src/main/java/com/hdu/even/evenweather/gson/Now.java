package com.hdu.even.evenweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Even on 2017/3/12.
 * 当前温度和天气描述类
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
