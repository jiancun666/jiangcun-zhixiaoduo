package com.semple.zhixiaoduo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author feilong
 * @date 2025年04月27日 9:22
 * @description
 */
public class DevicesUtils {
    public static boolean isMobileDevice(String userAgent) {
        if (userAgent == null) {
            return false;
        }
        String regex = "android|iphone|ipad|ipod|blackberry|iemobile|opera mini|mobile|windows phone|phone|webos|kindle|tablet";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(userAgent.toLowerCase());
        return matcher.find();
    }
}
