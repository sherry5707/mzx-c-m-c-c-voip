package com.kinstalk.her.cmccmode.utils;

import android.util.Log;

import java.util.HashMap;

/**
 * 播报来电时小微会把电话号码识别为数字，这个是用来将电话号码转为中文的
 */
public class NumberToChineseUtil {
    private static final String TAG = "NumberToChineseUtil";
    private static HashMap<String,String> numberMaps = new HashMap<>();
    static {
        numberMaps.put("1","幺");
        numberMaps.put("2","二");
        numberMaps.put("3","三");
        numberMaps.put("4","四");
        numberMaps.put("5","五");
        numberMaps.put("6","六");
        numberMaps.put("7","七");
        numberMaps.put("8","八");
        numberMaps.put("9","九");
        numberMaps.put("0","零");

    }

    public static String getChinese(String number){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<number.length();i++){
            String subStr = number.substring(i,i+1);
            sb.append(numberMaps.get(subStr));
        }
        Log.i(TAG, "getChinese: number:"+number+",to Chinese:"+sb.toString());
        return sb.toString();
    }

    private static HashMap<String,String> pinyinMaps = new HashMap<>();
    static {
        pinyinMaps.put("1","一");
        pinyinMaps.put("2","二");
        pinyinMaps.put("3","三");
        pinyinMaps.put("4","四");
        pinyinMaps.put("5","五");
        pinyinMaps.put("6","六");
        pinyinMaps.put("7","七");
        pinyinMaps.put("8","八");
        pinyinMaps.put("9","九");
        pinyinMaps.put("0","零");

    }

    public static String getChinesePinYin(String number){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<number.length();i++){
            String subStr = number.substring(i,i+1);
            sb.append(pinyinMaps.get(subStr));
        }
        Log.i(TAG, "getChinese: number:"+number+",to Chinese:"+sb.toString());
        return sb.toString();
    }
}
