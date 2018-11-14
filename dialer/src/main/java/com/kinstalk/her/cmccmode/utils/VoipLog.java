package com.kinstalk.her.cmccmode.utils;

import android.os.Build;
import android.util.Log;

import okhttp3.logging.HttpLoggingInterceptor;

public class VoipLog {
    //当前Debug模式
    public static boolean DE_BUG = true;
    public static void e(String tag, String text) {
        if (DE_BUG) {
            Log.e(tag, text);
        }
    }

    public static void d(String tag, String text) {
        if (DE_BUG) {
            Log.d(tag, text);
        }
    }

    public static void i(String tag, String text) {
        if (DE_BUG) {
            Log.i(tag, text);
        }
    }

    public static void w(String tag, String text) {
        if (DE_BUG) {
            Log.w(tag, text);
        }
    }

    public static void v(String tag, String text) {
        if (DE_BUG) {
            Log.v(tag, text);
        }
    }

    public static class HttpLogger implements HttpLoggingInterceptor.Logger {
        private static final String PREFIX = "VoipOK-";
        private String mTag;

        public HttpLogger(String tag) {
            mTag = tag;
        }

        @Override
        public void log(String message) {
            VoipLog.d(mTag,message);
        }
    }

    public static boolean isUserType() {
        return Build.TYPE.equals("user");
    }
}
