package com.hdu.even.baidumap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

/**
 * Created by Even on 2017/2/16.
 */

public class SelectLocationActivity extends Activity implements OnGetGeoCoderResultListener{
    GeoCoder mSearch = null;
    BaiduMap mBaiduMap = null;
    MapView mMapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        //初始化地图
        mMapView =  (MapView) findViewById(R.id.search_map_view);
        mBaiduMap = mMapView.getMap();
        //初始化搜索模块，注册监听事件
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        Button mDepartureSearch = (Button) findViewById(R.id.departure_search);
        mDepartureSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButtonProcess(v);
            }
        });

        Button mDestinationSearch =(Button) findViewById(R.id.destination_search);
        mDestinationSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButtonProcess(v);
            }
        });
    }
    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        mSearch.destroy();
        super.onDestroy();
    }

    public void searchButtonProcess(View view){
        if(view.getId() == R.id.departure_search){
            EditText mDepartureCity = (EditText) findViewById(R.id.departure_city);
            EditText mDepartureAddress = (EditText) findViewById(R.id.departure_address);
            String departureCity = mDepartureCity.getText().toString();
            String departureAddress = mDepartureAddress.getText().toString();
            //Geo搜索
            mSearch.geocode(new GeoCodeOption().city(departureCity).address(departureAddress));
        }
        else if(view.getId() == R.id.destination_search) {
            EditText mDestinationCity = (EditText) findViewById(R.id.destination_city);
            EditText mDestinationAddress = (EditText) findViewById(R.id.destination_address);
            String destinationCity = mDestinationCity.getText().toString();
            String destinationAddress = mDestinationAddress.getText().toString();
            //Geo搜索
            mSearch.geocode(new GeoCodeOption().city(destinationCity).address(destinationAddress));
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(SelectLocationActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions()
                .position(geoCodeResult.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_st)));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory
                .newLatLng(geoCodeResult.getLocation()));
        String strInfo = String.format("纬度：%f 经度：%f",
                geoCodeResult.getLocation().latitude, geoCodeResult.getLocation().longitude);
        Toast.makeText(SelectLocationActivity.this, strInfo, Toast.LENGTH_LONG).show();
    }

    //
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

    }
}
