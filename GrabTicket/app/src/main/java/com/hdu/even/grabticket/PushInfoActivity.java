package com.hdu.even.grabticket;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.hdu.even.grabticket.db.TicketInfo;
import com.hdu.even.grabticket.socket.ConnectServer;
import com.hdu.even.grabticket.socket.RequestMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Even on 2017/4/15.
 */

public class PushInfoActivity extends Activity {
    private ImageButton mToGitCode;
    private ArrayList<String> mTrainNos;//车次
    private String mStart;
    private String mEnd;
    private String mDate;
    private String mEmail;
    private ArrayList<String> mSeats;//座位类型

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_socket);
        mToGitCode = (ImageButton)findViewById(R.id.to_git_code);
        mToGitCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://github.com/Evenss/SmallTrick/tree/master/GrabTicket");
                intent.setData(content_url);
                startActivity(intent);
            }
        });
        TicketInfo ticketInfo = getIntent().getParcelableExtra("ticketInfo");
        mTrainNos = ticketInfo.trainNos;
        mDate = ticketInfo.date;
        mStart = ticketInfo.start;
        mEnd = ticketInfo.end;
        mEmail = ticketInfo.email;
        mSeats = ticketInfo.seats;
        new SocketTask().execute();
    }
        class SocketTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params) {
            try {
                Map<String, String> ticketMap = new HashMap<>();
                ticketMap.put("date",mDate);
                ticketMap.put("start",mStart);
                ticketMap.put("end",mEnd);
                ticketMap.put("email",mEmail);
                RequestMessage requestMessage = new RequestMessage();
                requestMessage.setCode(1);
                requestMessage.setData(ticketMap);
                requestMessage.setTrainNos(mTrainNos);
                requestMessage.setSeats(mSeats);
                new ConnectServer("123.56.27.89",8888).sendInfo(requestMessage);
                //new ConnectServer("192.168.56.1",8888).sendInfo(requestMessage);
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
