package com.hdu.even.grabticket.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Even on 2017/4/10.
 */

public class UserInfo extends DataSupport {
    private int id;
    private String start;
    private String end;
    private String date;
    private String email;

    private boolean isOnlyGD;

    public int getId() {
        return id;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getDate() {
        return date;
    }

    public String getEmail() {
        return email;
    }

    public boolean isOnlyGD() {
        return isOnlyGD;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOnlyGD(boolean onlyGD) {
        isOnlyGD = onlyGD;
    }
}
