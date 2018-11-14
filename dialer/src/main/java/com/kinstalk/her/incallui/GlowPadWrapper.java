/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
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

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telecom.VideoProfile;
import android.util.AttributeSet;
import android.view.View;

import com.kinstalk.her.incallui.widget.multiwaveview.GlowPadView;

import java.io.IOException;

/**
 *
 */
public class GlowPadWrapper extends GlowPadView implements GlowPadView.OnTriggerListener {

    // Parameters for the GlowPadView "ping" animation; see triggerPing().
    private static final int PING_MESSAGE_WHAT = 101;
    private static final boolean ENABLE_PING_AUTO_REPEAT = true;
    private static final long PING_REPEAT_DELAY_MS = 1200;
    public boolean isIncall = false;

    private final Handler mPingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PING_MESSAGE_WHAT:
                    triggerPing();
                    break;
            }
        }
    };

    private AnswerFragment mAnswerFragment;
    private boolean mPingEnabled = true;
    public boolean mTargetTriggered = false;
    private int mVideoState = VideoProfile.STATE_BIDIRECTIONAL;
    private MediaPlayer mRingMediaPlayer;

    public GlowPadWrapper(Context context) {
        super(context);
        Log.d(this, "class created " + this + " ");
    }

    public GlowPadWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(this, "class created " + this);
    }

    @Override
    protected void onFinishInflate() {
        Log.d(this, "onFinishInflate()");
        super.onFinishInflate();
        setOnTriggerListener(this);
    }

    public void startPing() {
        Log.d(this, "startPing");
        mPingEnabled = true;
        triggerPing();
    }

    public void stopPing() {
        Log.d(this, "stopPing");
        mPingEnabled = false;
        mPingHandler.removeMessages(PING_MESSAGE_WHAT);
    }

    private void triggerPing() {
        Log.d(this, "triggerPing(): " + mPingEnabled + " " + this);
        //add by mengzhaoxue
        try {
            if ((mRingMediaPlayer == null || !mRingMediaPlayer.isPlaying()) && !isIncall) {
                Uri ringUri = RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE);
                Log.i(this, "ringUri:" + ringUri);
                mRingMediaPlayer = new MediaPlayer();
                mRingMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mRingMediaPlayer.setLooping(true);
                mRingMediaPlayer.setDataSource(getContext(), ringUri);
                mRingMediaPlayer.prepare();
                mRingMediaPlayer.start();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //end
        if (mPingEnabled && !mPingHandler.hasMessages(PING_MESSAGE_WHAT)) {
            ping();

            if (ENABLE_PING_AUTO_REPEAT) {
                mPingHandler.sendEmptyMessageDelayed(PING_MESSAGE_WHAT, PING_REPEAT_DELAY_MS);
            }
        }
    }

    @Override
    public void onGrabbed(View v, int handle) {
        Log.d(this, "onGrabbed()");
        stopPing();
    }

    @Override
    public void onReleased(View v, int handle) {
        Log.d(this, "onReleased()");
        if (mTargetTriggered) {
            mTargetTriggered = false;
        } else {
            startPing();
        }
    }

    @Override
    public void onTrigger(View v, int target) {
        Log.d(this, "onTrigger() view=" + v + " target=" + target);
        final int resId = getResourceIdForTarget(target);
        if (resId == R.drawable.ic_lockscreen_answer) {
            if (mRingMediaPlayer != null && mRingMediaPlayer.isPlaying()) {
                mRingMediaPlayer.stop();
                isIncall = true;
            }
            mAnswerFragment.onAnswer(VideoProfile.STATE_AUDIO_ONLY, getContext());
            mTargetTriggered = true;
        } else if (resId == R.drawable.ic_lockscreen_decline) {
            if (mRingMediaPlayer != null && mRingMediaPlayer.isPlaying()) {
                mRingMediaPlayer.stop();
            }
            mAnswerFragment.onDecline(getContext());
            mTargetTriggered = true;
            isIncall = false;
        } else if (resId == R.drawable.ic_lockscreen_text) {
//            mAnswerFragment.onText();
//            mTargetTriggered = true;
        } else if (resId == R.drawable.ic_videocam || resId == R.drawable.ic_lockscreen_answer_video) {
            mAnswerFragment.onAnswer(mVideoState, getContext());
            mTargetTriggered = true;
        } else if (resId == R.drawable.ic_lockscreen_decline_video) {
            mAnswerFragment.onDeclineUpgradeRequest(getContext());
            mTargetTriggered = true;
        } else {
            // Code should never reach here.
            Log.e(this, "Trigger detected on unhandled resource. Skipping.");
        }
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {

    }

    @Override
    public void onFinishFinalAnimation() {

    }

    public void setAnswerFragment(AnswerFragment fragment) {
        mAnswerFragment = fragment;
    }

    /**
     * Sets the video state represented by the "video" icon on the glow pad.
     *
     * @param videoState The new video state.
     */
    public void setVideoState(int videoState) {
        mVideoState = videoState;
    }

    public void stopRingAndRelease() {
        if (mRingMediaPlayer != null) {
            if (mRingMediaPlayer.isPlaying()) {
                mRingMediaPlayer.stop();
            }
            mRingMediaPlayer.release();
        }
    }

    public void stopRing() {
        if (mRingMediaPlayer != null) {
            if (mRingMediaPlayer.isPlaying()) {
                mRingMediaPlayer.stop();
            }
        }
    }

    public void startRing() {
        if (mRingMediaPlayer != null) {
            if (!mRingMediaPlayer.isPlaying()) {
                mRingMediaPlayer.start();
            }
        }
    }
}
