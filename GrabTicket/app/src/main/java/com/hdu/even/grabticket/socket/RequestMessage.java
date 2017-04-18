package com.hdu.even.grabticket.socket;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Even on 2017/4/16.
 * 数据请求模板类RequestMessage（主要用来封装发给服务器端的数据）
 */

public class RequestMessage {
    private Integer code;//操作码
    private Map<String, String> data;//传送给服务器端的数据（车次、日期、出发地、目的地、email、seats）
    private ArrayList<String> trainNos;
    private ArrayList<String> seats;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public ArrayList<String> getTrainNos() {
        return trainNos;
    }

    public void setTrainNos(ArrayList<String> trainNos) {
        this.trainNos = trainNos;
    }

    public ArrayList<String> getSeats() {
        return seats;
    }

    public void setSeats(ArrayList<String> seats) {
        this.seats = seats;
    }
}
