package com.hdu.even.grabticket.util;

/**
 * Created by Even on 2017/4/13.
 * 邮箱验证工具类
 */

public class EmailUtil {
    public static boolean isValidEmail(String email){
        if(email.contains("@")){
            return true;
        }else{
            return false;
        }
    }
}
