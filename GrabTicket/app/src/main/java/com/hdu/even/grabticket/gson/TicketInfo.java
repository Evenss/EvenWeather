package com.hdu.even.grabticket.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Even on 2017/4/10.
 */

public class TicketInfo {
    public boolean flag;

    public String dptStation;
    public String arrStation;
    public String dptDate;

    @SerializedName("s2sBeanList")
    public List<TicketList> ticketLists;

    public boolean sameCity;
}
