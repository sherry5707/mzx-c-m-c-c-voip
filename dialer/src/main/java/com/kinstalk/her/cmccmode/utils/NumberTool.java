package com.kinstalk.her.cmccmode.utils;

import android.util.Log;

public class NumberTool {
    private static final String TAG = "NumberTool";
    //移除手机号+86
    public static String phoneNumberFilter(String number) {
        if (number.contains("+")) {
            number = number.substring(3, number.length());
        }
        Log.i(TAG, "after phoneNumberFilter: number:" + number);
        return number;
    }

    private static String[] testNumber = new String[]{
            "130733179394",
            "0531581091624",
            "1008611",
            "1001011",
            "17512536267",
            "13073317939",
            "1751253626744",
            "053158109162"
    };

    public static void testValidNumber(){
        for(int i=0;i<testNumber.length;i++){
            cutValidNumber(testNumber[i]);
        }
    }

    /**
     * 如果是1开头的电话号码，必须小于11位
     * @param number
     * @return
     */
    public static String cutValidNumber(String number){
        if(number.charAt(0)=='1'){
            Log.i(TAG, "cutValidNumber: is start with 1");
            if(number.length()>11){
                number = number.substring(0,11);
            }
        }else if(number.charAt(0)=='0'){
            Log.i(TAG, "cutValidNumber: is start with 0");
            if(number.length()>12){
                number = number.substring(0,12);
            }
        }
        Log.i(TAG, "after cutValidNumber,number is:"+number);
        return number;
    }
}
