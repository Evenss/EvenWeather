package com.hdu.even.evenweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hdu.even.evenweather.gson.Weather;
import com.hdu.even.evenweather.gson.Weathers;
import com.hdu.even.evenweather.service.AutoUpdateService;
import com.hdu.even.evenweather.util.HttpUtil;
import com.hdu.even.evenweather.util.Utility;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    //private static final String URL = "http://guolin.tech/api/weather?cityid=";
    private static final String URL = "http://aider.meizu.com/app/weather/listWeather?cityIds=";
    private static final String KEY = "&key=aba0fc977a56484cbc908a56d537604c";
    private static final String BINGPIC_URL = "http://guolin.tech/api/bing_pic";

    public DrawerLayout drawerLayout;
    private ScrollView weatherLayout;
    //标题栏
    private TextView titleCity;
    //当前日期情况
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView sendibleDegreeText;
    private TextView weatherInfoText;

    private LinearLayout forecastLayout;
    //空气质量
    private TextView qualityText;
    private TextView pm25Text;
    private TextView cityRankText;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        alterTitleBar();
        init();
        load();

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);//????
            }
        });
    }

    /**
     * 修改标题栏和导航栏为透明
     */
    private void alterTitleBar(){
        if(Build.VERSION.SDK_INT >=21){
            Window window = getWindow();//??????
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private void load(){
        //设置下拉刷新进度条颜色
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        //加载必应的图片
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
        if(weatherString != null){
            //存在本地缓存
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.weatherId.toString();
            showWeatherInfo(weather);
        }else{
            //本地无缓存，去服务器查询
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
    }

    private void init(){
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        qualityText = (TextView)findViewById(R.id.quality_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        navButton = (Button)findViewById(R.id.nav_button);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        sendibleDegreeText = (TextView)findViewById(R.id.sendible_degree_text);
        cityRankText = (TextView)findViewById(R.id.cityrank);
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic(){
        HttpUtil.sendOkHttpRequest(BINGPIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
    /**
     * 处理并展示weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather){
        String cityName = weather.cityName;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String currentTime = simpleDateFormat.format(new Date());
        String updateTime = currentTime;
        String degree = weather.realtime.temp + "°C";
        String weatherInfo = weather.realtime.weather;
        String sendibleDegree = weather.realtime.sendibleTemp;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime + " 刷新");
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        sendibleDegreeText.setText("体感 " + sendibleDegree + "°");

        forecastLayout.removeAllViews();
        for(Weathers weathers: weather.weatherList){
            View view = LayoutInflater
                    .from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dataText.setText(weathers.week);
            infoText.setText(weathers.weather);
            maxText.setText(weathers.highestTemp);
            minText.setText(weathers.lowerestTemp);
            forecastLayout.addView(view);
        }

        if(weather.pm25 != null){
            qualityText.setText(weather.pm25.quality);
            pm25Text.setText(weather.pm25.pm25);
            cityRankText.setText(weather.pm25.cityrank.toString());
        }
        String comfort = weather.suggestionList.get(24).name +
                ":  "+weather.suggestionList.get(24).content;
        String carWash = weather.suggestionList.get(5).name +
                ":  "+weather.suggestionList.get(5).content;
        String sport = weather.suggestionList.get(3).name +
                ":  "+weather.suggestionList.get(3).content;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 根据天气Id请求城市天气信息
     */
    public void requestWeather(final String weatherId){
        //String weatherUrl = URL + weatherId + KEY;
        String weatherUrl = URL + weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,
                                "获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null){
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.weatherId.toString();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,
                                    "获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }
}
