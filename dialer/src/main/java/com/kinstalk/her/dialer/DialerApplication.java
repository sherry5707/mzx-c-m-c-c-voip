/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.kinstalk.her.dialer;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.kinstalk.her.cmccmode.CmccService;
import com.kinstalk.her.cmccmode.data.AWSPush;
import com.kinstalk.her.cmccmode.utils.CmccPublicMode;
import com.kinstalk.her.cmccmode.utils.DeviceStatusUtil;
import com.kinstalk.her.cmccmode.utils.VoipLog;
import com.kinstalk.her.contactscommon.common.extensions.ExtensionsFactory;
import com.kinstalk.her.dialer.database.FilteredNumberAsyncQueryHandler;
import com.kinstalk.her.dialer.filterednumber.BlockedNumbersAutoMigrator;
import com.kinstalk.her.incallui.Log;
import com.mobile.voip.sdk.api.CMImsManager;
import com.mobile.voip.sdk.callback.VoIP;

public class DialerApplication extends Application {

    private static final String TAG = "DialerApplication";
    private static final String ACTION_DEVICEID_GET = "com.kinstalk.qloveandlinkapp.ANDLINKDONE";

    private static Context sContext;
    @Override
    public void onCreate() {
        sContext = this;
        Trace.beginSection(TAG + " onCreate");
        super.onCreate();
        Trace.beginSection(TAG + " ExtensionsFactory initialization");
        ExtensionsFactory.init(getApplicationContext());
        Trace.endSection();
        new BlockedNumbersAutoMigrator(PreferenceManager.getDefaultSharedPreferences(this),
                new FilteredNumberAsyncQueryHandler(getContentResolver())).autoMigrate();
        Trace.endSection();

        //推送服务注册
        Log.i(TAG, "onCreate: this version is 20181029");
        //AWSPush.bindService(this);
        Intent service = new Intent(this, CmccService.class);
        startService(service);
    }

    @Nullable
    public static Context getContext() {
        return sContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
