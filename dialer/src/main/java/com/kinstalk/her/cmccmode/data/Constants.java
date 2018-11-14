package com.kinstalk.her.cmccmode.data;

import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.kinstalk.her.cmccmode.utils.SystemTool;
import com.kinstalk.her.cmccmode.utils.VoipLog;
import com.kinstalk.her.dialer.DialerApplication;

import java.io.File;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Constants {
    private static final String TAG = "Constants";
    public static final String APP_KEY = "4xg249z7mugo7y75";
    public static final String APP_SECRET = "zl3s529djihd9now";
    private static String LOG_PATH;//= "/data/data/com.kinstalk.her.cmccvoip/log/";
    private static final String TEST_DEVICE_ID = "innertest-jykj";
    public static final int TYPE_CALLIN = 0;
    public static final int TYPE_CALLOUT = 1;

    public static String enCiper(String appSecret) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] raw = appSecret.getBytes("utf-8");
        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        String sn = SystemTool.getMacForSn();
        if (sn.length() == 16) {
            sn = "83" + sn;
        }
        Log.i(TAG, "enCiper: sn:" + sn);
        byte[] original = cipher.doFinal(getDeviceId().getBytes("utf-8"));
        return Base64.encodeToString(original, Base64.NO_WRAP);
    }

    public static String getDeviceId() {
        String deviceId = Settings.System.getString(DialerApplication.getContext()
                .getContentResolver(), "andlink_deviceId");
        VoipLog.e(TAG, "deviceId:" + deviceId);
        return deviceId;
    }


    /**
     * 根据当前的存储位置得到日志的绝对存储路径
     *
     * @return
     */
    public static String getLogPath() {
        LOG_PATH = DialerApplication.getContext().getCacheDir().getAbsolutePath() + File.separator + "log";
        Log.i(TAG, "getLogPath: path:"+LOG_PATH);
        return LOG_PATH;
    }
}
