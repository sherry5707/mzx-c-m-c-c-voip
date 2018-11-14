package com.kinstalk.her.cmccmode.utils;

import android.content.Context;
import android.util.Log;

import com.kinstalk.her.cmccmode.CmccService;
import com.kinstalk.her.cmccmode.data.Api;
import com.kinstalk.her.cmccmode.data.Constants;
import com.kinstalk.her.cmccmode.data.ContactInfo;
import com.kinstalk.her.cmccmode.data.MyDBProviderHelper;
import com.kinstalk.her.cmccmode.data.VoipThreadManager;
import com.kinstalk.her.dialer.DialerApplication;
import com.kinstalk.her.dialer.R;
import com.mobile.voip.sdk.api.CMImsManager;
import com.mobile.voip.sdk.api.utils.VoIPServerConnectListener;
import com.mobile.voip.sdk.callback.QueryContactCallBack;
import com.mobile.voip.sdk.callback.VoIP;
import com.mobile.voip.sdk.callback.VoIPCallStateCallBack;
import com.mobile.voip.sdk.callback.VoIPInComingCallListener;

import java.util.List;

public class CmccPublicMode {
    private static final String TAG = "CmccPublicMode";
    private static VoIPServerConnectListener connectListener;
    private static VoIPInComingCallListener comingCallListener;
    private static VoIPCallStateCallBack callStateCallBack;
    private static int session;

    public static int getSession() {
        return session;
    }

    public static void setSession(int session) {
        CmccPublicMode.session = session;
    }

    /**
     * 设置设备信息
     */
    public static void setDeviceInfo(String appSecret) {
        try {
            Log.e(TAG, "setDeviceInfo");
            CMImsManager.getInstance().setDeviceId(Constants.enCiper(appSecret));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateLog() {
        CmccPublicMode.updateLog(new VoIP.CallBack() {
            @Override
            public void onSuccess() {
                VoipLog.i(TAG, DialerApplication.getContext().getString(R.string.update_log_success));
            }

            @Override
            public void onFailed(int code, String errorString) {
                VoipLog.i(TAG, DialerApplication.getContext().getString(R.string.update_log_fail)
                        + ",code:" + code + ",reson:" + errorString);
            }
        });
    }

    /**
     * 注册连接状态监听并拉取联系人
     * 监听和家固话登录服务状态，包含登录成功、失败。重连等状态
     *
     * @param context
     */
    public static void registerConnectionStateListenerAndFetchContact(final Context context) {
        connectListener = new VoIPServerConnectListener() {
            @Override
            public void onLoginSucceed(int sipFlag) {
                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.login_success) + ",type:" + sipFlag);
                CmccService.startFetchContacts();
            }

            @Override
            public void onLoginFailed(int failedReason, int sipFlag) {
                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.login_fail) + ",type:" + sipFlag + ",reason:" + failedReason);
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
        };
        CMImsManager.getInstance().addServerConnectListener(connectListener);
    }

    /**
     * 注册连接状态监听
     * 监听和家固话登录服务状态，包含登录成功、失败。重连等状态
     *
     * @param connectListener
     */
    public static void registerConnectionStateListener(VoIPServerConnectListener connectListener) {
//        connectListener = new VoIPServerConnectListener() {
//            @Override
//            public void onLoginSucceed(int sipFlag) {
//                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.login_success) + ",type:" + sipFlag);
//            }
//
//            @Override
//            public void onLoginFailed(int failedReason, int sipFlag) {
//                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.login_fail) + ",type:" + sipFlag + ",reason:" + failedReason);
//            }
//
//            @Override
//            public void onImsLogging(int result, int sipFlag) {
//                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.logining) + ",type:" + sipFlag + ",result:" + result);
//            }
//
//            @Override
//            public void onDisConnected(int reason) {
//                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.disconect) + ",reson:" + reason);
//            }
//
//            @Override
//            public void onConnectSucceed() {
//                VoipLog.i(TAG, "ConnectionState:" + context.getString(R.string.connect_success));
//            }
//        };
        CMImsManager.getInstance().addServerConnectListener(connectListener);
    }

    /**
     * 注销服务器连接状态监听
     */
    public static void unRegisterConnectionStateListener() {
        if (connectListener != null) {
            CMImsManager.getInstance().removeServerConnectListener(connectListener);
        }
    }

    /**
     * 注册来电监听
     * 如果当前有来电则进⼊来电监听的回调⽅法中
     *
     * @param context
     */
    public static void registerComingCallListener(final Context context, VoIP.OnInComingCallListener listener) {
        comingCallListener = listener;
        CMImsManager.getInstance().setInComingCallListener(comingCallListener);
    }

    /**
     * 注册通话状态监听
     * 监听电话的各种状态，拨打中、振铃中、拨打失败、接通、释放等；在收到来电后注册即可
     *
     * @param listener
     */
    public static void registerCallStateListener(VoIPCallStateCallBack listener) {
        callStateCallBack = listener;
        CMImsManager.getInstance().addCallStateListener(listener);
    }

    /**
     * 注销通话状态监听
     */
    public static void unRegisterCallStateListener() {
        if (callStateCallBack != null) {
            CMImsManager.getInstance().removeCallStateListener(callStateCallBack);
        }
    }

    /**
     * 登录和家固话
     */
    public static void loginHJGH() {
        CMImsManager.getInstance().loginIms();
    }

    /**
     * 登出和家固话
     */
    public static void logoutHJGH() {
        CMImsManager.getInstance().doLogoutIms();
    }

    /**
     * 日志上传
     * 发生异常时，厂商将异常传回
     *
     * @param callBack
     */
    public static void updateLog(VoIP.CallBack callBack) {
        CMImsManager.getInstance().uploadLogcat(Constants.getLogPath(), callBack);
    }

    public static void getNickNameByNumber(String PhoneNumber, final QueryContactCallBack callBack) {
        CMImsManager.getInstance().queryImsContact(PhoneNumber, callBack);
    }

    /**
     * 通话过程中发送DTMF，⽐如打10086，按语⾳提示输⼊数字
     *
     * @param keycode
     */
    public static void sendDTMF(int keycode) {
        Log.i(TAG, "sendDTMF: keyCode:" + keycode);
        CMImsManager.getInstance().sendDTMF(session, keycode);
    }


    //add by mengzhaoxue to fetch contacts info from server
    public static void startFetchContacts() {
        Log.i(TAG, "startFetchContacts: ");
        Api.fetchAllContacts(new Api.AllContactsCallBack() {
            @Override
            public void getAllContacts(final List<ContactInfo> contacts) {
                if (contacts != null && contacts.size() > 0) {
                    Log.i(TAG, "getAllContacts,contacts:" + contacts);
                    VoipThreadManager.getInstance().start(new Runnable() {
                        @Override
                        public void run() {
                            //清空本地联系人
                            MyDBProviderHelper.clearContactsDB();
                            int status = MyDBProviderHelper.insertAllContacts(contacts);
                            Log.i(TAG, "startFetchContacts,insert contacts to my db status:"+status);
                        }
                    });
                    VoipThreadManager.getInstance().start(new Runnable() {
                        @Override
                        public void run() {
                            //清空系统联系人
                            MyDBProviderHelper.clearSystemContactsDB();
                            MyDBProviderHelper.insertContactToSystemDB(contacts);
                        }
                    });
                } else {
                    Log.e(TAG, "server has no contacts or fetch contacts error");
                    MyDBProviderHelper.clearContactsDB();
                    MyDBProviderHelper.clearSystemContactsDB();
                }
            }
        });
    }
}
