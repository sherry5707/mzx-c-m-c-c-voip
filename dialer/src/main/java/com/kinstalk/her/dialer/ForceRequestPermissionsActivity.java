/*
 * Copyright (C) 2015 add by geniusgithub begin
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
 * limitations under the License.
 */
package com.kinstalk.her.dialer;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.kinstalk.her.cmccmode.utils.DeviceStatusUtil;
import com.kinstalk.her.contactscommon.common.util.PermissionsUtil;
import com.kinstalk.qloveaicore.AIManager;

import java.util.ArrayList;
import java.util.Arrays;

import static com.kinstalk.her.cmccmode.CmccService.ACTION_UNBIND_FROM_ANDLINK;
import static com.kinstalk.her.cmccmode.data.OpenQPushMessageService.ACTION_BIND_STATUS;
import static com.kinstalk.her.cmccmode.data.OpenQPushMessageService.EXTRA_BIND_STATUS;


@TargetApi(23)
public class ForceRequestPermissionsActivity extends Activity {


    private final static String TAG = "ForceRequestPermissionsActivity";

    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 1;

    public static final String PREVIOUS_ACTIVITY_INTENT = "previous_intent";
    public static final String ACTION_FINISH = "force_request_action_finish";
    private Intent mPreviousActivityIntent;
    private String[] requiredPermissions;
    private ArrayList<String> mForbiddenPermissionList = new ArrayList<String>();
    private TextView tipsTextView;

    private BroadcastReceiver bindStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UNBIND_FROM_ANDLINK.equals(intent.getAction())) {
                DeviceStatusUtil.setBindStatus(context, 0);
            }
            int status = intent.getIntExtra(EXTRA_BIND_STATUS, 0);
            if (status == 1) {
                requestNecessaryRequiredPermissions();
            }
            if(ACTION_FINISH.equals(intent.getAction()));{
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: ");
        Intent intent = getIntent();
        mPreviousActivityIntent = (Intent) intent.getExtras().get(PREVIOUS_ACTIVITY_INTENT);
        requiredPermissions = PermissionsUtil.sRequiredPermissions;

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.permission_check_activity);
        tipsTextView = findViewById(R.id.tips);
        registerBindReceiver();
        requestNecessaryRequiredPermissions();
        if (DeviceStatusUtil.getBindStatus(this) == 0) {
            AIManager.getInstance(DialerApplication.getContext()).playTextWithStr(
                    DialerApplication.getContext().getString(R.string.tts_device_unbind), null);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DeviceStatusUtil.getBindStatus(this) == 0) {
            tipsTextView.setText(R.string.required_bind_promo);
        } else {
            tipsTextView.setText(R.string.required_permissions_promo);
        }
    }

    private void registerBindReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BIND_STATUS);
        filter.addAction(ACTION_UNBIND_FROM_ANDLINK);
        filter.addAction(ACTION_FINISH);
        registerReceiver(bindStatusReceiver, filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bindStatusReceiver);
    }

    public static boolean startPermissionActivity(Activity activity) {
//        if (!PermissionsUtil.hasNecessaryRequiredPermissions(activity)) {
        Intent intent = new Intent(activity, ForceRequestPermissionsActivity.class);
        intent.putExtra(PREVIOUS_ACTIVITY_INTENT, activity.getIntent());
        activity.startActivity(intent);
        activity.finish();
        return true;
//        }
//        return false;
    }

    private void redirect() {
        mPreviousActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mPreviousActivityIntent);
        finish();
        overridePendingTransition(0, 0);
    }


    private void requestNecessaryRequiredPermissions() {
        Log.i(TAG, "requestNecessaryRequiredPermissions: ");
        final ArrayList<String> unsatisfiedPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                unsatisfiedPermissions.add(permission);
            }
        }
        if (unsatisfiedPermissions.size() == 0) {
            if (DeviceStatusUtil.getBindStatus(this) == 1) {
                Log.i(TAG, "requestNecessaryRequiredPermissions: permisson all get");
                DeviceStatusUtil.setPermissionStatus(this, true);
                redirect();
                return;
            } else {
                return;
            }
        }
        requestPermissions(unsatisfiedPermissions.toArray(new String[unsatisfiedPermissions
                .size()]), PERMISSIONS_REQUEST_ALL_PERMISSIONS);
    }

/*	private void requestSpecialPermissions(String permission, int requestCode){
		String []permissions = new String[]{permission};
		requestPermissions(permissions, requestCode);
	}*/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ALL_PERMISSIONS: {
                mForbiddenPermissionList.clear();
                if (permissions != null && permissions.length > 0
                        && isAllGranted(permissions, grantResults, mForbiddenPermissionList)) {
//                    redirect();
                    DeviceStatusUtil.setPermissionStatus(this, true);
                    if (DeviceStatusUtil.getBindStatus(this) == 1) {
                        redirect();
                    }
                } else {
                    Dialog dialog = PermissionsUtil.createPermissionSettingDialog(this, mForbiddenPermissionList.toString());
                    dialog.show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }


    }


    private boolean isAllGranted(String permissions[], int[] grantResult, ArrayList<String> forbiddenPermissionList) {
        boolean isAllGrand = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResult[i] != PackageManager.PERMISSION_GRANTED
                    && isPermissionRequired(permissions[i])) {
                forbiddenPermissionList.add(permissions[i]);
                isAllGrand = false;
            }
        }
        return isAllGrand;
    }

    private boolean isPermissionRequired(String p) {
        return Arrays.asList(requiredPermissions).contains(p);
    }
}
