package com.kinstalk.her.cmccmode.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kinstalk.her.cmccmode.CmccService;
import com.kinstalk.her.cmccmode.data.MyDBProviderHelper;
import com.kinstalk.her.cmccmode.data.VoipThreadManager;
import com.kinstalk.her.incallui.InCallActivity;
import com.mobile.voip.sdk.api.CMImsManager;
import com.mobile.voip.sdk.callback.VoIPDialCallBack;

import java.util.logging.Handler;

import static com.kinstalk.her.cmccmode.data.Constants.TYPE_CALLOUT;

public class CmccCallMode {
    private static final String TAG = "CmccCallMode";

    /**
     * 通过通讯录id，号码拨打电话
     *
     * @param contactId
     * @param phoneNum
     * @param callBack
     */
    public static void callOut(String contactId, String phoneNum, VoIPDialCallBack callBack) {
        CMImsManager.getInstance().callOutId(contactId, phoneNum, callBack);
    }

    /**
     * 在登录成功后有电话呼⼊的情况下，⽤户点击接听按键调用该接口
     */
    public static void pickUpCall() {
        Log.i(TAG, "pickUpCall: ");
        int result = CMImsManager.getInstance().pickUpCall(CmccPublicMode.getSession());
    }

    /**
     * 挂断电话
     *
     * @return
     */
    public static void hangUPCall() {
        Log.i(TAG, "hangUPCall: ");
        MyDBProviderHelper.setInCallStatus(0);
        CMImsManager.getInstance().hangUpCall(CmccPublicMode.getSession());
    }
}
