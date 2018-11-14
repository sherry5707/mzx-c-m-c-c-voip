package com.kinstalk.her.cmccmode.data;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.github.promeg.pinyinhelper.Pinyin;
import com.kinstalk.her.cmccmode.CmccService;
import com.kinstalk.her.cmccmode.utils.CmdStructure;
import com.kinstalk.her.cmccmode.utils.DeviceStatusUtil;
import com.kinstalk.her.cmccmode.utils.PinYinTool;
import com.kinstalk.her.cmccmode.utils.RegexItem;
import com.kinstalk.her.cmccmode.utils.RegexList;
import com.kinstalk.her.dialer.DialerApplication;
import com.kinstalk.her.dialer.DialtactsActivity;
import com.kinstalk.her.dialer.R;
import com.kinstalk.qloveaicore.AIManager;
import com.kinstalk.qloveaicore.CmdCallback;
import com.tencent.xiaowei.info.QLoveResponseInfo;
import com.tencent.xiaowei.info.XWAppInfo;
import com.tencent.xiaowei.info.XWResponseInfo;

import org.apache.log4j.pattern.LogEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kinstalk.com.qloveaicore.AICoreDef;

import static com.kinstalk.her.cmccmode.utils.PinYinTool.isPinYinChar;
import static com.kinstalk.her.dialer.DialtactsActivity.EXTRA_SHOW_TAB;
import static com.kinstalk.her.dialer.list.ListsFragment.TAB_INDEX_ALL_CONTACTS;
import static com.kinstalk.her.dialer.list.ListsFragment.TAB_INDEX_HISTORY;
import static com.kinstalk.her.dialer.list.ListsFragment.TAB_SHOW_DIAPAD;

public class VoipIntentService extends IntentService {
    private static final String TAG = "VoipIntentService";
    private static final String INTENT_MAKE_CALL = "makeCall";
    private static final String INTENT_OPEN_CALL_LOG = "openCallRecord";
    private static final String INTENT_OPEN_CMCC_VOIP = "openCmccVoip";
    private static final String INTENT_OPEN_CONTACTS = "openContactList";
    private static final String INTENT_OPEN_DIAPADS = "openNumerKeyboard";
    private static final String INTENT_RECALL = "reCall";
    private static final String INTENT_REJECTCALL = "rejectCmccVoipCall";
    private static final String INTENT_ACCEPTCALL = "acceptCmccVoipCall";
    private static final String INTENT_UPDATELOG = "reportCmccVoipLog";
    private static final String CMCC_VOIP_SKILL_ID = "8dab4796-fa37-1441-5971-9672f7f22000";
    //弱网环境下，本地对打电话命令进行解析并传过来
    private static final String PERTER_ACTION_CMD = "peter_action_cmd";

    private static final MyCallback callback = new MyCallback();

    private static class MyCallback extends CmdCallback {

        @Override
        public void handleQLoveResponseInfo(String s, QLoveResponseInfo qLoveResponseInfo, byte[] bytes) {
            Log.d(TAG, "handleQLoveResponseInfo");
            //app在AICore中的注册成功后，后续相关的语音命令处理工作都在改方法中处理。
            //voip 相关工作[开始]
            processData(s, qLoveResponseInfo, bytes);
            //voip 相关工作[结束]
        }
    }

    public VoipIntentService() {
        super("VoipIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent: ");
        if (intent != null) {
            String voiceId = intent.getStringExtra(AICoreDef.AI_INTENT_SERVICE_VOICEID);
            QLoveResponseInfo rspData = intent.getParcelableExtra(AICoreDef.AI_INTENT_SERVICE_INFO);
            byte[] extendData = intent.getByteArrayExtra(AICoreDef.AI_INTENT_SERVICE_DATA);
            //Log.d(TAG, " onHandleIntent ");
            Log.d(TAG, " intent service " + intent + "rspData " + rspData + " extradata " + extendData);
            //app在未注册到AICore时，处理相关的语音，后续流程会有注册动作，
            //后续的处理工作在CmdCallback的回调方法handleQLoveResponseInfo中执行。
            //XXXXXXX 相关工作[开始]
            processData(voiceId, rspData, extendData);

            String peterCmdString = intent.getStringExtra(PERTER_ACTION_CMD);
            if (!TextUtils.isEmpty(peterCmdString)) {
                selfDealCmd(peterCmdString);
            }
        }
    }

    /**
     * 相关工作的示例方法，请按照应用实际情况定义
     */
    @SuppressLint("StringFormatInvalid")
    private static void processData(String voiceId, QLoveResponseInfo qLoveResponseInfo, byte[] extendData) {
        if (qLoveResponseInfo != null) {
            XWResponseInfo xwResponseInfo = qLoveResponseInfo.xwResponseInfo;
            String requestText = xwResponseInfo.requestText;//语音文本
            Log.i(TAG, "processData: requestText:" + requestText);
            XWAppInfo appInfo = xwResponseInfo.appInfo;
            Log.i(TAG, "processData: appInfo:" + appInfo);
            String skillId = appInfo.ID;
            Log.i(TAG, "processData: skill-ID:" + skillId);

            if (CMCC_VOIP_SKILL_ID.equals(skillId)) {
                //腾讯可识别的
                String rspData = xwResponseInfo.responseData;//JSONObject数据
                Log.i(TAG, "processData: responseData:" + rspData);
                try {
                    JSONObject jsonObj = new JSONObject(rspData);
                    if (jsonObj.has("intent" +
                            "Name")) {
                        //获取意图：intentName
                        String intentName = jsonObj.optString("intentName");
                        Log.i(TAG, "processData: intetnName;" + intentName);
                        if (INTENT_MAKE_CALL.equals(intentName)) {
                            if (DeviceStatusUtil.getBindStatus(DialerApplication.getContext()) == 0) {
                                AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                                        DialerApplication.getContext().getString(R.string.tts_device_unbind), null);
                                return;
                            } else if (DeviceStatusUtil.getEnabledStatus(DialerApplication.getContext()) == 0) {
                                AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                                        DialerApplication.getContext().getString(R.string.tts_device_disabled), null);
                                return;
                            }
                            //打电话
                            if (jsonObj.has("slots")) {
                                JSONArray jsonArray = jsonObj.getJSONArray("slots");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    //根据key来解析所需的信息
                                    String key = jsonObject.optString("key");
                                    String value = jsonObject.optString("value");
                                    Log.i(TAG, "processData: key:" + key + ",value:" + value);
                                    if ("name".equals(key)) {
                                        if (!TextUtils.isEmpty(value)) {
                                            ArrayList<String> pinyinResult = PinYinTool.getPinyinArrayFromString(value);
                                            Log.e(TAG, "processData: PinYinTool getPinyin:" + pinyinResult);
                                            for (String py : pinyinResult) {
                                                int j = 0;
                                                for (; j < pinyinResult.size(); j++) {
                                                    Log.e(TAG, "processData: PinYinTool getPinyin:" + pinyinResult.get(i));
                                                    ContactInfo contact = MyDBProviderHelper.getContactInfoByPinYin(pinyinResult.get(i));
                                                    if (contact != null) {
                                                        CmccService.callOutByNameAndContactid(contact);
                                                        break;
                                                    }
                                                }
                                                if (j == pinyinResult.size()) {
                                                    Log.i(TAG, "processData: self skill,call out,no such contact");
                                                    CmccService.reportTTS(
                                                            String.format(DialerApplication.getContext().getString(R.string.voice_callout_no_contacts),
                                                                    value));
                                                }
                                            }
                                        }
                                    } else if ("tel_number".equals(key)) {
                                        if (!TextUtils.isEmpty(value)) {
                                            Log.i(TAG, "processData: INTENT_MAKE_CALL by number:" + value);
                                            CmccService.callOutByNumber(value);
                                        }
                                    } else if ("number".equals(key)) {
                                        if (!TextUtils.isEmpty(value)) {
                                            Log.i(TAG, "processData: INTENT_MAKE_CALL by number:" + value);
                                            CmccService.callOutByNumber(value);
                                        }
                                    } else if ("person".equals(key)) {
                                        if (!TextUtils.isEmpty(value)) {
                                            Log.i(TAG, "processData: INTENT_MAKE_CALL by person:" + value);
                                            CmccService.callOutByName(value);
                                        }
                                    }
                                }
                            } else {
                                Log.e(TAG, "bad responseData.slots , can not handle it anymore!");
                            }
                        } else if (INTENT_OPEN_CALL_LOG.equals(intentName)) {
                            //打开通话记录
                            Intent intent = new Intent(DialerApplication.getContext(), DialtactsActivity.class);
                            intent.putExtra(EXTRA_SHOW_TAB, TAB_INDEX_HISTORY);
                            DialerApplication.getContext().startActivity(intent);
                        } else if (INTENT_OPEN_CMCC_VOIP.equals(intentName)) {
                            //打开和家固话app
                            Intent intent = new Intent(DialerApplication.getContext(), DialtactsActivity.class);
                            DialerApplication.getContext().startActivity(intent);
                        } else if (INTENT_OPEN_CONTACTS.equals(intentName)) {
                            //打开联系人
                            Intent intent = new Intent(DialerApplication.getContext(), DialtactsActivity.class);
                            intent.putExtra(EXTRA_SHOW_TAB, TAB_INDEX_ALL_CONTACTS);
                            DialerApplication.getContext().startActivity(intent);
                        } else if (INTENT_OPEN_DIAPADS.equals(intentName)) {
                            //打开拨号盘
                            Intent intent = new Intent(DialerApplication.getContext(), DialtactsActivity.class);
                            intent.putExtra(EXTRA_SHOW_TAB, TAB_SHOW_DIAPAD);
                            DialerApplication.getContext().startActivity(intent);
                        } else if (INTENT_RECALL.equals(intentName)) {
                            //重拨/拨打上一个电话号码
                            ContactInfo contact = MyDBProviderHelper.getLastRecordContact(DialerApplication.getContext());
                            CmccService.callOutByNumber(contact.getContactId());
//                        DialerApplication.callOut(DialerApplication.getContext(), contact.getContactId());
                        } else if (INTENT_ACCEPTCALL.equals(intentName)) {
                            CmccService.voicePickUpCall();
                        } else if (INTENT_REJECTCALL.equals(intentName)) {
                            CmccService.voiceHangUpCall();
                        } else if (INTENT_UPDATELOG.equals(intentName)) {
                            CmccService.voiceUpdateLog();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //腾讯不能识别的打电话语义
                selfDealCmd(requestText);
            }
        }
    }

    //通过正则表达式自己去解析
    public static void selfDealCmd(String requestText) {
        CmdStructure cmd = requestMachRegex(requestText);
        if (cmd != null) {
            if (INTENT_MAKE_CALL.equals(cmd.getIntentName())) {
                if (DeviceStatusUtil.getBindStatus(DialerApplication.getContext()) == 0) {
                    AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                            DialerApplication.getContext().getString(R.string.tts_device_unbind), null);
                    return;
                } else if (DeviceStatusUtil.getEnabledStatus(DialerApplication.getContext()) == 0) {
                    AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                            DialerApplication.getContext().getString(R.string.tts_device_disabled), null);
                    return;
                }
                if ("name".equals(cmd.getKey())) {
                    if (!TextUtils.isEmpty(cmd.getValue())) {
                        ArrayList<String> pinyinResult = PinYinTool.getPinyinArrayFromString(cmd.getValue());
                        Log.i(TAG, "processData: PinYinTool getPinyin:" + pinyinResult);
                        int i = 0;
                        for (; i < pinyinResult.size(); i++) {
                            Log.e(TAG, "processData: PinYinTool getPinyin:" + pinyinResult.get(i));
                            ContactInfo contact = MyDBProviderHelper.getContactInfoByPinYin(pinyinResult.get(i));
                            if (contact != null) {
                                CmccService.callOutByNameAndContactid(contact);
                                break;
                            }
                        }
                        if (i == pinyinResult.size()) {
                            Log.i(TAG, "processData: self skill,call out,no such contact");
                            CmccService.reportTTS(
                                    String.format(DialerApplication.getContext().getString(R.string.voice_callout_no_contacts),
                                            cmd.getValue()));
                        }
                    }
                } else if ("number".equals(cmd.getKey())) {
                    Log.i(TAG, "processData: self skill,call out by number:" + cmd.getValue());
                    CmccService.callOutByNumber(cmd.getValue());
                }
            }
        }
    }

    public static CmdStructure requestMachRegex(String requestText) {
        ArrayList<RegexItem> regexList = new RegexList().contents;
        int k = 0;
        for (int num = 0; num < regexList.size(); num++) {
            Pattern r = regexList.get(num).regex;
            String[] tags = regexList.get(num).tags;

            // 现在创建 matcher 对象
            Matcher m = r.matcher(requestText);
            if (m.find()) {
                System.out.println("Pattern: " + r.pattern());
                System.out.println(requestText + ">>> Much: " + m.group(0));
                String key = null;
                String value = null;
                for (int seq = 1; seq <= tags.length; seq++) {
                    System.out.println(tags[seq - 1] + ": " + m.group(seq));
                    key = tags[seq - 1];
                    value = m.group(seq);
                }
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    CmdStructure cmd;
                    if (!isValueNumber(value)) {
                        cmd = new CmdStructure(regexList.get(num).intentName, "name", value);
                    } else {
                        cmd = new CmdStructure(regexList.get(num).intentName, "number", value);
                    }
                    Log.i(TAG, "requestMachRegex: cmd:" + cmd);
                    return cmd;
                }
                k++;
            } else {
                System.out.println("NO MATCH: " + requestText);
            }
        }
        return null;
    }

    /**
     * 判断是否是电话号码
     *
     * @param value
     * @return
     */
    public static boolean isValueNumber(String value) {
        String number;
        try {
            number = String.valueOf(Long.parseLong(value));
            return true;
        } catch (NumberFormatException e) {
            Log.i(TAG, "isValueNumber: not number");
            return false;
        }
    }


    public static String buildJson(String type, String pkg, String svc) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("pkg", pkg);
            json.put("svcClass", svc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
