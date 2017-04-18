package com.hdu.even.grabticket.util;

import java.util.regex.Pattern;

/**
 * Created by Even on 2017/4/13.
 * 邮箱验证工具类
 */

public class EmailUtil {
    //正则表达式：验证邮箱
    public static final String REGEX_EMAIL =
            "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    public static boolean isValidEmail(String email){
        return Pattern.matches(REGEX_EMAIL,email);
    }
}
