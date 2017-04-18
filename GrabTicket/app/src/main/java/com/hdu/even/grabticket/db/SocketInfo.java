package com.hdu.even.grabticket.db;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Even on 2017/4/16.
 * 用于活动之间传递对象
 */

public class SocketInfo implements Parcelable {
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
    public static final Parcelable.Creator<SocketInfo> CREATOR = new Parcelable.Creator<SocketInfo>(){
        @Override
        public SocketInfo createFromParcel(Parcel source) {
            SocketInfo socketInfo = new SocketInfo();
            source.readList(socketInfo.trainNos,getClass().getClassLoader());
            socketInfo.date = source.readString();
            socketInfo.start = source.readString();
            socketInfo.end = source.readString();
            socketInfo.email = source.readString();
            source.readList(socketInfo.seats,getClass().getClassLoader());
            return socketInfo;
        }

        @Override
        public SocketInfo[] newArray(int size) {
            return new SocketInfo[size];
        }
    };
}
