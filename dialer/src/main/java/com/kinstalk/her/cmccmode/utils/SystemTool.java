/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.cmccmode.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.kinstalk.her.dialer.DialerApplication;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by knight.xu on 2018/4/6.
 */

public final class SystemTool {
    private static final String TAG = "SystemTool";
    private static String sWifiMacAddress = "";
    public static String sn;

    /**
     * 获得mac
     *
     * @return
     */
    public static String getLocalMacAddress() {

        if (!TextUtils.isEmpty(sWifiMacAddress)) {
            return sWifiMacAddress;
        }

        String Mac = null;
        try {
            String path = "sys/class/net/wlan0/address";
            if ((new File(path)).exists()) {
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer = new byte[8192];
                int byteCount = fis.read(buffer);
                if (byteCount > 0) {
                    Mac = new String(buffer, 0, byteCount, "utf-8");
                }
                fis.close();
            }

            if (Mac == null || Mac.length() == 0) {
                path = "sys/class/net/eth0/address";
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer_name = new byte[8192];
                int byteCount_name = fis.read(buffer_name);
                if (byteCount_name > 0) {
                    Mac = new String(buffer_name, 0, byteCount_name, "utf-8");
                }
                fis.close();
            }

            if (!TextUtils.isEmpty(Mac)) {
                Mac = Mac.substring(0, Mac.length() - 1);
            }
        } catch (Exception io) {
        }

        if (TextUtils.isEmpty(Mac)) {
            WifiManager wifiManager = (WifiManager) DialerApplication.getContext()
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getMacAddress() != null) {
                Mac = wifiInfo.getMacAddress();
            }
        }

        Log.d(TAG, "wifi Mac = " + Mac);
        sWifiMacAddress = Mac;

        return TextUtils.isEmpty(Mac) ? "" : Mac;
    }


    public static String getMacForSn() {
        String serialNum = getQloveSN();
        boolean isSnGot = false;
        if (!TextUtils.isEmpty(serialNum)) {
            Log.i(TAG, "getSn: serialNum = " + serialNum);
            if (serialNum.length() == 18) {
                sn = serialNum.substring(2, 18);
                isSnGot = true;
                Log.i(TAG, "getSn: sn = " + sn);
            } else {
                sn = "1234567890";
                Log.e(TAG, "getSn: wrong serial number");
            }
        } else {
            Log.e(TAG, "getSn: empty serial number ");
            String macAddr = SystemTool.getLocalMacAddress();
            if (!TextUtils.isEmpty(macAddr)) {
                Log.i(TAG, "getSn: mac = " + macAddr);
                if (macAddr.length() == 17) {
                    sn = macAddr.substring(0, 2) + macAddr.substring(3, 17);
                    isSnGot = true;
                    Log.i(TAG, "getSn: mac sn = " + sn);
                } else {
                    sn = "1234567890";
                    Log.e(TAG, "getSn: wrong mac Addr");
                }
            } else {
                sn = "1234567890";
                Log.e(TAG, "getSn: macAddr is empty");
            }
        }
        return sn;
    }

    private static String getQloveSN() {
        String qlovesn = SystemPropertiesProxy.getString(DialerApplication.getContext(),
                "ro.serialno");
        String qloveSn = TextUtils.isEmpty(qlovesn) ? "" : qlovesn;

        return qloveSn;
    }
}