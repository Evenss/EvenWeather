package com.hdu.even.grabticket.socket;

import com.alibaba.fastjson.JSON;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by Even on 2017/4/17.
 */

public class MySendInfo {
    private int ip;
    private String host;
    private final static String END = "#end#";//用来结束发送的字符串
    public MySendInfo(String host,int ip){
        this.ip = ip;
        this.host = host;
    }
    public void sendInfo(RequestMessage requestMessage){
        try{
            final Socket socket = new Socket(host,ip);
            String json = JSON.toJSONString(requestMessage);

            OutputStream out = socket.getOutputStream();
            PrintStream printStream = new PrintStream(out);
            printStream.println(json);
            printStream.println(END);
            printStream.flush();

            socket.shutdownOutput();
            out.close();
            printStream.close();
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
