package com.hdu.even.evenweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Even on 2017/3/12.
 * 基础信息类
 */

public class Basic {
    //用注解方式让JSON字段和Java字段之间建立映射关系
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }


}
