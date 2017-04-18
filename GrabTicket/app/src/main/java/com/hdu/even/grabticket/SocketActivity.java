package com.hdu.even.grabticket;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.hdu.even.grabticket.db.SocketInfo;
import com.hdu.even.grabticket.socket.MySendInfo;
import com.hdu.even.grabticket.socket.RequestMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Even on 2017/4/15.
 */

public class SocketActivity extends Activity {
    private ImageButton mGotoGit;
    private ArrayList<String> trainNos;//车次
    private String start;
    private String end;
    private String date;
    private String email;
    private ArrayList<String> seats;//座位类型

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_socket);
        mGotoGit = (ImageButton)findViewById(R.id.goto_git);
        mGotoGit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://github.com/Evenss/SmallTrick/tree/master/GrabTicket");
                intent.setData(content_url);
                startActivity(intent);
            }
        });
        SocketInfo socketInfo = getIntent().getParcelableExtra("socketInfo");
        trainNos = socketInfo.trainNos;
        date = socketInfo.date;
        start = socketInfo.start;
        end = socketInfo.end;
        email = socketInfo.email;
        seats = socketInfo.seats;
        new TestSocketTask().execute();
    }
        class TestSocketTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params) {
            try {
                Map<String, String> map = new HashMap<>();
                map.put("date",date);
                map.put("start",start);
                map.put("end",end);
                map.put("email",email);
                RequestMessage requestMessage = new RequestMessage();
                requestMessage.setCode(1);
                requestMessage.setData(map);
                requestMessage.setTrainNos(trainNos);
                requestMessage.setSeats(seats);
                new MySendInfo("123.56.27.89",8888).sendInfo(requestMessage);
                //new MySendInfo("192.168.56.1",8888).sendInfo(requestMessage);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
