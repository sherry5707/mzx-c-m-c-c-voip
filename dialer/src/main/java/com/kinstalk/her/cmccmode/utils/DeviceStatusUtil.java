package com.kinstalk.her.cmccmode.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.kinstalk.her.cmccmode.data.AppInfo;
import com.kinstalk.her.cmccmode.data.Constants;
import com.kinstalk.her.cmccmode.data.MyDBProvider;
import com.kinstalk.her.cmccmode.data.MyDBProviderHelper;

public class DeviceStatusUtil {
    private static final String TAG = "DeviceStatusUtil";
    private static final String SP_NAME = "device_info";
    private static final String SP_KEY_BIND = "key_bind_status";
    private static final String SP_KEY_ENABLED = "key_enabled_status";
    private static final String SP_KEY_APPKEY = "key_appkey";
    private static final String SP_KEY_APPKSECRET = "key_appsecret";
    private static final String SP_KEY_ALLPERMISSIONS = "key_all_permissions";
    private static final String SP_KEY_CALL_STATUS = "key_call_status";
    public static boolean HJGH_LOGIN_STATUS = false;

    /**
     * 设置设备与和家亲的绑定状态
     *
     * @param context
     * @param flag    0表示未绑定  1表示绑定
     */
    public static void setBindStatus(Context context, int flag) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME,Context.MODE_MULTI_PROCESS);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(SP_KEY_BIND,flag);
//        editor.commit();
        MyDBProviderHelper.setBindStatus(flag);
    }

    /**
     * 返回设备与和家亲的绑定状态
     *
     * @param context
     * @return
     */
    public static int getBindStatus(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME,Context.MODE_MULTI_PROCESS);
//        int status = sharedPreferences.getInt(SP_KEY_BIND,0);
////        int status = Settings.System.getInt(context.getContentResolver(), "cmccvoip.bind.status", 0);
//        Log.i(TAG, "getBindStatus: " + status);
//        return status;
        return MyDBProviderHelper.getBindStatus();
    }

    /**
     * 设置设备的启用状态
     *
     * @param context
     * @param flag    0表示未启用  1表示启用
     */
    public static void setEnabledStatus(Context context, int flag) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME,Context.MODE_MULTI_PROCESS);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(SP_KEY_ENABLED,flag);
//        editor.commit();
//        Settings.System.putInt(context.getContentResolver(), "cmccvoip.enabled.status", flag);
        MyDBProviderHelper.setEnableStatus(flag);
    }

    /**
     * 返回设备的启用状态
     *
     * @param context
     * @return
     */
    public static int getEnabledStatus(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME,Context.MODE_MULTI_PROCESS);
//        int status = sharedPreferences.getInt(SP_KEY_ENABLED,0);
////        int status = Settings.System.getInt(context.getContentResolver(), "cmccvoip.enabled.status", 0);
//        Log.i(TAG, "getBindStatus: " + status);
//        return status;
        return MyDBProviderHelper.getEnableStatus();
    }

    /**
     * 设置和家固话
     *
     * @param flag    0表示未登录  1表示登录
     */
    public static void setHJGHLoginStatus(int flag) {
        MyDBProviderHelper.setHJGHLoginStatus(flag);
    }

    /**
     * 返回设备的启用状态
     *
     * @return
     */
    public static int getHJGHLoginStatus() {
        return MyDBProviderHelper.getHJGHLoginStatus();
    }

    public static void setKeyAndSecret(Context context, AppInfo appInfo) {
//        Log.i(TAG, "setKeyAndSecret: appInfo:" + appInfo);
//        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_MULTI_PROCESS);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(SP_KEY_APPKEY, appInfo.getAppKey());
//        editor.putString(SP_KEY_APPKSECRET, appInfo.getAppsecret());
//        editor.commit();
        MyDBProviderHelper.setAppKeySecret(appInfo);
    }

    public static AppInfo getKeyAndSecret(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_MULTI_PROCESS);
//        String secret = sharedPreferences.getString(SP_KEY_APPKSECRET, null);
//        String key = sharedPreferences.getString(SP_KEY_APPKEY, null);
//        AppInfo appInfo = new AppInfo(key, secret);
//        Log.i(TAG, "getKeyAndSecret: appInfo:" + appInfo);
//        return appInfo;
        return MyDBProviderHelper.getAppKeySecret();
    }

    /**
     * 判断是否获得了所有权限
     *
     * @param context
     * @param flag
     */
    public static void setPermissionStatus(Context context, boolean flag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SP_KEY_ALLPERMISSIONS, flag);
        editor.commit();
    }

    public static boolean isAllPermissionGet(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_MULTI_PROCESS);
        boolean flag = sharedPreferences.getBoolean(SP_KEY_ALLPERMISSIONS, false);
        return flag;
    }
}
