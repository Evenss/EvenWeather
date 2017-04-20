package com.hdu.even.grabticket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.bigkoo.pickerview.OptionsPickerView;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.hdu.even.grabticket.db.UserInfo;
import com.zaaach.citypicker.CityPickerActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class GrabTicketActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener{
    //UI
    private Button mStart;
    private Button mEnd;
    private ImageButton mExchange;
    private Button mSelectDate;
    private Button mSelectTime;
    private Button mSeatLevel;
    private CheckBox isOnlyGD;
    private Button mQueryTicket;
    private Toolbar mToolbar;

    //date&time
    public static final String DATEPICKER_TAG = "datepicker";
    private final Calendar calendar = Calendar.getInstance();
    private DatePickerDialog datePickerDialog;
    private OptionsPickerView timePickerView;
    private ArrayList<String> timeItem = new ArrayList<>();

    //seat type
    private OptionsPickerView seatPickerView;
    private ArrayList<String> seatItem = new ArrayList<>();

    //map
    private static final int REQUEST_CODE_START_CITY = 0;
    private static final int REQUEST_CODE_END_CITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_grab_ticket);
        initView();
        setListeners();
        initTimePicker();
        initSeatType();
        datePickerDialog = DatePickerDialog.newInstance(this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        false);

        mToolbar.inflateMenu(R.menu.menu);
        //??
        if (savedInstanceState != null) {
            DatePickerDialog dpd =
                    (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATEPICKER_TAG);
            if (dpd != null) {
                dpd.setOnDateSetListener(this);
            }
        }

    }

    void initView(){
        mStart = (Button)findViewById(R.id.start);
        mEnd = (Button)findViewById(R.id.end);
        mExchange = (ImageButton) findViewById(R.id.exchange);
        mSelectDate = (Button)findViewById(R.id.select_date);
        mSelectTime = (Button)findViewById(R.id.select_time);
        mSeatLevel = (Button)findViewById(R.id.seat_level);
        isOnlyGD = (CheckBox) findViewById(R.id.only_GD);
        mQueryTicket = (Button)findViewById(R.id.query_ticket);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }
    void setListeners(){
        mStart.setOnClickListener(this);
        mEnd.setOnClickListener(this);
        mExchange.setOnClickListener(this);
        mSelectDate.setOnClickListener(this);
        mSelectTime.setOnClickListener(this);
        mSeatLevel.setOnClickListener(this);
        mQueryTicket.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                startActivityForResult(new Intent(this, CityPickerActivity.class),
                        REQUEST_CODE_START_CITY);
                break;

            case R.id.end:
                startActivityForResult(new Intent(this, CityPickerActivity.class),
                        REQUEST_CODE_END_CITY);
                break;

            case R.id.exchange:
                String temp = mStart.getText().toString();
                mStart.setText(mEnd.getText().toString());
                mEnd.setText(temp);
                break;

            case R.id.select_date:
                selectDate();
                break;

            case R.id.select_time:
                timePickerView.show();
                break;

            case R.id.seat_level:
                seatPickerView.show();
                break;

            case R.id.query_ticket:
                queryTicket();
                break;
        }
    }

    //日期选择
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        month++;
        String monthStr = String.valueOf(month);
        String dayStr = String.valueOf(day);
        if(month < 10){
            monthStr = "0" + month;
        }
        if(day < 10){
            dayStr = "0" + day;
        }
        mSelectDate.setText(year + "-" + monthStr + "-" + dayStr);
    }

    //打开日期选择框
    private void selectDate(){
        datePickerDialog.setVibrate(false);
        datePickerDialog.setYearRange(2017, 2020);
        datePickerDialog.setCloseOnSingleTapDay(false);
        datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
    }

    //时间选择器初始化
    private void initTimePicker(){
        timePickerView = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String str = timeItem.get(options1);
                mSelectTime.setText(str);
            }
        })
                .setContentTextSize(23)//滚轮文字大小
                .isDialog(true)
                .build();
        timeItem.add("上午");
        timeItem.add("下午");
        timePickerView.setPicker(timeItem);//添加数据
    }

    //席位类别初始化
    private void initSeatType(){
        seatPickerView = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String str = seatItem.get(options1);
                mSeatLevel.setText(str);
            }
        })
                .setContentTextSize(23)//滚轮文字大小
                .isDialog(true)
                .build();
        seatItem.add("不限");
        seatItem.add("一等座");
        seatItem.add("二等座");
        seatItem.add("无座");
        seatPickerView.setPicker(seatItem);//添加数据

    }

    //城市选择返回结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE_START_CITY:
                    if(data != null){
                        String city = data.getStringExtra(CityPickerActivity.KEY_PICKED_CITY);
                        mStart.setText(city);
                    }
                    break;
                case REQUEST_CODE_END_CITY:
                    if(data != null){
                        String city = data.getStringExtra(CityPickerActivity.KEY_PICKED_CITY);
                        mEnd.setText(city);
                    }
                    break;
            }
        }
    }

    private void queryTicket(){
        String start = mStart.getText().toString();
        String end = mEnd.getText().toString();
        String date = mSelectDate.getText().toString();
        boolean onlyGD = isOnlyGD.isChecked();

        UserInfo userInfo = new UserInfo();
        userInfo.setStart(start);
        userInfo.setEnd(end);
        userInfo.setDate(date);
        userInfo.setOnlyGD(onlyGD);
        userInfo.save();

        Intent intent = new Intent(this,TicketShowActivity.class);
        //Intent intent = new Intent(this,PushInfoActivity.class);
        intent.putExtra("userId",userInfo.getId());
        startActivity(intent);
    }
}
