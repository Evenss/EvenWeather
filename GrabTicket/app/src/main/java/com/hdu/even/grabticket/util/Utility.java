package com.hdu.even.grabticket.util;

import com.google.gson.Gson;
import com.hdu.even.grabticket.gson.TicketInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Even on 2017/4/10.
 */

public class Utility {

    public static TicketInfo handleTicketInfoResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            String ticketInfo = jsonObject.getJSONObject("data").toString();
            return new Gson().fromJson(ticketInfo,TicketInfo.class);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }
}
