package com.hdu.even.grabticket.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Even on 2017/4/10.
 */

public class TicketList {
    public Seats seats;

    public String trainNo;

    public String dptStationName;
    public String arrStationName;
    public String dptTime;
    public String arrTime;

    @SerializedName("extraBeanMap")
    public ExtraTicketInfo extraTicketInfo;
}
