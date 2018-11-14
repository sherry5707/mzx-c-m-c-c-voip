package com.kinstalk.her.cmccmode.data;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.kinstalk.her.cmccmode.CmccService;
import com.kinstalk.her.cmccmode.utils.DeviceStatusUtil;
import com.kinstalk.her.dialer.R;
import com.kinstalk.her.openqsdk.receiver.OpenQPushBaseReceiver;

public class OpenQPushMessageService extends IntentService {

    private static final String TAG = "OpenQPushMessageReceiver";

    String PUSH_ACTION = "com.kinstalk.her.openq.push_message";
    String STATUS_ACTION = "com.kinstalk.her.openq.online_status";

    String TOPIC_EXTRA = "topic";
    String MESSAGE_EXTRA = "message";
    String STATUS_EXTRA = "status";

    private static final String AWS_TYPE_BIND = "bind";
    private static final String AWS_TYPE_UNBIND = "unbind";
    private static final String AWS_TYPE_ENABLE = "enable";
    private static final String AWS_TYPE_DISABLE = "disable";
    private static final String AWS_TYPE_CONTACT = "contact";
    private static final String AWS_TYPE_SECRET = "secret";

    public static final String ACTION_BIND_STATUS = "action_bind_status";
    public static final String ACTION_ENABLED_STATUS = "aciton_enabled_status";
    public static final String ACTION_GET_KS = "action_get_keyAndSecret";

    public static final String EXTRA_BIND_STATUS = "extra_bind_status";
    public static final String EXTRA_ENABLED_STATUS = "extra_enabled_status";
    public static final String EXTRA_KEY = "extra_key";
    public static final String EXTRA_SECRET = "extra_secret";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */
    public OpenQPushMessageService() {
        super("OpenQPushMessageService");
    }

    // 收到应用内消息后回调此接口。
    @SuppressLint("LongLogTag")
    public void onTextMessage(Context context, String topic, String message) {
        Log.e(TAG, "MessageReceiver::OnTextMessage : message is " + message);
        JSONObject rspJson = JSONObject.parseObject(message);
        AWSResponse responseObj = JSONObject.parseObject(rspJson.toJSONString(), AWSResponse.class);
        Log.e(TAG, "onTextMessage: responseObj:" + responseObj.toString());
        parseContent(context, responseObj);
    }

    @SuppressLint("LongLogTag")
    private void parseContent(final Context context, AWSResponse response) {
        if (AWS_TYPE_BIND.equals(response.getType())) {
            if (DeviceStatusUtil.getBindStatus(context) == 1) {
                //如果已经绑定，就不需要再操作，防止重启后aws推送上一次的消息
                return;
            }
            DeviceStatusUtil.setBindStatus(context, 1);
            sendBindStatusReceiver(context, 1);
            //留到登录成功后set
//            DeviceStatusUtil.setEnabledStatus(context, 1);
            sendEnabledStatusReceiver(context, 1);

            //判断是否有sharedpreference里是否有appkey,appsecret，没有的话需要从服务器获取
            AppInfo appInfo = DeviceStatusUtil.getKeyAndSecret(context);
            if (TextUtils.isEmpty(appInfo.getAppsecret())
                    || TextUtils.isEmpty(appInfo.getAppKey())) {
                Api.fetchKeySecret(new Api.KeySecretCallBack() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void getKeySecret(AppInfo appInfo) {
                        Log.e(TAG, "parseContent,getKeySecret from server success");
                        DeviceStatusUtil.setKeyAndSecret(context, appInfo);
                        Intent intent = new Intent(ACTION_GET_KS);
                        intent.putExtra(EXTRA_KEY, appInfo.getAppKey());
                        intent.putExtra(EXTRA_SECRET, appInfo.getAppsecret());
                        context.sendBroadcast(intent);

                        CmccService.initVoipAndFetchContacts2();
//                        DialerApplication.initVoip(appInfo.getAppKey(), appInfo.getAppsecret());
                    }
                });
            } else {
                Log.e(TAG, "parseContent,AWS_TYPE_BIND,so we init voip");
                CmccService.initVoipAndFetchContacts2();
//                DialerApplication.initVoip(DeviceStatusUtil.getAppKey(context), DeviceStatusUtil.getAppSecret(context));
            }
        } else if (AWS_TYPE_UNBIND.equals(response.getType())) {
            if (DeviceStatusUtil.getBindStatus(context) == 0) {
                //如果已经绑定，就不需要再操作，防止重启后aws推送上一次的消息
                return;
            }
            DeviceStatusUtil.setBindStatus(context, 0);
            sendBindStatusReceiver(context, 0);
            DeviceStatusUtil.setEnabledStatus(context, 0);
            sendEnabledStatusReceiver(context, 0);

            //登录信息，注册监听全部move掉
            CmccService.logOut();
//            DialerApplication.logoutHJGH();
        } else if (AWS_TYPE_CONTACT.equals(response.getType())) {
//            MyDBProviderHelper.clearContactsDB();
            //fetch里面会clear
            CmccService.startFetchContacts();
        } else if (AWS_TYPE_DISABLE.equals(response.getType())) {
            if (DeviceStatusUtil.getEnabledStatus(context) == 0) {
                //如果已经禁用，就不需要再操作，防止重启后aws推送上一次的消息
                return;
            }
            CmccService.logOut();
            DeviceStatusUtil.setEnabledStatus(context, 0);
            sendEnabledStatusReceiver(context, 0);
            CmccService.reportTTS(context.getString(R.string.voip_diabled));
        } else if (AWS_TYPE_ENABLE.equals(response.getType())) {
            Log.i(TAG, "parseContent: enabled status:"+DeviceStatusUtil.getEnabledStatus(context));
            if (DeviceStatusUtil.getEnabledStatus(context) == 1 || DeviceStatusUtil.getHJGHLoginStatus() == 1) {
                //如果已经启用，就不需要再操作，防止重启后aws推送上一次的消息
                return;
            }
            CmccService.initVoipAndFetchContacts2();
            DeviceStatusUtil.setEnabledStatus(context, 1);
            sendEnabledStatusReceiver(context, 1);
            CmccService.reportTTS(context.getString(R.string.voip_enabled));
        } else if (AWS_TYPE_SECRET.equals(response.getType())) {
            AppInfo appInfo = JSONObject.parseObject(response.getData(), AppInfo.class);
            Log.i(TAG, "parseContent: appInfo:" + appInfo);
            DeviceStatusUtil.setKeyAndSecret(context, appInfo);
            Intent intent = new Intent(ACTION_GET_KS);
            intent.putExtra(EXTRA_KEY, appInfo.getAppKey());
            intent.putExtra(EXTRA_SECRET, appInfo.getAppsecret());
            context.sendBroadcast(intent);
        }
    }

    private void sendBindStatusReceiver(Context context, int status) {
        Intent intent = new Intent(ACTION_BIND_STATUS);
        intent.putExtra(EXTRA_BIND_STATUS, status);
        context.sendBroadcast(intent);
    }

    private void sendEnabledStatusReceiver(Context context, int status) {
        Intent intent = new Intent(ACTION_ENABLED_STATUS);
        intent.putExtra(EXTRA_ENABLED_STATUS, status);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String topic = intent.getStringExtra(TOPIC_EXTRA);
            String message = intent.getStringExtra(MESSAGE_EXTRA);
            onTextMessage(getApplicationContext(), topic, message);
        }

    }
}
