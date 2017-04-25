package com.hdu.even.baidumap;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.PoiOverlay;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Even on 2017/2/20.
 */

public class PoiSearchActivity extends FragmentActivity implements
        OnGetSuggestionResultListener,OnGetPoiSearchResultListener{
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private BaiduMap mBaiduMap = null;
    private List<String> suggest;

    //搜索窗口
    private EditText mDepartureCity = null;
    private EditText mDestinationCity = null;
    private AutoCompleteTextView mDepartureAddress = null;
    private AutoCompleteTextView mDestinationAddress = null;
    //提示窗口
    private ArrayAdapter<String> mDepartureSugAdapter =null;
    private ArrayAdapter<String> mDestinationSugAdapter = null;
    private int loadIndex = 0;//加载的页面数量

    //出发地和目的地logo
    private BitmapDescriptor departureIcon =
            BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
    private BitmapDescriptor destinationIcon =
            BitmapDescriptorFactory.fromResource(R.drawable.icon_en);

    //??
    private InfoWindow mInfoWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_search);
        //初始化搜索模块
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        //初始化建议搜索模块
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        //初始化输入框
        mDepartureCity = (EditText) findViewById(R.id.poi_departure_city);
        mDestinationCity = (EditText) findViewById(R.id.poi_destination_city);
        mDepartureAddress = (AutoCompleteTextView) findViewById(R.id.poi_departure_address);
        mDestinationAddress = (AutoCompleteTextView) findViewById(R.id.poi_destination_address);

        //设置适配器adapter
        mDepartureSugAdapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_dropdown_item_1line);
        mDepartureAddress.setAdapter(mDepartureSugAdapter);
        mDepartureAddress.setThreshold(1);//出现提示的最小字符

        mDestinationSugAdapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_dropdown_item_1line);
        mDestinationAddress.setAdapter(mDestinationSugAdapter);
        mDestinationAddress.setThreshold(1);

        mBaiduMap = ((SupportMapFragment)
                (getSupportFragmentManager().findFragmentById(R.id.poi_search_map_view)))
                .getBaiduMap();
        //设置缩放级别
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build()));

        //输入的关键字变化时，动态更新建议列表
        mDepartureAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()<=0){
                    return;
                }
                //使用建议搜索服务获取建议列表
                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(s.toString()).city(mDepartureCity.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mDestinationAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()<=0){
                    return;
                }
                //使用建议搜索服务获取建议列表
                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(s.toString()).city(mDestinationCity.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Button点击事件
        Button mDepartureSearch = (Button) findViewById(R.id.poi_departure_search);
        mDepartureSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButtonProcess(v);
                //确定出发点点击事件
                mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        Button button = new Button(getApplicationContext());
                        button.setBackgroundResource(R.drawable.popup);
                        button.setText("设为出发地");
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                marker.setIcon(departureIcon);
                                mBaiduMap.hideInfoWindow();
                            }
                        });
                        LatLng ll = marker.getPosition();
                        mInfoWindow = new InfoWindow(button, ll, -47);
                        mBaiduMap.showInfoWindow(mInfoWindow);
                        return true;
                    }
                });
            }
        });

        Button mDestinationSearch =(Button) findViewById(R.id.poi_destination_search);
        mDestinationSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButtonProcess(v);
                //确定目的地点击事件
                mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        Button button = new Button(getApplicationContext());
                        button.setBackgroundResource(R.drawable.popup);
                        button.setText("设为目的地");
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                marker.setIcon(destinationIcon);
                                mBaiduMap.hideInfoWindow();
                            }
                        });
                        LatLng ll = marker.getPosition();
                        mInfoWindow = new InfoWindow(button, ll, -47);
                        mBaiduMap.showInfoWindow(mInfoWindow);
                        return true;
                    }
                });
            }
        });


    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        //资源回收
        departureIcon.recycle();
        destinationIcon.recycle();
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
        super.onDestroy();
    }

    //响应搜索按钮点击事件
    public void searchButtonProcess(View v){
        if(v.getId() == R.id.poi_departure_search){
            String cityStr = mDepartureCity.getText().toString();
            String addressStr = mDepartureAddress.getText().toString();
            mPoiSearch.searchInCity((new PoiCitySearchOption())
                    .city(cityStr).keyword(addressStr).pageNum(loadIndex));
        }else if(v.getId() == R.id.poi_destination_search){
            String cityStr = mDestinationCity.getText().toString();
            String addressStr = mDestinationAddress.getText().toString();
            mPoiSearch.searchInCity((new PoiCitySearchOption())
                    .city(cityStr).keyword(addressStr).pageNum(loadIndex));
        }
    }

    //获取POI搜索结果
    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if(poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND){
            Toast.makeText(PoiSearchActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        //在市区内找到相关地点
        if(poiResult.error == SearchResult.ERRORNO.NO_ERROR){
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(poiResult);
            overlay.addToMap();
            overlay.zoomToSpan();//????
        }
        //当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
        if(poiResult.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD){
            String strInfo = "在";
            for (CityInfo cityInfo : poiResult.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(PoiSearchActivity.this, strInfo, Toast.LENGTH_LONG).show();
        }
    }

    //获POI详情搜索结果，得到searchPoiDetail返回的搜索结果
    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        if(poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR){
            Toast.makeText(PoiSearchActivity.this,"抱歉，未找到结果",Toast.LENGTH_SHORT).show();
        }else{
            String info = poiDetailResult.getName()+": "+poiDetailResult.getAddress();
            Toast.makeText(PoiSearchActivity.this,info,Toast.LENGTH_LONG).show();
        }
    }

    //返回室内搜索结果
    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    //获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
    @Override
    public void onGetSuggestionResult(SuggestionResult result) {
        if(result == null || result.getAllSuggestions() == null){
            return;
        }
        suggest = new ArrayList<>();
        for(SuggestionResult.SuggestionInfo info : result.getAllSuggestions()){
            if(info.key != null){
                suggest.add(info.key);
            }
        }
        mDepartureSugAdapter = new ArrayAdapter<String>
                (PoiSearchActivity.this,android.R.layout.simple_dropdown_item_1line,suggest);
        mDepartureAddress.setAdapter(mDepartureSugAdapter);
        mDepartureSugAdapter.notifyDataSetChanged();

        mDestinationSugAdapter = new ArrayAdapter<String>
                (PoiSearchActivity.this,android.R.layout.simple_dropdown_item_1line,suggest);
        mDestinationAddress.setAdapter(mDestinationSugAdapter);
        mDestinationSugAdapter.notifyDataSetChanged();
    }


    //自定义覆盖物类
    private class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap baiduMap){
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poiInfo = getPoiResult().getAllPoi().get(index);//得到所有相关的poi
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poiInfo.uid));//??
            return true;
        }
    }

}
