package com.kinstalk.her.cmccmode.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.kinstalk.her.openqsdk.manager.OpenQManager;

public class AWSPush {
    private static final String TAG = "AWSPush";
    private static OpenQManager mOpenQManager;

    public static void bindService(final Context context) {
        // Get OpenQManager
//        Log.i(TAG, "Get OpenQManager!");
//        VoipThreadManager.getInstance().start(new Runnable() {
//            @Override
//            public void run() {
//                mOpenQManager = OpenQManager.getInstance(context);
//
//                Log.e(TAG, "Call OpenQManager to subscribe push!");
//                if (mOpenQManager != null) {
//                    //mManager.subscribe(branchs, new MessageCallback());
////                String topic = SystemProperties.get("persist.qlove.sn", "");
//                    String topic = "cmcc";
//                    Log.e(TAG, "mOpenQManager is not null");
//                    if (!TextUtils.equals(topic, "")) {
//                        mOpenQManager.subscribePush(topic, true);
//                        mOpenQManager.subscribePush(topic, false);
//                    } else {
//                        Log.i(TAG, "Failed to get SN!");
//                    }
//                    Log.i(TAG, "Subscribe branch " + topic);
//                }
//            }
//        });
    }

}
