package com.kinstalk.her.cmccmode.data;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.kinstalk.her.cmccmode.utils.SystemTool;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;

public class Api {
    private static final String TAG = "Api";
    public static String TEST_URL = "https://cmcc-test.kinstalk.com";
    public static String PRODUCT_URL = "https://cmcc.kinstalk.com";
    private static final String GET_CONTACT_PATH = "cmcc/concat";
    private static final String GET_APPKEY_PATH = "cmcc/appkey";

    /**
     * 获取所有联系人
     *
     * @param callBack
     */
    public static void fetchAllContacts(final AllContactsCallBack callBack) {
        VoipThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                fetchContactPublic(null, callBack);
            }
        });
    }

    private static Request createAllContactsRequest() {
        Log.i(TAG, "createPhoneNumbRequest");
        HttpUrl httpUrl = new Request.Builder()
                .url(PRODUCT_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(GET_CONTACT_PATH)
                .addQueryParameter("sn", "83"+SystemTool.getMacForSn())
                .build();

        Log.i(TAG, "createPhoneNumbRequest:data = " + httpUrl.toString());

        return new Request.Builder()
                .url(httpUrl)
                .get()//requestBody)
                .build();
    }

    /**
     * 根据nickName获取某个联系人的信息
     *
     * @param callBack
     */
    public static void fetchContactByName(final String nickname, final AllContactsCallBack callBack) {
        VoipThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                fetchContactPublic(nickname, callBack);
            }
        });
    }

    private static Request createContactRequest(String nickName) {
        Log.i(TAG, "createPhoneNumbRequest");
        HttpUrl httpUrl = new Request.Builder()
                .url(PRODUCT_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(GET_CONTACT_PATH)
                .addQueryParameter("sn", "83"+SystemTool.getMacForSn())
                .addQueryParameter("nickname", nickName)
                .build();

        Log.i(TAG, "createPhoneNumbRequest:data = " + httpUrl.toString());

        return new Request.Builder()
                .url(httpUrl)
                .get()//requestBody)
                .build();
    }

    public interface AllContactsCallBack {
        void getAllContacts(List<ContactInfo> contacts);
    }

    private static void fetchContactPublic(final String nickname, final AllContactsCallBack callBack) {
        Log.i(TAG, "fetchContactPublic: Enter");
        okhttp3.Response response;

        try {
            if (TextUtils.isEmpty(nickname)) {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createContactRequest(nickname)).execute();
            } else {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createAllContactsRequest()).execute();

            }
            //判断请求是否成功
            if (response == null) {
                Log.e(TAG, "fetchContactPublic response empty");
            } else if (!response.isSuccessful()) {
                Log.e(TAG, "fetchContactPublic: response:" + response);
                Log.e(TAG, "fetchContactPublic response failed");
            } else {
                if (!HttpHeaders.hasBody(response)) {
                    Log.e(TAG, "fetchContactPublic rspBody empty");
                } else {
                    //打印服务端返回结果
                    ResponseBody rspBody = response.body();
                    long contentLength = rspBody.contentLength();
                    if (contentLength != 0) {
                        String sBody = rspBody.string();
                        Log.i(TAG, "fetchContactPublic body = : " + sBody);
                        try {
                            JSONObject rspJson = JSONObject.parseObject(sBody);
                            AllContactsResponse responseObj = JSONObject.parseObject(rspJson.toJSONString(), AllContactsResponse.class);
                            Log.i(TAG, "fetchContactPublic: responseObj:" + responseObj.toString());
                            callBack.getAllContacts(responseObj.getAllContacts());
                        } catch (JSONException e) {
                            callBack.getAllContacts(null);
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            callBack.getAllContacts(null);
            e.printStackTrace();
        }
    }

    /**
     * 获取AppKey和AppSecret
     *
     * @param callBack
     */
    public static void fetchKeySecret(final KeySecretCallBack callBack) {
        VoipThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "fetchKeySecret: Enter");
                okhttp3.Response response;

                try {
                    response = OkhttpClientHelper.getOkHttpClient().newCall(createKSRequest()).execute();
                    //判断请求是否成功
                    if (response == null) {
                        Log.e(TAG, "fetchKeySecret response empty");
                    } else if (!response.isSuccessful()) {
                        Log.e(TAG, "run: response:" + response);
                        Log.e(TAG, "fetchKeySecret response failed");
                    } else {
                        if (!HttpHeaders.hasBody(response)) {
                            Log.e(TAG, "fetchKeySecret rspBody empty");
                        } else {
                            //打印服务端返回结果
                            ResponseBody rspBody = response.body();
                            long contentLength = rspBody.contentLength();
                            if (contentLength != 0) {
                                String sBody = rspBody.string();
                                Log.i(TAG, "fetchKeySecret body = : " + sBody);
                                try {
                                    JSONObject rspJson = JSONObject.parseObject(sBody);
                                    AppInfoResponse responseObj = JSONObject.parseObject(rspJson.toJSONString(), AppInfoResponse.class);
                                    Log.i(TAG, "fetchKeySecret: responseObj:" + responseObj.toString());
                                    callBack.getKeySecret(responseObj.getResult());
                                } catch (JSONException e) {
                                    callBack.getKeySecret(null);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    callBack.getKeySecret(null);
                    e.printStackTrace();
                }
            }
        });
    }

    private static Request createKSRequest() {
        Log.i(TAG, "createKeySecretRequest");
        HttpUrl httpUrl = new Request.Builder()
                .url(PRODUCT_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(GET_APPKEY_PATH)
                .build();

        Log.i(TAG, "createPhoneNumbRequest:data = " + httpUrl.toString());

        return new Request.Builder()
                .url(httpUrl)
                .get()//requestBody)
                .build();
    }

    public interface KeySecretCallBack {
        void getKeySecret(AppInfo appInfo);
    }
}
