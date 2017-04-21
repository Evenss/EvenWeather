package com.hdu.even.grabticket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hdu.even.grabticket.db.TicketInfo;
import com.hdu.even.grabticket.db.UserInfo;
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
    private RecyclerView mRecyclerView;
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    private void initView(){
        mShowDate = (TextView)findViewById(R.id.show_date);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
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
                final com.hdu.even.grabticket.gson.TicketInfo ticketInfo = Utility.handleTicketInfoResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(ticketInfo != null){
                            adapter = new TicketAdapter(ticketInfo.ticketLists,NO_SELECT_STATE);
                            mRecyclerView.setAdapter(adapter);
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
                    for(int i=0;i<typesSubmit.size();i++){
                        if(typesSubmit.get(i).equals(types.get(which))){
                            typesSubmit.remove(i);
                        }
                    }
                }
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
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
        builder.setPositiveButton("好的，我会耐心等待", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                socketConnection();
            }
        });
        builder.show();
    }
    //传数据到PushInfoActivity
    private void socketConnection(){
        ArrayList<String> trainNos = new ArrayList<>();
        for(TicketList ticketInfo:listSubmit){
            trainNos.add(ticketInfo.trainNo);
        }
        TicketInfo ticketInfo = new TicketInfo();
        ticketInfo.trainNos = trainNos;
        ticketInfo.date = mUserInfo.getDate();
        ticketInfo.start = mUserInfo.getStart();
        ticketInfo.end = mUserInfo.getEnd();
        ticketInfo.email = mUserInfo.getEmail();
        ticketInfo.seats = typesSubmit;
        Intent intent = new Intent(this,PushInfoActivity.class);
        intent.putExtra("ticketInfo", ticketInfo);
        startActivity(intent);
        finish();
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

    class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.MyViewHolder>{
        private List<TicketList> list;
        private HashMap<Integer, Integer> isCheckBoxVisible = new HashMap<>();// 用来记录是否显示checkBox
        private HashMap<Integer, Boolean> isChecked = new HashMap<>();// 用来记录是否被选中

        public TicketAdapter(List<TicketList> list, int position) {
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
        //创建新View，被LayoutManager所调用
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_ticket,parent,false);
            final MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }


        //将数据与界面进行绑定的操作
        @Override
        public void onBindViewHolder(final MyViewHolder holder,int position) {
            setValue(holder,position);
            // 根据position设置CheckBox是否可见，是否选中
            holder.selected.setChecked(isChecked.get(position));
            int flag = View.INVISIBLE;
            if(View.VISIBLE == isCheckBoxVisible.get(position)){
                flag = View.VISIBLE;
            }else if(View.INVISIBLE == isCheckBoxVisible.get(position)){
                flag = View.INVISIBLE;
            }
            holder.selected.setVisibility(flag);

        }
        //获取数据的数量
        @Override
        public int getItemCount() {
            return list.size();
        }

        private void setValue(final MyViewHolder viewHolder, int position){
            viewHolder.start.setText(list.get(position).dptStationName);
            viewHolder.startDate.setText(list.get(position).dptTime);
            viewHolder.end.setText(list.get(position).arrStationName);
            viewHolder.endDate.setText(list.get(position).arrTime);
            viewHolder.trainNum.setText(list.get(position).trainNo);
            viewHolder.interval.setText(list.get(position).extraTicketInfo.interval);
            viewHolder.seatType1.setText("");//强制刷新剩余票数
            viewHolder.seatType2.setText("");
            viewHolder.seatType3.setText("");
            viewHolder.seatType4.setText("");
            if (isMultiSelect && isChecked.get(position).equals(true)) {
                viewHolder.selected.setChecked(true);
            }else{
                viewHolder.selected.setChecked(false);
            }
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
                viewHolder.price.setText("￥"+list.get(position).seats.二等座.price);
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
                viewHolder.price.setText("￥"+list.get(position).seats.硬座.price);
                if((count = list.get(position).seats.硬座.count)==0){
                    viewHolder.seatType3.setText("硬座:" + count + "(抢)");
                    viewHolder.seatType3.setTextColor(getColor(R.color.OrangeRed));
                }else if (count > 0){
                    viewHolder.seatType3.setText("硬座:" + count);
                    viewHolder.seatType3.setTextColor(getColor(R.color.darkGray));
                }
            }
        }

        //自定义的ViewHolder，持有每个Item的的所有界面元素
        class MyViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener,View.OnLongClickListener{
            private TextView start;
            private TextView startDate;
            private TextView end;
            private TextView endDate;
            private TextView trainNum;
            private TextView interval;
            private TextView price;
            private CheckBox selected;

            private TextView seatType1;
            private TextView seatType2;
            private TextView seatType3;
            private TextView seatType4;

            public MyViewHolder(final View view) {
                super(view);
                initView(view);
                view.setOnClickListener(this);
                // ListView每一个Item的长按事件
                view.setOnLongClickListener(this);
            }
            /*
             * 在ListView中点击每一项的处理
             * 如果CheckBox未选中，则点击后选中CheckBox，并将数据添加到list_delete中
             * 如果CheckBox选中，则点击后取消选中CheckBox，并将数据从list_delete中移除
             */
            @Override
            public void onClick(View view) {
                // 处于多选模式
                if (isMultiSelect) {
                    if (selected.isChecked()) {
                        selected.setChecked(false);
                        isChecked.put(getLayoutPosition(),false);
                        listSubmit.remove(list.get(getLayoutPosition()));
                    } else {
                        selected.setChecked(true);
                        isChecked.put(getLayoutPosition(),true);
                        listSubmit.add(list.get(getLayoutPosition()));
                    }
                }else {
                    Snackbar.make(view,"长按选择多个",Snackbar.LENGTH_SHORT).show();
                }
            }
            //长按事件
            @Override
            public boolean onLongClick(View view) {
                int position = getLayoutPosition();
                if(isMultiSelect == true){//选中状态下长按，取消
                    isMultiSelect = false;
                    listSubmit.clear();
                    adapter = new TicketAdapter(list, NO_SELECT_STATE);
                    mRecyclerView.setAdapter(adapter);
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
                adapter = new TicketAdapter(list, position);
                mRecyclerView.setAdapter(adapter);
                return true;
            }

            public void initView(View convertView){
                start = (TextView) convertView.findViewById(R.id.start);
                startDate = (TextView) convertView.findViewById(R.id.start_date);
                end = (TextView) convertView.findViewById(R.id.end);
                endDate = (TextView) convertView.findViewById(R.id.end_date);
                trainNum = (TextView) convertView.findViewById(R.id.train_num);
                interval = (TextView) convertView.findViewById(R.id.interval);
                price = (TextView) convertView.findViewById(R.id.price);
                selected = (CheckBox) convertView.findViewById(R.id.selected);

                seatType1 = (TextView) convertView.findViewById(R.id.seat_type1);
                seatType2 = (TextView) convertView.findViewById(R.id.seat_type2);
                seatType3 = (TextView) convertView.findViewById(R.id.seat_type3);
                seatType4 = (TextView) convertView.findViewById(R.id.seat_type4);
            }
        }
    }
}