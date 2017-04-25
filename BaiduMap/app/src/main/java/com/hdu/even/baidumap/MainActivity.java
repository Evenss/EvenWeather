package com.hdu.even.baidumap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {
    private Integer mDistance;
    RoutePlan routePlan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mLocationBtn = (Button) findViewById(R.id.location_btn);
        mLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,LocationActivity.class);
                startActivity(intent);
            }
        });
        Button mSelectLocationBtn = (Button) findViewById(R.id.selectLocation_btn);
        mSelectLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SelectLocationActivity.class);
                startActivity(intent);
            }
        });
        Button mPoiSearchBtn = (Button) findViewById(R.id.poi_search_btn);
        mPoiSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PoiSearchActivity.class);
                startActivity(intent);
            }
        });
        Button mSearchBtn = (Button) findViewById(R.id.search_btn);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MapLocationActivity.class);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    SharedPreferences pref = getSharedPreferences("location",MODE_PRIVATE);
                    LocationBean location = new LocationBean(
                            pref.getString("city",""),
                            pref.getString("address",""),
                            pref.getFloat("lng",0),
                            pref.getFloat("lat",0));
                    Log.d("return_data",location.toString());
                    sendRequestData();
                }
        }
    }

    private void sendRequestData(){
        try {
            LatLng stLatLng = new LatLng(30.324604034423828,120.35021209716797);
            LatLng enLatLng = new LatLng(30.285778045654297,120.17228698730469);

            routePlan = new RoutePlan();
            routePlan.getDistance(stLatLng, enLatLng ,new DistanceCallBack() {
                @Override
                public void onDataReceiveSuccess(Integer distance) {
                    mDistance = distance;
                    Log.d("RouteResult",mDistance.toString());
                }

                @Override
                public void onDataReceiveFailed(Exception e) {
                    Log.d("return","failed");
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
