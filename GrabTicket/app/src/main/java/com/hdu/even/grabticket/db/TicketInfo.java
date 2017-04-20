package com.hdu.even.grabticket.db;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Even on 2017/4/16.
 * 用于活动之间传递对象
 */

public class TicketInfo implements Parcelable {
    public ArrayList<String> trainNos = new ArrayList<>();
    public String date;
    public String start;
    public String end;
    public String email;
    public ArrayList<String> seats = new ArrayList<>();
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(trainNos);
        dest.writeString(date);
        dest.writeString(start);
        dest.writeString(end);
        dest.writeString(email);
        dest.writeList(seats);
    }
    public static final Parcelable.Creator<TicketInfo> CREATOR = new Parcelable.Creator<TicketInfo>(){
        @Override
        public TicketInfo createFromParcel(Parcel source) {
            TicketInfo ticketInfo = new TicketInfo();
            source.readList(ticketInfo.trainNos,getClass().getClassLoader());
            ticketInfo.date = source.readString();
            ticketInfo.start = source.readString();
            ticketInfo.end = source.readString();
            ticketInfo.email = source.readString();
            source.readList(ticketInfo.seats,getClass().getClassLoader());
            return ticketInfo;
        }

        @Override
        public TicketInfo[] newArray(int size) {
            return new TicketInfo[size];
        }
    };
}
