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

package com.kinstalk.her.incallui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telecom.VideoProfile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kinstalk.her.dialer.R;

// M: add for plugin. @{
/// @}


public class GlowPadAnswerFragment extends AnswerFragment {

    private GlowPadWrapper mGlowpad;


    public static final String ACTION_VOICE_PICKUP = "action_voice_pickup";
    public static final String ACTION_VOICE_HANGUP = "action_voice_hangup";
    public static final String ACTION_RESTART_RING = "aciton_restart_ring";
    public static final String ACTION_STOP_RING = "aciton_stop_ring";

    private BroadcastReceiver mVoiceCmdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(this,"action:"+intent.getAction());
            if (ACTION_VOICE_HANGUP.equals(intent.getAction())) {
                mGlowpad.stopRing();
                onDecline(context);
                mGlowpad.mTargetTriggered = true;
                mGlowpad.isIncall = false;
            } else if (ACTION_VOICE_PICKUP.equals(intent.getAction())) {
                mGlowpad.stopRing();
                mGlowpad.isIncall = true;
                onAnswer(VideoProfile.STATE_AUDIO_ONLY, getContext());
                mGlowpad.mTargetTriggered = true;
            } else if (ACTION_RESTART_RING.equals(intent.getAction())){
                mGlowpad.startRing();
            } else if(ACTION_STOP_RING.equals(intent.getAction())){
//                mGlowpad.stopRing();
                //防止trigger后又开始响
//                mGlowpad.isIncall = true;
            }
        }
    };

    private void registerVoiceCmdReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_VOICE_HANGUP);
        filter.addAction(ACTION_VOICE_PICKUP);
        filter.addAction(ACTION_RESTART_RING);
        filter.addAction(ACTION_STOP_RING);
        getContext().registerReceiver(mVoiceCmdReceiver, filter);
    }

    public GlowPadAnswerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGlowpad = (GlowPadWrapper) inflater.inflate(R.layout.answer_fragment,
                container, false);

        Log.d(this, "Creating view for answer fragment ", this);
        Log.d(this, "Created from activity", getActivity());
        mGlowpad.setAnswerFragment(this);

//        ExtensionManager.getVilteAutoTestHelperExt().registerReceiverForAcceptAndRejectUpgrade(
//                getActivity(),InCallPresenter.getInstance().getAnswerPresenter());
        registerVoiceCmdReceiver();
        return mGlowpad;
    }

    @Override
    public void onResume() {
        super.onResume();
        mGlowpad.requestFocus();
        onShowAnswerUi(true);
    }

    @Override
    public void onDestroyView() {
        Log.d(this, "onDestroyView");
        if (mGlowpad != null) {
            mGlowpad.stopPing();
            mGlowpad.stopRingAndRelease();
            mGlowpad = null;
        }
//        ExtensionManager.getVilteAutoTestHelperExt().unregisterReceiverForAcceptAndRejectUpgrade();
        getContext().unregisterReceiver(mVoiceCmdReceiver);
        super.onDestroyView();
    }

    @Override
    public void onShowAnswerUi(boolean shown) {
        Log.d(this, "Show answer UI: " + shown);
        if (shown) {
            mGlowpad.startPing();
        } else {
            mGlowpad.stopPing();
        }
    }

    /**
     * Sets targets on the glowpad according to target set identified by the parameter.
     *
     * @param targetSet Integer identifying the set of targets to use.
     */
    public void showTargets(int targetSet) {
        showTargets(targetSet, VideoProfile.STATE_BIDIRECTIONAL);
    }

    /**
     * Sets targets on the glowpad according to target set identified by the parameter.
     *
     * @param targetSet Integer identifying the set of targets to use.
     */
    @Override
    public void showTargets(int targetSet, int videoState) {
        final int targetResourceId;
        final int targetDescriptionsResourceId;
        final int directionDescriptionsResourceId;
        final int handleDrawableResourceId;
        mGlowpad.setVideoState(videoState);
        Log.i(this, "showTargets,tartetSet:" + targetSet);
        switch (targetSet) {
            case TARGET_SET_FOR_AUDIO_WITH_SMS:
                targetResourceId = R.array.incoming_call_widget_audio_with_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_with_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_with_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
                break;
            case TARGET_SET_FOR_VIDEO_WITHOUT_SMS:
                targetResourceId = R.array.incoming_call_widget_video_without_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_video_without_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_video_without_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            case TARGET_SET_FOR_VIDEO_WITH_SMS:
                targetResourceId = R.array.incoming_call_widget_video_with_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_video_with_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_video_with_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            case TARGET_SET_FOR_VIDEO_ACCEPT_REJECT_REQUEST:
                targetResourceId =
                        R.array.incoming_call_widget_video_request_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_video_request_target_descriptions;
                directionDescriptionsResourceId = R.array
                        .incoming_call_widget_video_request_target_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            /**
             * M: [video call]3G Video call doesn't support answer as audio, and reject via SMS. @{
             */
            case TARGET_SET_FOR_VIDEO_WITHOUT_SMS_AUDIO:
                targetResourceId = R.array.mtk_incoming_call_widget_video_without_sms_audio_targets;
                targetDescriptionsResourceId =
                        R.array.
                                mtk_incoming_call_widget_video_without_sms_audio_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.
                                mtk_incoming_call_widget_video_without_sms_audio_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            /** @}*/
            case TARGET_SET_FOR_AUDIO_WITHOUT_SMS:
            default:
                targetResourceId = R.array.incoming_call_widget_audio_without_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_without_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_without_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
                break;
        }

        if (targetResourceId != mGlowpad.getTargetResourceId()) {
            mGlowpad.setTargetResources(targetResourceId);
            mGlowpad.setTargetDescriptionsResourceId(targetDescriptionsResourceId);
            mGlowpad.setDirectionDescriptionsResourceId(directionDescriptionsResourceId);
            mGlowpad.setHandleDrawable(handleDrawableResourceId);
            mGlowpad.reset(false);
            /// M: Force layout to avoid UI abnormally.
            mGlowpad.requestLayout();
        }
    }

    @Override
    protected void onMessageDialogCancel() {
        if (mGlowpad != null) {
            mGlowpad.startPing();
        }
    }
}
