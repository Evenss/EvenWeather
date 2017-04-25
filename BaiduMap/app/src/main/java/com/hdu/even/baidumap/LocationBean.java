package com.hdu.even.baidumap;

/**
 * Created by Even on 2017/2/21.
 * 封装的关于地理位置的类
 */

public class LocationBean {
    private String city;
    private String address;
    private double lng;//经度
    private double lat;//纬度

    LocationBean(){
        this.city = "";
        this.address = "";
        this.lng = 0.0;
        this.lat = 0.0;
    }

    LocationBean(String city,String address,double lng,double lat){
        this.city = city;
        this.address = address;
        this.lng = lng;
        this.lat = lat;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public double getLng() {
        return lng;
    }

    public double getLat() {
        return lat;
    }

    @Override
    public String toString() {
        return "LocationBean{" +
                "city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", lng=" + lng +
                ", lat=" + lat +
                '}';
    }
}
