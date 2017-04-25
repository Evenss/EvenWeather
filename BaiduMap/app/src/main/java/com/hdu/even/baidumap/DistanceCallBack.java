package com.hdu.even.baidumap;

/**
 * Created by Even on 2017/2/26.
 * 异步线程返回值接口类
 */

public interface DistanceCallBack {
    void onDataReceiveSuccess(Integer distance);
    void onDataReceiveFailed(Exception e);
}
