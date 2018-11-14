package com.kinstalk.her.cmccmode;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.kinstalk.her.cmccmode.data.Api;
import com.kinstalk.her.cmccmode.data.AppInfo;
import com.kinstalk.her.cmccmode.data.CallRecord;
import com.kinstalk.her.cmccmode.data.Constants;
import com.kinstalk.her.cmccmode.data.ContactInfo;
import com.kinstalk.her.cmccmode.data.MyDBProviderHelper;
import com.kinstalk.her.cmccmode.data.VoipThreadManager;
import com.kinstalk.her.cmccmode.utils.CmccCallMode;
import com.kinstalk.her.cmccmode.utils.CmccPublicMode;
import com.kinstalk.her.cmccmode.utils.DeviceStatusUtil;
import com.kinstalk.her.cmccmode.utils.NumberToChineseUtil;
import com.kinstalk.her.cmccmode.utils.RingUtils;
import com.kinstalk.her.cmccmode.utils.VoipLog;
import com.kinstalk.her.dialer.DialerApplication;
import com.kinstalk.her.dialer.R;
import com.kinstalk.her.incallui.InCallActivity;
import com.kinstalk.qloveaicore.AIManager;
import com.kinstalk.qloveaicore.TTSListener;
import com.mobile.voip.sdk.api.CMImsManager;
import com.mobile.voip.sdk.api.utils.MyLogger;
import com.mobile.voip.sdk.api.utils.VoIPServerConnectListener;
import com.mobile.voip.sdk.callback.QueryContactCallBack;
import com.mobile.voip.sdk.callback.VoIP;
import com.mobile.voip.sdk.callback.VoIPDialCallBack;

import java.util.HashMap;

import static com.kinstalk.her.cmccmode.data.Constants.TYPE_CALLIN;
import static com.kinstalk.her.cmccmode.data.Constants.TYPE_CALLOUT;
import static com.kinstalk.her.cmccmode.data.OpenQPushMessageService.ACTION_BIND_STATUS;
import static com.kinstalk.her.cmccmode.data.OpenQPushMessageService.ACTION_ENABLED_STATUS;
import static com.kinstalk.her.cmccmode.data.OpenQPushMessageService.EXTRA_BIND_STATUS;
import static com.kinstalk.her.cmccmode.data.OpenQPushMessageService.EXTRA_ENABLED_STATUS;
import static com.kinstalk.her.cmccmode.utils.NumberTool.phoneNumberFilter;
import static com.kinstalk.her.dialer.ForceRequestPermissionsActivity.ACTION_FINISH;
import static com.kinstalk.her.incallui.GlowPadAnswerFragment.ACTION_VOICE_HANGUP;
import static com.kinstalk.her.incallui.GlowPadAnswerFragment.ACTION_VOICE_PICKUP;

public class CmccService extends Service {
    private static final String TAG = "CmccService";
    public static final String CMD = "service_cmd";
    public static final int INIT_VOIP_AND_FETCH_CONTACTS = 1;
    private static final int CALL_IN = 2;
    private static final int INIT_VOIP = 3;
    public static final int SIMPLE_INIT_VOIP = 4;
    public static final int LOGOUT_HJGH = 5;
    private static final int CALL_OUT = 6;
    private static final int QUERY_NICKNAME = 7;    //外拨电话之前需要先查找nickName
    public static final int CALL_OUT_BY_NAME = 8;
    public static final int CALL_OUT_BY_NUMBER = 9;
    public static final int CALL_OUT_BY_NAME_AND_CONTACTID = 10;
    public static final int CALL_STATE_LISTENER_REGISTER = 11;
    public static final int INCALL_HANGUP = 12;
    public static final int INCALL_PICKUP = 13;
    public static final int FETCH_CONTACTS = 14;
    public static final int SEND_DTMF = 15;
    public static final int VOICE_RECEIVE_CALL = 16;
    public static final int VOICE_REJECT_CALL = 17;
    public static final int VOICE_UPDATE_LOG = 18;
    public static final int TTS_REPORT = 19;
    public static final int TTS_INTERREPT = 20;
    public static final int UPDATE_LOG = 21;
    private static final int TOAST_SHOW = 22;
    public static final int AUTO_INIT = 23;
    private static MyHandler myHandler;
    /**
     * Hash Map to map a view id to a character
     */
    private static final HashMap<Integer, Integer> mErrorCodeMap =
            new HashMap<Integer, Integer>();

    /** Set up the static maps*/
    static {
        mErrorCodeMap.put(100001, R.string.invalid_app_key);
        mErrorCodeMap.put(100002, R.string.invalid_device_info);
        mErrorCodeMap.put(100003, R.string.device_unbind);
        mErrorCodeMap.put(100004, R.string.device_disnabled);
        mErrorCodeMap.put(100005, R.string.have_no_hjgh_function);
        mErrorCodeMap.put(100006, R.string.invalid_number);
        mErrorCodeMap.put(110000, R.string.hjgh_not_init);
        mErrorCodeMap.put(110001, R.string.param_null);
        mErrorCodeMap.put(110002, R.string.net_reques_error);
        mErrorCodeMap.put(110003, R.string.file_exception);
        mErrorCodeMap.put(110004, R.string.hjgh_account_data_error);
        mErrorCodeMap.put(110005, R.string.hjgh_not_login);
        mErrorCodeMap.put(110006, R.string.phone_number_error);
        mErrorCodeMap.put(110007, R.string.call_out_error_native_not_login);
        mErrorCodeMap.put(110008, R.string.call_out_error_can_not_find_session);
        mErrorCodeMap.put(110009, R.string.call_out_error_native_audio_init_error);
        mErrorCodeMap.put(110010, R.string.file_not_exit_or_exception);
        mErrorCodeMap.put(900003, R.string.device_id_error);
    }

    public static class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CALL_IN: {
                    Bundle bundle = msg.getData();
                    final String noFilterNumber = bundle.getString("phone_number");
                    final String phoneNumber = phoneNumberFilter(noFilterNumber);
                    final int session = bundle.getInt("session");
                    CmccPublicMode.getNickNameByNumber(phoneNumber, new QueryContactCallBack() {
                        @SuppressLint("StringFormatInvalid")
                        @Override
                        public void onQueryFailed(int code) {
                            Log.e(TAG, DialerApplication.getContext().getString(R.string.coming_call) + ",phone:" + phoneNumber +
                                    ",onQueryFailed: code:" + code);
                            CmccPublicMode.setSession(session);
                            //语音播报
                            String ttsWithName = String.format(DialerApplication.getContext().getString(R.string.call_in_with_number_tts), phoneNumber);
                            AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(ttsWithName, null);
                            //插入接入通话记录
                            CallRecord callRecord = new CallRecord();
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                callRecord.setmNumber(phoneNumber);
                                callRecord.setmCallLogType(CallLog.Calls.INCOMING_TYPE);
                                callRecord.setmCallLogDate(System.currentTimeMillis());
                                MyDBProviderHelper.insertCallRecord(DialerApplication.getContext(), callRecord);
                            }

                            Intent intent = new Intent(DialerApplication.getContext(), InCallActivity.class);
                            intent.putExtra("phone_number", phoneNumber);
                            intent.putExtra("type", TYPE_CALLIN);
                            DialerApplication.getContext().startActivity(intent);
                        }

                        @SuppressLint("StringFormatInvalid")
                        @Override
                        public void onQuerySuccess(String nickName) {
                            Log.i(TAG, DialerApplication.getContext().getString(R.string.coming_call) + ",phone:" + phoneNumber + ",name:" + nickName);
                            CmccPublicMode.setSession(session);
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                ContactInfo contact = MyDBProviderHelper.getContactByNickNamFromSelfDB(nickName);
                                Log.i(TAG, "onQuerySuccess: contact:" + contact);
                                if (contact != null) {
                                    //语音播报name
                                    String ttsWithName = String.format(DialerApplication.getContext().getString(R.string.call_in_with_name_tts), nickName);
                                    AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(ttsWithName, new TTSListener() {
                                        @Override
                                        public void onTTSPlayBegin(String s) {

                                        }

                                        @Override
                                        public void onTTSPlayEnd(String s) {
                                            //小微开始拾音（判断是否用户会说接听，挂断）
                                            //因为响铃的状态sdk里面不会调用到alerting，所以没法判断是否在响铃状态。只能在未接的时候去拾音
                                            if (MyDBProviderHelper.getInCalleStatus() == 0) {
                                                Log.e(TAG, "name,onTTSPlayEnd: begin recognize:");
                                                AIManager.getInstance(DialerApplication.getContext()).beginRecognize(null);
                                            }

                                        }

                                        @Override
                                        public void onTTSPlayProgress(String s, int i) {

                                        }

                                        @Override
                                        public void onTTSPlayError(String s, int i, String s1) {

                                        }
                                    });
                                } else {
                                    //语音播报number
                                    String ttsWithNumber = String.format(DialerApplication.getContext().getString(R.string.call_in_with_number_tts),
                                            NumberToChineseUtil.getChinese(nickName));
                                    AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(ttsWithNumber, new TTSListener() {
                                        @Override
                                        public void onTTSPlayBegin(String s) {

                                        }

                                        @Override
                                        public void onTTSPlayEnd(String s) {
                                            //小微开始拾音（判断是否用户会说接听，挂断）
                                            if (MyDBProviderHelper.getInCalleStatus() == 0) {
                                                Log.e(TAG, "number,onTTSPlayEnd: begin recognize:");
                                                AIManager.getInstance(DialerApplication.getContext()).beginRecognize(null);
                                            }
                                        }

                                        @Override
                                        public void onTTSPlayProgress(String s, int i) {

                                        }

                                        @Override
                                        public void onTTSPlayError(String s, int i, String s1) {

                                        }
                                    });
                                }
                                //插入接入通话记录
                                CallRecord callRecord = new CallRecord();
                                if (contact != null) {
                                    callRecord.setmName(nickName);
                                    callRecord.setmNumber(contact.getContactId());
                                } else {
                                    callRecord.setmNumber(phoneNumber);
                                }
                                callRecord.setmCallLogType(CallLog.Calls.INCOMING_TYPE);
                                callRecord.setmCallLogDate(System.currentTimeMillis());
                                MyDBProviderHelper.insertCallRecord(DialerApplication.getContext(), callRecord);
                            }

                            Intent intent = new Intent(DialerApplication.getContext(), InCallActivity.class);
                            intent.putExtra("phone_number", phoneNumber);
                            intent.putExtra("nick_name", (!TextUtils.isEmpty(phoneNumber) && phoneNumber.equals(nickName)) ? "" : nickName);
                            intent.putExtra("type", TYPE_CALLIN);
                            DialerApplication.getContext().startActivity(intent);
                        }
                    });
                    break;
                }
                case CALL_OUT: {
                    Log.i(TAG, "handleMessage: call out");
                    if (DeviceStatusUtil.getBindStatus(DialerApplication.getContext()) == 0) {
                        AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                                DialerApplication.getContext().getString(R.string.tts_device_unbind), null);
                    } else {
                        if (DeviceStatusUtil.getHJGHLoginStatus() == 0) {
                            autoFetchKSandContactsFromServer(DialerApplication.getContext());
                        }
                        if (MyDBProviderHelper.getInCalleStatus() == 1) {
                            Log.i(TAG, "handleMessage: callout but already has a call,so return");
                            return;
                        }
                        MyDBProviderHelper.setInCallStatus(1);
                        Bundle bundle = msg.getData();
                        final String contactId = bundle.getString("contact_id");
                        final String nickName = bundle.getString("nick_name");
                        final String number = bundle.getString("number");
                        CmccCallMode.callOut(TextUtils.isEmpty(nickName) ? null : contactId,
                                TextUtils.isEmpty(nickName) ? number : null, new VoIPDialCallBack() {
                                    @Override
                                    public void onHandleDialSuccess(int callSession) {
                                        CmccPublicMode.setSession(callSession);
                                        //插入外拨通话记录
                                        CallRecord callRecord = new CallRecord();
                                        if (!TextUtils.isEmpty(nickName)) {
                                            callRecord.setmName(nickName);
                                            callRecord.setmNumber(contactId);
                                        } else if (!TextUtils.isEmpty(number)) {
                                            callRecord.setmNumber(number);
                                        }
                                        callRecord.setmCallLogType(CallLog.Calls.OUTGOING_TYPE);
                                        callRecord.setmCallLogDate(System.currentTimeMillis());
                                        MyDBProviderHelper.insertCallRecord(DialerApplication.getContext(), callRecord);

                                        VoipLog.e(TAG, "onHandleDialSuccess,callSession:" + callSession);
                                        Intent intent = new Intent(DialerApplication.getContext(), InCallActivity.class);
                                        if (!TextUtils.isEmpty(nickName)) {
                                            intent.putExtra("nick_name", nickName);
                                        } else {
                                            intent.putExtra("phone_number", number);
                                        }
                                        intent.putExtra("type", TYPE_CALLOUT);
                                        DialerApplication.getContext().startActivity(intent);
                                    }

                                    @SuppressLint("StringFormatInvalid")
                                    @Override
                                    public void onHandleDialError(int errorCode) {
                                        MyDBProviderHelper.setInCallStatus(0);
                                        VoipLog.e(TAG, "onHandleDialError: errorCode:" + errorCode);
                                        //先判断是否绑定再判断是否启用
                                        if (DeviceStatusUtil.getBindStatus(DialerApplication.getContext()) == 0) {
                                            AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                                                    DialerApplication.getContext().getString(R.string.tts_device_unbind), null);
                                            showToast(DialerApplication.getContext().getString(R.string.device_unbind));
//                                        Toast.makeText(DialerApplication.getContext(),
//                                                DialerApplication.getContext().getString(R.string.device_unbind), Toast.LENGTH_SHORT).show();
                                        } else if (DeviceStatusUtil.getEnabledStatus(DialerApplication.getContext()) == 0) {
                                            AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                                                    DialerApplication.getContext().getString(R.string.tts_device_disabled), null);
                                            showToast(DialerApplication.getContext().getString(R.string.voip_diabled));
//                                        Toast.makeText(DialerApplication.getContext(),
//                                                DialerApplication.getContext().getString(R.string.voip_diabled), Toast.LENGTH_SHORT).show();
                                        } else if (mErrorCodeMap.containsKey(errorCode)) {
                                            Log.e(TAG, "mErrorCodeMap is not null");
                                            int errorStringRes = mErrorCodeMap.get(errorCode);
                                            AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                                                    String.format(DialerApplication.getContext().getString(R.string.tts_hjgh_error_reason),
                                                            NumberToChineseUtil.getChinese(String.valueOf(errorCode)),
                                                            DialerApplication.getContext().getString(errorStringRes)),
                                                    null);
                                            showToast(String.format(DialerApplication.getContext().getString(R.string.tts_hjgh_error_reason),
                                                    String.valueOf(errorCode),
                                                    DialerApplication.getContext().getString(errorStringRes)));
//                                        Toast.makeText(DialerApplication.getContext(),
//                                                String.format(DialerApplication.getContext().getString(R.string.tts_hjgh_error_reason),
//                                                        String.valueOf(errorCode), DialerApplication.getContext().getString(errorStringRes)),
//                                                Toast.LENGTH_SHORT).show();
                                        } else {
                                            AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                                                    String.format(DialerApplication.getContext().getString(R.string.tts_hjgh_error_code),
                                                            NumberToChineseUtil.getChinese(String.valueOf(errorCode))),
                                                    null);
                                            showToast(String.format(DialerApplication.getContext().getString(R.string.tts_hjgh_error_code),
                                                    String.valueOf(errorCode)));
//                                        Toast.makeText(DialerApplication.getContext(),
//                                                String.format(DialerApplication.getContext().getString(R.string.tts_hjgh_error_code),
//                                                        String.valueOf(errorCode)),
//                                                Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                    break;
                }
                case QUERY_NICKNAME: {
                    Bundle bundle = msg.getData();
                    final String number = bundle.getString("number");
                    final Bundle newBudle = new Bundle();
                    Log.i(TAG, "handleMessage: query_nickName,number:" + number);
                    CmccPublicMode.getNickNameByNumber(number, new QueryContactCallBack() {
                        @Override
                        public void onQueryFailed(int code) {
                            //该号码不是联系人，直接按号码拨出
                            Log.i(TAG, "onQueryFailed: code:" + code);
                            Message message = Message.obtain();
                            message.what = CALL_OUT;
                            newBudle.putString("number", number);
                            message.setData(newBudle);
                            myHandler.sendMessage(message);
                        }

                        @Override
                        public void onQuerySuccess(String imsNick) {
                            //从sdk中查到了该number对应的name，再从数据库中查找该name的contactId
                            Message message = Message.obtain();
                            message.what = CALL_OUT;
                            ContactInfo contact = MyDBProviderHelper.getContactByNickNamFromSelfDB(imsNick);
                            Log.i(TAG, "onQuerySuccess: contact:" + contact);
                            if (contact != null) {
                                newBudle.putString("contact_id", contact.getContactId());
                                newBudle.putString("nick_name", contact.getNickname());
                            }
                            newBudle.putString("number", number);
                            message.setData(newBudle);
                            myHandler.sendMessage(message);
                        }
                    });
                    break;
                }
                case INIT_VOIP: {
                    Log.i(TAG, "handleMessage: init voip");
                    MyLogger.initLogger(true, Constants.getLogPath());
                    loginHJGH();
                    registerCallbackAndFetchContacts();
                    break;
                }
                case TTS_INTERREPT:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(" ", null);
                        }
                    }, 600);
                    break;
                case UPDATE_LOG:
                    VoipThreadManager.getInstance().start(new Runnable() {
                        @Override
                        public void run() {
                            CmccPublicMode.updateLog();
                        }
                    });
                    break;
                case TOAST_SHOW:
                    Bundle bundle = msg.getData();
                    String text = bundle.getString("toast_text");
                    Log.e(TAG, "handleMessage: TOAST_SHOW,text:" + text);
                    Toast.makeText(DialerApplication.getContext(), text, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public static void registerCallbackAndFetchContacts() {
        //监听连接状态
        CmccPublicMode.registerConnectionStateListenerAndFetchContact(DialerApplication.getContext());
        //监听来电
        CmccPublicMode.registerComingCallListener(DialerApplication.getContext(), new VoIP.OnInComingCallListener() {
            @Override
            public void onInComingCall(String s) {
                VoipLog.e(TAG, DialerApplication.getContext().getString(R.string.coming_call) + ",phone:" + s);
            }

            @Override
            public void onInComingCall(final String phoneNumber, final int callType, final int session) {
                Message message = Message.obtain();
                message.what = CALL_IN;
                Bundle bundle = new Bundle();
                bundle.putString("phone_number", phoneNumber);
                bundle.putInt("session", session);
                message.setData(bundle);
                myHandler.sendMessage(message);
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //接听挂断/拒绝广播
    public static final String ACTION_INCALL_HANG_UP = "incall_activity_hang_up";
    public static final String ACTION_INCALL_REFUSE = "incall_activity_refuse";
    public static final String ACTION_BIND_FROM_ANDLINK = "com.kinstalk.qloveandlinkapp.ANDLINKDONE";
    public static final String ACTION_UNBIND_FROM_ANDLINK = "com.kinstalk.qloveandlinkapp.UNBOUND";
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "action:" + intent.getAction());
            if (ACTION_INCALL_HANG_UP.equals(intent.getAction())
                    || ACTION_INCALL_REFUSE.equals(intent.getAction())) {
                Log.e(TAG, "ACTION_INCALL_HANG_UP or REFUSE");
                hangUpCall();
            } else if (ACTION_BIND_FROM_ANDLINK.equals(intent.getAction())) {
                if (DeviceStatusUtil.getBindStatus(context) == 0 || DeviceStatusUtil.getHJGHLoginStatus() == 0) {
                    Log.i(TAG, "onReceive: get action from andlink and start init");
                    autoInit();
                }
                DeviceStatusUtil.setBindStatus(context, 1);
            } else if (ACTION_UNBIND_FROM_ANDLINK.equals(intent.getAction())) {
                DeviceStatusUtil.setBindStatus(context, 0);
                logOut();
            }
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INCALL_HANG_UP);
        filter.addAction(ACTION_INCALL_REFUSE);
        filter.addAction(ACTION_BIND_FROM_ANDLINK);
        filter.addAction(ACTION_UNBIND_FROM_ANDLINK);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initHandler();
        int bindStatus = DeviceStatusUtil.getBindStatus(DialerApplication.getContext());
        int enableStatus = DeviceStatusUtil.getEnabledStatus(DialerApplication.getContext());
        String deviceId = Constants.getDeviceId();
        Log.i(TAG, "onCreate: bindStatus:" + bindStatus + ",enabledStatus:" + enableStatus + ",deviceId:" + deviceId);
        DeviceStatusUtil.setHJGHLoginStatus(0);
        registerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand,bind_status:" + DeviceStatusUtil.getBindStatus(DialerApplication.getContext()));
        if (intent != null) {
            int what = intent.getIntExtra(CMD, -1);
            Log.i(TAG, "onStartCommand: what:" + what);
            switch (what) {
                case INIT_VOIP_AND_FETCH_CONTACTS: {
                    Log.i(TAG, "onStartCommand: INIT_VOIP_AND_FETCH_CONTACTS");
                    initVoipAndFetchContacts();
                    break;
                }
                case SIMPLE_INIT_VOIP: {
                    Log.i(TAG, "onStartCommand: SIMPLE_INIT_VOIP");
                    initVoip();
                    break;
                }
                case LOGOUT_HJGH: {
                    Log.i(TAG, "onStartCommand: LOGOUT_HJGH");
                    logoutHJGH();
                    break;
                }
                case CALL_OUT_BY_NAME: {
                    Log.i(TAG, "onStartCommand: CALL_OUT_BY_NAME");
                    String name = intent.getStringExtra("name");
                    Log.i(TAG, "onStartCommand: name:" + name);
                    if (!TextUtils.isEmpty(name)) {
                        callOut(name);
                    } else {
                        Log.e(TAG, "CALL_OUT_BY_NAME,name is null!!!");
                    }
                    break;
                }
                case CALL_OUT_BY_NUMBER: {
                    Log.i(TAG, "onStartCommand: CALL_OUT_BY_NUMBER");
                    String number = intent.getStringExtra("number");
                    Log.i(TAG, "onStartCommand: name:" + number);
                    if (!TextUtils.isEmpty(number)) {
                        callOut(DialerApplication.getContext(), number);
                    } else {
                        Log.e(TAG, "CALL_OUT_BY_NUMBER,number is null!!!");
                    }
                    break;
                }
                case CALL_OUT_BY_NAME_AND_CONTACTID: {
                    Log.i(TAG, "onStartCommand: CALL_OUT_BY_NAME_AND_CONTACTID");
                    String contactId = intent.getStringExtra("number");
                    String nickName = intent.getStringExtra("name");
                    if (!TextUtils.isEmpty(contactId) && !TextUtils.isEmpty(nickName)) {
                        callOut(nickName, contactId);
                    } else {
                        Log.e(TAG, "CALL_OUT_BY_NAME_AND_CONTACTID,number and contactId is null!!!");
                    }
                    break;
                }
                case CALL_STATE_LISTENER_REGISTER: {
                    Log.i(TAG, "onStartCommand: CALL_STATE_LISTENER_REGISTER");
                    registerCallingStateCallback();
                    break;
                }
                case INCALL_HANGUP: {
                    Log.i(TAG, "onStartCommand: INCALL_HANGUP");
                    CmccCallMode.hangUPCall();
                    break;
                }
                case INCALL_PICKUP: {
                    Log.i(TAG, "onStartCommand: INCALL_PICKUP");
                    CmccCallMode.pickUpCall();
                    break;
                }
                case FETCH_CONTACTS: {
                    Log.i(TAG, "onStartCommand: FETCH_CONTACTS");
                    CmccPublicMode.startFetchContacts();
                    break;
                }
                case SEND_DTMF: {
                    Log.i(TAG, "onStartCommand: SEND_DTMF");
                    int keycode = intent.getIntExtra("keycode", -1);
                    CmccPublicMode.sendDTMF(keycode);
                    break;
                }
                case VOICE_RECEIVE_CALL: {
                    Log.i(TAG, "onStartCommand: VOICE_RECEIVE_CALL");
                    CmccCallMode.pickUpCall();
                    break;
                }
                case VOICE_REJECT_CALL: {
                    Log.i(TAG, "onStartCommand: VOICE_REJECT_CALL");
                    CmccCallMode.hangUPCall();
                    break;
                }
                case VOICE_UPDATE_LOG: {
                    Log.i(TAG, "onStartCommand: VOICE_UPDATE_LOG");
                    CmccPublicMode.updateLog();
                    break;
                }
                case TTS_REPORT: {
                    Log.i(TAG, "onStartCommand: TTS_REPORT");
                    String tts = intent.getStringExtra("tts");
                    Log.i(TAG, "onStartCommand: tts:" + tts);
                    if (!TextUtils.isEmpty(tts)) {
                        AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(tts, null);
                    }
                    break;
                }
                case AUTO_INIT: {
                    Log.i(TAG, "onStartCommand: AUTO_INIT");
                    autoFetchKSandContactsFromServer(DialerApplication.getContext());
                    break;
                }
                default:
                    Log.i(TAG, "onStartCommand: default");
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CMImsManager.getInstance().destroy();
        myHandler = null;
        unregisterReceiver(broadcastReceiver);
    }

    private void initHandler() {
        HandlerThread thread = new HandlerThread("Dialer");
        thread.start();
        Looper mHandlerLooper = thread.getLooper();
        myHandler = new MyHandler(mHandlerLooper);
    }


    public static void loginHJGH() {
        VoipLog.i(TAG, "loginHJGH");
        if (DeviceStatusUtil.getHJGHLoginStatus() == 0) {
            CmccPublicMode.loginHJGH();
        }
    }

    /**
     * voip first initVoip
     * 第一次绑定，需要fetch contacts
     */
    public static void initVoipAndFetchContacts() {
        if (DeviceStatusUtil.getHJGHLoginStatus() == 1) {
            Log.i(TAG, "initVoip: already login");
            return;
        }
        Log.i(TAG, "start initVoip cmcc public mode and fetch contacts");
        AppInfo appInfo = DeviceStatusUtil.getKeyAndSecret(DialerApplication.getContext());
        if (!TextUtils.isEmpty(appInfo.getAppKey())) {
            CMImsManager.getInstance().init(DialerApplication.getContext(), appInfo.getAppKey());
        } else {
            Log.e(TAG, "initVoip: appKey is null,can not initVoip");
            return;
        }
        if (!TextUtils.isEmpty(appInfo.getAppsecret())) {
            CmccPublicMode.setDeviceInfo(appInfo.getAppsecret());
        } else {
            Log.i(TAG, "initVoip: appSecret is null,can not setDeviceinfo");
            return;
        }
        myHandler.sendEmptyMessage(INIT_VOIP);
    }


    /**
     * 主要是为了重新安装的时候会自己初始化
     */
    public static void initVoip() {
        if (DeviceStatusUtil.getHJGHLoginStatus() == 1) {
            Log.i(TAG, "initVoip: already login");
            return;
        }
        AppInfo appInfo = DeviceStatusUtil.getKeyAndSecret(DialerApplication.getContext());
        if (!TextUtils.isEmpty(appInfo.getAppKey())) {
            CMImsManager.getInstance().init(DialerApplication.getContext(), appInfo.getAppKey());
        } else {
            Log.e(TAG, "initVoip: appKey is null,can not initVoip");
        }
        if (!TextUtils.isEmpty(appInfo.getAppsecret())) {
            CmccPublicMode.setDeviceInfo(appInfo.getAppsecret());
        } else {
            Log.i(TAG, "initVoip: appSecret is null,can not setDeviceinfo");
        }
        loginHJGH();
        registerCallback(DialerApplication.getContext());
    }

    public static void registerCallback(final Context context) {
        //监听连接状态
        CmccPublicMode.registerConnectionStateListener(new VoIPServerConnectListener() {
            @Override
            public void onLoginSucceed(int sipFlag) {
                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.login_success) + ",type:" + sipFlag);
                showToast(context.getString(R.string.login_success));

                DeviceStatusUtil.setHJGHLoginStatus(1);
                context.sendBroadcast(new Intent(ACTION_FINISH));
                //防止aws推送出现问题，就以login成功为判断。因为如果disable,login会失败
                DeviceStatusUtil.setEnabledStatus(context, 1);

            }

            @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
            @Override
            public void onLoginFailed(int failedReason, int sipFlag) {
                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.login_fail) +
                        ",type:" + sipFlag + ",errorcode:" + failedReason + ",reason:" +
                        mErrorCodeMap.get(failedReason));
                DeviceStatusUtil.setHJGHLoginStatus(0);
                if (DeviceStatusUtil.getBindStatus(context) == 1 && DeviceStatusUtil.getEnabledStatus(context) == 1) {
                    showToast(context.getString(R.string.login_fail) +
                            String.format(context.getString(R.string.errorcode), failedReason));
                    if (failedReason == 504) {
                        CmccService.reportTTS(context.getString(R.string.login_504_error));
                    }
                }
            }

            @Override
            public void onImsLogging(int result, int sipFlag) {
                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.logining) + ",type:" + sipFlag + ",result:" + result);
            }

            @Override
            public void onDisConnected(int reason) {
                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.disconect) + ",reson:" + reason);
            }

            @Override
            public void onConnectSucceed() {
                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.connect_success));
            }
        });
        //监听来电
        CmccPublicMode.registerComingCallListener(DialerApplication.getContext(), new VoIP.OnInComingCallListener() {
            @Override
            public void onInComingCall(String s) {
                VoipLog.e(TAG, DialerApplication.getContext().getString(R.string.coming_call) + ",phone:" + s);
            }

            @Override
            public void onInComingCall(final String phoneNumber, final int callType, final int session) {
                Message message = Message.obtain();
                message.what = CALL_IN;
                Bundle bundle = new Bundle();
                bundle.putString("phone_number", phoneNumber);
                bundle.putInt("session", session);
                message.setData(bundle);
                myHandler.sendMessage(message);
            }
        });
    }

    public static void sendBindStatusReceiver(Context context, int status) {
        Intent intent = new Intent(ACTION_BIND_STATUS);
        intent.putExtra(EXTRA_BIND_STATUS, status);
        context.sendBroadcast(intent);
    }

    public static void sendEnabledStatusReceiver(Context context, int status) {
        Intent intent = new Intent(ACTION_ENABLED_STATUS);
        intent.putExtra(EXTRA_ENABLED_STATUS, status);
        context.sendBroadcast(intent);
    }

    /**
     * （通话记录/拨号盘拨号）
     * 外部调用拨号的方法
     * number可能是contactId,或者是正常号码
     * 正常号码可能是联系人的，那么打电话的时候会需要显示姓名
     */
    public static void callOut(final Context context, final String number) {
        Log.i(TAG, "CmccVoipCallout: number:" + number);
        VoipThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                ContactInfo contact = MyDBProviderHelper.getContactByContactIdFromSystemDB(context, number);
                Log.i(TAG, "callOut: contact:" + contact);
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("number", number);
                if (contact != null) {
                    //从数据库中查到了该number是contactId
                    bundle.putString("nick_name", contact.getNickname());
                    bundle.putString("contact_id", contact.getContactId());
                    message.what = CALL_OUT;
                    message.setData(bundle);
                    myHandler.sendMessage(message);
                } else {
                    //从sdk查该number是否是联系人
                    message.what = QUERY_NICKNAME;
                    message.setData(bundle);
                    myHandler.sendMessage(message);
                }
            }
        });

    }

    /**
     * 根据contactId和name拨号（联系人拨号）
     *
     * @param name
     * @param contactId
     */
    public static void callOut(final String name, final String contactId) {
        Log.e(TAG, "callOut: name:" + name + ",contactId:" + contactId);
        VoipThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Message message = Message.obtain();
                message.what = CALL_OUT;
                Bundle bundle = new Bundle();
                if (!TextUtils.isEmpty(name)) {
                    bundle.putString("contact_id", contactId);
                    bundle.putString("nick_name", name);
                }
                message.setData(bundle);
                myHandler.sendMessage(message);
            }
        });
    }

    /**
     * 根据用户昵称拨号
     *
     * @param nickName
     */
    @SuppressLint("StringFormatInvalid")
    public static void callOut(final String nickName) {
        Log.e(TAG, "CmccVoipCallout: nickName:" + nickName);
        VoipThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                ContactInfo contact = MyDBProviderHelper.getContactByNickNamFromSelfDB(nickName);
                Log.i(TAG, "callOut: contact:" + contact);
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                if (contact != null) {
                    //从数据库中查到了该number是contactId
                    bundle.putString("nick_name", contact.getNickname());
                    bundle.putString("contact_id", contact.getContactId());
                    message.what = CALL_OUT;
                    message.setData(bundle);
                    myHandler.sendMessage(message);
                } else {
                    AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(String.format(
                            DialerApplication.getContext().getString(R.string.voice_callout_no_contacts), nickName
                    ), null);
                }
            }
        });
    }

    private void registerCallingStateCallback() {
        CmccPublicMode.registerCallStateListener(new VoIP.OnCallStateListener() {
            @Override
            public void onCallProceeding() {
                Log.e(TAG, "CallStateListener   onCallProceeding");
            }

            @Override
            public void onCallAlerting() {
                Log.e(TAG, "CallStateListener   onCallAlerting");
            }

            @Override
            public void onCallAnswered() {
                Log.e(TAG, "CallStateListener   onCallAnswered");
                myHandler.sendEmptyMessage(TTS_INTERREPT);
                MyDBProviderHelper.setInCallStatus(1);
                //通话开始
                sendBroadcast(new Intent("ACTION_ENTER_VOIP_CALL"));
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onMakeCallFailed(int i) {
                Log.e(TAG, "CallStateListener   onMakeCallFailed,fail status:" + i + ",string:" +
                        String.format(DialerApplication.getContext().getString(R.string.tts_hjgh_error_code),
                                String.valueOf(i)));
                RingUtils.stop();
                showToast(String.format(DialerApplication.getContext().getString(R.string.tts_hjgh_error_code),
                        String.valueOf(i)));
                MyDBProviderHelper.setInCallStatus(0);
            }

            @Override
            public void onCallReleased() {
                Log.e(TAG, "CallStateListener   onCallReleased");
                //为了在被叫挂断是退出aicore的唤醒界面
                AIManager.getInstance(DialerApplication.getContext()).getData("{\"cmd\":\"eggClick\"}", null);
                InCallActivity.actionFinish();
                RingUtils.stop();
                //通话结束
                sendBroadcast(new Intent("ACTION_QUIT_VOIP_CALL"));
                MyDBProviderHelper.setInCallStatus(0);
            }

            @Override
            public void onCallProceeding(int i) {
                Log.e(TAG, "CallStateListener   onCallProceeding i:" + i);
            }

            @Override
            public void onCallAlerting(int session) {
                //被叫如果有回应，就返回这个事件。当收到这个事件时，对⽅会响起振铃⾳，本地会响起回铃⾳
                Log.e(TAG, "CallStateListener   onCallAlerting,session:" + session);
                RingUtils.startRing(DialerApplication.getContext(), R.raw.ringback);
                CmccPublicMode.setSession(session);
                MyDBProviderHelper.setInCallStatus(1);
            }

            @Override
            public void onStopCallAlerting(int session) {
                // 接收服务器推送的铃声之前,要停⽌本地铃声和震动
                Log.e(TAG, "CallStateListener   onStopCallAlerting");
                RingUtils.stop();
                CmccPublicMode.setSession(session);
                MyDBProviderHelper.setInCallStatus(1);
            }

            @Override
            public void onCallAnswered(int session, int callType) {
                // 外呼时，被叫接听，就返回这个事件。接收到这个事件，表明对⽅已经应答，进⼊通话状态
                RingUtils.stop();
                Log.e(TAG, "CallStateListener   onCallAnswered,session:" + session);
                MyDBProviderHelper.setInCallStatus(1);
                myHandler.sendEmptyMessage(TTS_INTERREPT);
                //通话开始
                sendBroadcast(new Intent("ACTION_ENTER_VOIP_CALL"));
                CmccPublicMode.setSession(session);
            }

            @Override
            public void onCallForward(int session) {
                //OTT呼叫转IMS事件。收到转接后，app再调⽤呼叫IMS
                Log.e(TAG, "CallStateListener   onCallForward");
                CmccPublicMode.setSession(session);
            }

            @Override
            public void onLogout() {
                //登出事件
                Log.e(TAG, "CallStateListener   onLogout");
                MyDBProviderHelper.setInCallStatus(0);
                InCallActivity.actionFinish();
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onMakeCallFailed(int session, String failStatus) {
                // 如果呼叫失败，就返回这个事件。外呼失败的原因有很多，可以根据status来判断
                Log.e(TAG, "CallStateListener   onMakeCallFailed,failStatus:" + failStatus);
/*                if (isValueNumber(failStatus) && !"487".equals(failStatus)) {
                    showToast(DialerApplication.getContext().getString(R.string.make_call_fail) +
                            String.format(DialerApplication.getContext().getString(R.string.errorcode), failStatus));
                }*/
                RingUtils.stop();
                CmccPublicMode.setSession(session);
                MyDBProviderHelper.setInCallStatus(0);
                InCallActivity.actionFinish();
            }

            @Override
            public void onCallReleased(int session) {
                //呼叫释放回调
                Log.e(TAG, "CallStateListener   onCallReleased,seeion:" + session);
                MyDBProviderHelper.setInCallStatus(0);
                CmccPublicMode.setSession(session);
                RingUtils.stop();
                //为了在被叫挂断是退出aicore的唤醒界面
                AIManager.getInstance(DialerApplication.getContext()).getData("{\"cmd\":\"eggClick\"}", null);
                InCallActivity.actionFinish();
                //打断tts播报
                myHandler.sendEmptyMessage(TTS_INTERREPT);
                //通话结束
                sendBroadcast(new Intent("ACTION_QUIT_VOIP_CALL"));
            }

            @Override
            public void onReceiveCallSwitch(int i) {
                // 对⽅将⾳频切换成视频时，就返回这个事件，然后应⽤层同意或者拒绝。
                // 如果对⽅是将视频切换成⾳频，本⽅会直接主动切换成⾳频
                Log.e(TAG, "CallStateListener   onReceiveCallSwitch");
            }

            @Override
            public void onCallReBuildResult(int session, int callType) {
                // 同意或者拒绝切换后返回的事件，callType是返回的处理切换后的电话类型，
                // 0表示是语⾳通话(即语⾳切换⾄视频被拒绝了，或者视频切换成语⾳成功了)，
                // 1表示是视频通话（即语⾳切换成视频被同意了）或者⽹络中断，然后重新建⽴成功了
                // ，0表示是语⾳通话；1表示是视频通话
                Log.e(TAG, "CallStateListener   onCallReBuildResult");
                CmccPublicMode.setSession(session);
            }

            @Override
            public void onReceiveAudioData(byte[] bytes, int i) {
                Log.e(TAG, "CallStateListener   onReceiveAudioData");
            }
        });
    }

    public static void logoutHJGH() {
        CmccPublicMode.logoutHJGH();
        CmccPublicMode.unRegisterConnectionStateListener();
    }


    /**
     * 绑定关系不依赖服务器通知，依赖andlink的广播。第一次绑定需要去从服务器拉取key,secret,contacts，然后init
     * 后面登录只需要拉取key,secret（检查一下是否非空，如果不是空就不拉）
     * 每次拨号前检查一下是否是登录状态，如果不是登录状态，需要去登录（登录前检查key secret）
     */
    public static void autoFetchKSandContactsFromServer(final Context context) {
        Log.i(TAG, "autoFetchKSandContactsFromServer: ");
        if (DeviceStatusUtil.getHJGHLoginStatus() == 1) {
            Log.i(TAG, "autoFetchKSandContactsFromServer,hjgh has login");
            return;
        }
        //判断是否有sharedpreference里是否有appkey,appsecret，没有的话需要从服务器获取,并拉取contacts
        AppInfo appInfo = DeviceStatusUtil.getKeyAndSecret(context);
        if (TextUtils.isEmpty(appInfo.getAppsecret())
                || TextUtils.isEmpty(appInfo.getAppKey())) {
            Log.i(TAG, "autoFetchKSandContactsFromServer,no key and secret,we should get from server");
            Api.fetchKeySecret(new Api.KeySecretCallBack() {
                @Override
                public void getKeySecret(AppInfo appInfo) {
                    Log.e(TAG, "key and secret null,parseContent,getKeySecret from server success");
                    DeviceStatusUtil.setKeyAndSecret(context, appInfo);
                    CmccService.initVoipAndFetchContacts2();
                }
            });
        } else {
            if (TextUtils.isEmpty(Constants.getDeviceId())) {   //如果dev
                // ceid是空，说明没有绑定
                showToast(context.getString(R.string.tts_device_unbind));
                AIManager.getInstance(context).playTextWithStr(context.getString(R.string.tts_device_unbind), null);
            } else {
                CmccService.initVoip();
            }
        }
    }

    /**
     * **********************************外部调用的方法********************************************
     */
    public static void pickUpCall() {
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, INCALL_PICKUP);
        DialerApplication.getContext().startService(service);
    }

    public static void hangUpCall() {
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, INCALL_HANGUP);
        DialerApplication.getContext().startService(service);
    }

    public static void initVoipAndFetchContacts2() {
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, INIT_VOIP_AND_FETCH_CONTACTS);
        DialerApplication.getContext().startService(service);
    }

    public static void logOut() {
        Log.i(TAG, "logOut: ");
        DeviceStatusUtil.setHJGHLoginStatus(0);
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, LOGOUT_HJGH);
        DialerApplication.getContext().startService(service);
    }

    public static void callOutByName(String name) {
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, CALL_OUT_BY_NAME);
        service.putExtra("name", name);
        DialerApplication.getContext().startService(service);
    }

    public static void callOutByNumber(String number) {
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, CALL_OUT_BY_NUMBER);
        service.putExtra("number", number);
        DialerApplication.getContext().startService(service);
    }

    public static void callOutByNameAndContactid(ContactInfo contact) {
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, CALL_OUT_BY_NAME_AND_CONTACTID);
        service.putExtra("name", contact.getNickname());
        service.putExtra("number", contact.getContactId());
        DialerApplication.getContext().startService(service);
    }

    public static void registerCallState() {
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, CALL_STATE_LISTENER_REGISTER);
        DialerApplication.getContext().startService(service);
    }

    public static void startFetchContacts() {
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, FETCH_CONTACTS);
        DialerApplication.getContext().startService(service);
    }

    public static void sendDTMF(int keycode) {
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, SEND_DTMF);
        service.putExtra("keycode", keycode);
        DialerApplication.getContext().startService(service);
    }

    //来电语音接听
    public static void voicePickUpCall() {
//        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
//        service.putExtra(CMD, VOICE_RECEIVE_CALL);
//        DialerApplication.getContext().startService(service);
        Intent intent = new Intent(ACTION_VOICE_PICKUP);
        DialerApplication.getContext().sendBroadcast(intent);
    }

    //语音挂断
    public static void voiceHangUpCall() {
//        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
//        service.putExtra(CMD, VOICE_REJECT_CALL);
//        DialerApplication.getContext().startService(service);
        Log.i(TAG, "voiceHangUpCall: ");
        //来电挂断（发送广播给GlowPadAnswerFragment）
        Intent intent = new Intent(ACTION_VOICE_HANGUP);
        DialerApplication.getContext().sendBroadcast(intent);
        //拨出电话未接时挂断
        hangUpCall();
    }

    public static void voiceUpdateLog() {
        Log.i(TAG, "voiceUpdateLog: ");
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, VOICE_UPDATE_LOG);
        DialerApplication.getContext().startService(service);
    }

    public static void reportTTS(String tts) {
        Log.i(TAG, "reportTTS: tts:" + tts);
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, TTS_REPORT);
        service.putExtra("tts", tts);
        DialerApplication.getContext().startService(service);
    }

    public static void showToast(String toast) {
        Log.i(TAG, "showToast: toast:" + toast);
        if (!TextUtils.isEmpty(toast)) {
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("toast_text", toast);
            message.setData(bundle);
            message.what = TOAST_SHOW;
            myHandler.sendMessage(message);
        }
    }

    public static void autoInit() {
        Log.i(TAG, "autoInit");
        Intent service = new Intent(DialerApplication.getContext(), CmccService.class);
        service.putExtra(CMD, AUTO_INIT);
        DialerApplication.getContext().startService(service);
    }
}
