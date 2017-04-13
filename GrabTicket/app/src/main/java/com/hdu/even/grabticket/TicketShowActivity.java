package com.hdu.even.grabticket;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hdu.even.grabticket.db.UserInfo;
import com.hdu.even.grabticket.gson.TicketInfo;
import com.hdu.even.grabticket.gson.TicketList;
import com.hdu.even.grabticket.util.HttpUtil;
import com.hdu.even.grabticket.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.hdu.even.grabticket.R.id.selected;
import static com.hdu.even.grabticket.util.EmailUtil.isValidEmail;

/**
 * Created by Even on 2017/4/10.
 */

public class TicketShowActivity extends AppCompatActivity {
    private TextView mShowDate;
    private UserInfo mUserInfo;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton mSubmit;
    //URL
    private static final String URL_START = "http://train.qunar.com/dict/open/s2s.do?dptStation=";
    private static final String URL_END ="&arrStation=";
    private static final String URL_DATE ="&date=";
    private static final String URL_TYPE ="&type=normal&user=neibu&source=site&start=1&num=500&sort=3";

    private static final int NO_SELECT_STATE = -1;
    private ListView mListView;
    private List<TicketList> listSubmit = new ArrayList<>();// 需要提交的数据
    private ArrayList<String> typesSubmit = new ArrayList<>();//需要提交的座位类型
    private boolean isMultiSelect = false;// 是否处于多选状态
    private TicketAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_ticket);
        initView();
        int userId = getIntent().getIntExtra("userId",-1);
        if(userId != -1){
            mUserInfo = DataSupport.find(UserInfo.class,userId);
        }else{
            Toast.makeText(this,"传入数据有误",Toast.LENGTH_SHORT).show();
        }
        mShowDate.setText(mUserInfo.getDate());
        requestTicket();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestTicket();
            }
        });
    }

    private void initView(){
        mShowDate = (TextView)findViewById(R.id.show_date);
        mListView = (ListView)findViewById(R.id.list_view);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        mSubmit = (FloatingActionButton)findViewById(R.id.submit);
    }

    //通过URL请求数据
    private void requestTicket(){
        String ticketUrl = URL_START + mUserInfo.getStart() +
                URL_END + mUserInfo.getEnd() +
                URL_DATE + mUserInfo.getDate() + URL_TYPE;
        HttpUtil.sendOkHttpRequest(ticketUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Toast.makeText(TicketShowActivity.this,"获取车票信息失败",Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final TicketInfo ticketInfo = Utility.handleTicketInfoResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(ticketInfo != null){
                            adapter = new TicketAdapter(TicketShowActivity.this,
                                    ticketInfo.ticketLists,NO_SELECT_STATE);
                            mListView.setAdapter(adapter);
                        }else{
                            Toast.makeText(TicketShowActivity.this,
                                    "获取车票信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }
    //提交信息到服务器，展示各种对话框
    public void Submit(View v){
        if(listSubmit.size() == 0){
            Snackbar.make(v,"至少选择一辆车次",Snackbar.LENGTH_SHORT).show();
        }else{
            createTypeDialog();
        }
    }

    //创建车型选择Dialog
    private void createTypeDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择要提醒的座位类型");
        builder.setCancelable(false);
        final ArrayList<String> types = new ArrayList<>();
        if(checkGDType()){
            types.add("商务座");
            types.add("一等座");
            types.add("二等座");
        }
        if(checkNormalType()){
            types.add("软卧");
            types.add("硬卧");
            types.add("硬座");
            types.add("无座");
        }
        builder.setMultiChoiceItems(types.toArray(new String[0]), null,
                new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked){
                    typesSubmit.add(types.get(which));
                }else{
                    typesSubmit.remove(which);
                }
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //typesSubmit.toString();
                createEmailDialog(builder);
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    //填写邮箱Dialog
    private void createEmailDialog(final AlertDialog.Builder builderType){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("填写联系邮箱");
        builder.setCancelable(false);
        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_email,null);
        builder.setView(view);

        final EditText email = (EditText)view.findViewById(R.id.email);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String emailStr = email.getText().toString().trim();
                if(isValidEmail(emailStr)){
                    mUserInfo.setEmail(emailStr);
                    createSuccessDialog();//发送信息到服务器！！！！！！！！！
                }else {
                    ViewGroup parent = (ViewGroup)view.getParent();
                    if(parent != null){//这里要移除父视图
                        parent.removeAllViews();
                    }
                    createErrorDialog(builder);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builderType.show();
            }
        });
        builder.show();
    }
    //错误提示Dialog
    private void createErrorDialog(final AlertDialog.Builder builderEmail){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("错误提示");
        builder.setCancelable(false);
        builder.setMessage("输入邮箱不合法，请重新输入！");
        builder.setPositiveButton("重新输入", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               builderEmail.show();
            }
        });
        builder.show();
    }
    //成功提交Dialog
    private void createSuccessDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("我们会将信息第一时间发送到您的邮箱，感谢支持！");
        builder.setPositiveButton("好的，我会耐心等待", null);
        builder.show();
    }
    //动态展示车型
    private boolean checkGDType(){
        for(int i = 0;i < listSubmit.size();i++){
            String str = listSubmit.get(i).trainNo.substring(0,1);
            if(str.equals("G") || str.equals("D")){
                return true;
            }
        }
        return false;
    }
    private boolean checkNormalType(){
        for(int i = 0;i < listSubmit.size();i++){
            String str = listSubmit.get(i).trainNo.substring(0,1);
            if(str.equals("Z") || str.equals("T") || str.equals("K")){
                return true;
            }
        }
        return false;
    }

    private class TicketAdapter extends BaseAdapter {
        private List<TicketList> list;
        private LayoutInflater inflater;

        private HashMap<Integer, Integer> isCheckBoxVisible = new HashMap<>();// 用来记录是否显示checkBox
        private HashMap<Integer, Boolean> isChecked = new HashMap<>();// 用来记录是否被选中

        public TicketAdapter(Context context, List<TicketList> list,int position) {
            inflater = LayoutInflater.from(context);
            this.list = list;
            // 如果处于多选状态，则显示CheckBox，否则不显示
            if (isMultiSelect) {
                for (int i = 0; i < list.size(); i++) {
                    isCheckBoxVisible.put(i, CheckBox.VISIBLE);
                    isChecked.put(i, false);
                }
                mSubmit.setVisibility(View.VISIBLE);
            } else {
                for (int i = 0; i < list.size(); i++) {
                    isCheckBoxVisible.put(i, CheckBox.INVISIBLE);
                    isChecked.put(i, false);
                }
                mSubmit.setVisibility(View.INVISIBLE);
            }

            // 如果长按Item，则设置长按的Item中的CheckBox为选中状态
            if (isMultiSelect && position >= 0) {
                isChecked.put(position, true);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public Object getItem(int position) {
            return list.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.listview_ticket, null);
                initView(viewHolder,convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            setValue(viewHolder,position);
            // 根据position设置CheckBox是否可见，是否选中
            viewHolder.selected.setChecked(isChecked.get(position));

            int flag = View.INVISIBLE;
            if(View.VISIBLE == isCheckBoxVisible.get(position)){
                flag = View.VISIBLE;
            }else if(View.INVISIBLE == isCheckBoxVisible.get(position)){
                flag = View.INVISIBLE;
            }
            viewHolder.selected.setVisibility(flag);
            // ListView每一个Item的长按事件
            convertView.setOnLongClickListener(new onMyLongClick(position, list));
            /*
             * 在ListView中点击每一项的处理
             * 如果CheckBox未选中，则点击后选中CheckBox，并将数据添加到list_delete中
             * 如果CheckBox选中，则点击后取消选中CheckBox，并将数据从list_delete中移除
             */
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 处于多选模式
                    if (isMultiSelect) {
                        if (viewHolder.selected.isChecked()) {
                            viewHolder.selected.setChecked(false);
                            listSubmit.remove(list.get(position));
                        } else {
                            viewHolder.selected.setChecked(true);
                            listSubmit.add(list.get(position));
                        }
                    }else {
                        Snackbar.make(v,"长按选择多个",Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            return convertView;
        }
        private void initView(ViewHolder viewHolder,View convertView){
            viewHolder.start = (TextView) convertView.findViewById(R.id.start);
            viewHolder.startDate = (TextView) convertView.findViewById(R.id.start_date);
            viewHolder.end = (TextView) convertView.findViewById(R.id.end);
            viewHolder.endDate = (TextView) convertView.findViewById(R.id.end_date);
            viewHolder.trainNum = (TextView) convertView.findViewById(R.id.train_num);
            viewHolder.interval = (TextView) convertView.findViewById(R.id.interval);
            viewHolder.price = (TextView) convertView.findViewById(R.id.price);
            viewHolder.selected = (CheckBox) convertView.findViewById(selected);

            viewHolder.seatType1 = (TextView) convertView.findViewById(R.id.seat_type1);
            viewHolder.seatType2 = (TextView) convertView.findViewById(R.id.seat_type2);
            viewHolder.seatType3 = (TextView) convertView.findViewById(R.id.seat_type3);
            viewHolder.seatType4 = (TextView) convertView.findViewById(R.id.seat_type4);
        }

        private void setValue(ViewHolder viewHolder,int position){
            viewHolder.start.setText(list.get(position).dptStationName);
            viewHolder.startDate.setText(list.get(position).dptTime);
            viewHolder.end.setText(list.get(position).arrStationName);
            viewHolder.endDate.setText(list.get(position).arrTime);
            viewHolder.trainNum.setText(list.get(position).trainNo);
            viewHolder.interval.setText(list.get(position).extraTicketInfo.interval);
            int count;
            if(list.get(position).seats.商务座 != null){
                if((count = list.get(position).seats.商务座.count)==0){
                    viewHolder.seatType1.setText("商务座:" + count + "(抢)");
                    viewHolder.seatType1.setTextColor(getColor(R.color.OrangeRed));
                }else if (count > 0){
                    viewHolder.seatType1.setText("商务座:" + count);
                    viewHolder.seatType1.setTextColor(getColor(R.color.darkGray));
                }
            }
            if(list.get(position).seats.一等座 != null){
                if((count = list.get(position).seats.一等座.count)==0){
                    viewHolder.seatType2.setText("一等座:" + count + "(抢)");
                    viewHolder.seatType2.setTextColor(getColor(R.color.OrangeRed));
                }else if (count > 0){
                    viewHolder.seatType2.setText("一等座:" + count);
                    viewHolder.seatType2.setTextColor(getColor(R.color.darkGray));
                }
            }
            if(list.get(position).seats.二等座 != null){
                if((count = list.get(position).seats.二等座.count)==0){
                    viewHolder.seatType3.setText("二等座:" + count + "(抢)");
                    viewHolder.seatType3.setTextColor(getColor(R.color.OrangeRed));
                }else if (count > 0){
                    viewHolder.seatType3.setText("二等座:" + count);
                    viewHolder.seatType3.setTextColor(getColor(R.color.darkGray));
                }
            }
            if(list.get(position).seats.无座 != null){
                if((count = list.get(position).seats.无座.count)==0){
                    viewHolder.seatType4.setText("无座:" + count + "(抢)");
                    viewHolder.seatType4.setTextColor(getColor(R.color.OrangeRed));
                }else if (count > 0){
                    viewHolder.seatType4.setText("无座:" + count);
                    viewHolder.seatType4.setTextColor(getColor(R.color.darkGray));
                }
            }
            if(list.get(position).seats.软卧 != null){
                if((count = list.get(position).seats.软卧.count)==0){
                    viewHolder.seatType1.setText("软卧:" + count + "(抢)");
                    viewHolder.seatType1.setTextColor(getColor(R.color.OrangeRed));
                }else if (count > 0){
                    viewHolder.seatType1.setText("软卧:" + count);
                    viewHolder.seatType1.setTextColor(getColor(R.color.darkGray));
                }
            }
            if(list.get(position).seats.硬卧 != null){
                if((count = list.get(position).seats.硬卧.count)==0){
                    viewHolder.seatType2.setText("硬卧:" + count + "(抢)");
                    viewHolder.seatType2.setTextColor(getColor(R.color.OrangeRed));
                }else if (count > 0){
                    viewHolder.seatType2.setText("硬卧:" + count);
                    viewHolder.seatType2.setTextColor(getColor(R.color.darkGray));
                }
            }
            if(list.get(position).seats.硬座 != null){
                if((count = list.get(position).seats.硬座.count)==0){
                    viewHolder.seatType3.setText("硬座:" + count + "(抢)");
                    viewHolder.seatType3.setTextColor(getColor(R.color.OrangeRed));
                }else if (count > 0){
                    viewHolder.seatType3.setText("硬座:" + count);
                    viewHolder.seatType3.setTextColor(getColor(R.color.darkGray));
                }
            }
        }

        class ViewHolder {
            public TextView start;
            public TextView startDate;
            public TextView end;
            public TextView endDate;
            public TextView trainNum;
            public TextView interval;
            public TextView price;
            public CheckBox selected;

            public TextView seatType1;
            public TextView seatType2;
            public TextView seatType3;
            public TextView seatType4;
        }

        // 自定义长按事件
        class onMyLongClick implements View.OnLongClickListener {

            private int position;
            private List<TicketList> list;

            // 获取数据，与长按Item的position
            public onMyLongClick(int position, List<TicketList> list) {
                this.position = position;
                this.list = list;
            }

            // 在长按监听时候，切记将监听事件返回true
            @Override
            public boolean onLongClick(View v) {
                if(isMultiSelect == true){//选中状态下长按，取消
                    isMultiSelect = false;
                    listSubmit.clear();
                    adapter = new TicketAdapter(TicketShowActivity.this,list, NO_SELECT_STATE);
                    mListView.setAdapter(adapter);
                    return true;
                }
                isMultiSelect = true;
                listSubmit.clear();
                // 添加长按Item到删除数据list中
                listSubmit.add(list.get(position));
                for (int i = 0; i < list.size(); i++) {
                    adapter.isCheckBoxVisible.put(i, CheckBox.VISIBLE);
                }
                // 根据position，设置ListView中对应的CheckBox为选中状态
                adapter = new TicketAdapter(TicketShowActivity.this,list, position);
                mListView.setAdapter(adapter);
                return true;
            }
        }
    }
}