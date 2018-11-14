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
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telecom.CallAudioState;
import android.util.SparseIntArray;
import android.view.ContextThemeWrapper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.kinstalk.her.contactscommon.common.util.MaterialColorMapUtils.MaterialPalette;
import com.kinstalk.her.incallui.util.CallButtonPresenter;

import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_ADD_CALL;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_AUDIO;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_BLIND_ECT;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_COUNT;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_DIALPAD;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_DOWNGRADE_TO_AUDIO;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_HANGUP_ACTIVE_AND_ANSWER_WAITING;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_HANGUP_ALL_CALLS;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_HANGUP_ALL_HOLD_CALLS;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_HIDE_LOCAL_VIDEO;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_HOLD;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_MANAGE_VIDEO_CONFERENCE;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_MERGE;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_MUTE;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_PAUSE_VIDEO;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_SET_ECT;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_SWAP;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_SWITCH_CAMERA;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_SWITCH_VOICE_RECORD;
import static com.kinstalk.her.incallui.CallButtonFragment.Buttons.BUTTON_UPGRADE_TO_VIDEO;

/// M: add for plug in. @{
/// @}
/// M: add for phone record. @{
/// @}

/**
 * Fragment for call control buttons
 */
public class CallButtonFragment
        extends BaseFragment<CallButtonPresenter, CallButtonPresenter.CallButtonUi>
        implements CallButtonPresenter.CallButtonUi, OnMenuItemClickListener, OnDismissListener,
        View.OnClickListener {

    private static final int INVALID_INDEX = -1;
    private int mButtonMaxVisible;
    // The button is currently visible in the UI
    private static final int BUTTON_VISIBLE = 1;
    // The button is hidden in the UI
    private static final int BUTTON_HIDDEN = 2;
    // The button has been collapsed into the overflow menu
    private static final int BUTTON_MENU = 3;

    public interface Buttons {

        public static final int BUTTON_AUDIO = 0;
        public static final int BUTTON_MUTE = 1;
        public static final int BUTTON_DIALPAD = 2;
        public static final int BUTTON_HOLD = 3;
        public static final int BUTTON_SWAP = 4;
        public static final int BUTTON_UPGRADE_TO_VIDEO = 5;
        public static final int BUTTON_SWITCH_CAMERA = 6;
        public static final int BUTTON_DOWNGRADE_TO_AUDIO = 7;
        /// M: [Hide and Downgrade button] @{
        public static final int BUTTON_HIDE_LOCAL_VIDEO = 8;
        /// @}
        public static final int BUTTON_ADD_CALL = 9;
        public static final int BUTTON_MERGE = 10;
        public static final int BUTTON_PAUSE_VIDEO = 11;
        public static final int BUTTON_MANAGE_VIDEO_CONFERENCE = 12;
        /// M: [Voice Record]
        public static final int BUTTON_SWITCH_VOICE_RECORD = 13;
        /// M: add other feature. @{
        public static final int BUTTON_SET_ECT = 14;
        public static final int BUTTON_HANGUP_ALL_CALLS = 15;
        public static final int BUTTON_HANGUP_ALL_HOLD_CALLS = 16;
        public static final int BUTTON_HANGUP_ACTIVE_AND_ANSWER_WAITING = 17;
        /// @}
        /// M: add for blind ECT feature
        public static final int BUTTON_BLIND_ECT = 18;
        public static final int BUTTON_COUNT = 19;
    }

    private SparseIntArray mButtonVisibilityMap = new SparseIntArray(BUTTON_COUNT);

    private CompoundButton mAudioButton;
    private CompoundButton mMuteButton;
    private CompoundButton mShowDialpadButton;
    private CompoundButton mHoldButton;
    private ImageButton mSwapButton;
    private ImageButton mChangeToVideoButton;
    private ImageButton mChangeToVoiceButton;
    /// M: [Hide button] @{
    private CompoundButton mHideOrShowLocalVideoButton;
    /// @}
    private CompoundButton mSwitchCameraButton;
    private ImageButton mAddCallButton;
    private ImageButton mMergeButton;
    private CompoundButton mPauseVideoButton;
    private ImageButton mOverflowButton;
    private ImageButton mManageVideoCallConferenceButton;

    private PopupMenu mAudioModePopup;
    private boolean mAudioModePopupVisible;
    private PopupMenu mOverflowPopup;

    /// M: add for plug in. @{
//    private IRCSeCallButtonExt mRCSeExt;
    /// @}

    /// M: for call button feature. @{
    private ImageButton mSetEctButton;
    private ImageButton mHangupAllCallsButton;
    private ImageButton mHangupAllHoldCallsButton;
    private ImageButton mHangupActiveAndAnswerWaitingButton;
    /// M: [Voice Record]
    private CompoundButton mRecordVoiceButton;
    /// @}

    private int mPrevAudioMode = 0;

    // Constants for Drawable.setAlpha()
    private static final int HIDDEN = 0;
    private static final int VISIBLE = 255;

    private boolean mIsEnabled;
    private MaterialPalette mCurrentThemeColors;

    /// M: [Voice Record] @{
    private Context mContext;
    /// @}
    /// M: for blind ECT feature
    private ImageButton mBlindEctButton;

    @Override
    public CallButtonPresenter createPresenter() {
        // TODO: find a cleaner way to include audio mode provider than having a singleton instance.
        return new CallButtonPresenter();
    }

    @Override
    public CallButtonPresenter.CallButtonUi getUi() {
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 0; i < BUTTON_COUNT; i++) {
            mButtonVisibilityMap.put(i, BUTTON_HIDDEN);
        }

        mButtonMaxVisible = getResources().getInteger(R.integer.call_card_max_buttons);
        /// M: add for plug in. @{
//        mRCSeExt = ExtensionManager.getRCSeCallButtonExt();
        /// @}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.call_button_fragment, container, false);

        mAudioButton = (CompoundButton) parent.findViewById(R.id.audioButton);
        mAudioButton.setOnClickListener(this);
        mMuteButton = (CompoundButton) parent.findViewById(R.id.muteButton);
        mMuteButton.setOnClickListener(this);
        mShowDialpadButton = (CompoundButton) parent.findViewById(R.id.dialpadButton);
        mShowDialpadButton.setOnClickListener(this);
        mHoldButton = (CompoundButton) parent.findViewById(R.id.holdButton);
        mHoldButton.setOnClickListener(this);
        mSwapButton = (ImageButton) parent.findViewById(R.id.swapButton);
        mSwapButton.setOnClickListener(this);
        mChangeToVideoButton = (ImageButton) parent.findViewById(R.id.changeToVideoButton);
        mChangeToVideoButton.setOnClickListener(this);
        mChangeToVoiceButton = (ImageButton) parent.findViewById(R.id.changeToVoiceButton);
        mChangeToVoiceButton.setOnClickListener(this);
        /// M: [Hide button] @{
        mHideOrShowLocalVideoButton =
                (CompoundButton) parent.findViewById(R.id.hideOrShowLocalVideo);
        mHideOrShowLocalVideoButton.setOnClickListener(this);
        /// @}
        mSwitchCameraButton = (CompoundButton) parent.findViewById(R.id.switchCameraButton);
        mSwitchCameraButton.setOnClickListener(this);
        mAddCallButton = (ImageButton) parent.findViewById(R.id.addButton);
        mAddCallButton.setOnClickListener(this);
        mMergeButton = (ImageButton) parent.findViewById(R.id.mergeButton);
        mMergeButton.setOnClickListener(this);
        mPauseVideoButton = (CompoundButton) parent.findViewById(R.id.pauseVideoButton);
        mPauseVideoButton.setOnClickListener(this);
        mOverflowButton = (ImageButton) parent.findViewById(R.id.overflowButton);
        mOverflowButton.setOnClickListener(this);
        mManageVideoCallConferenceButton = (ImageButton) parent.findViewById(
            R.id.manageVideoCallConferenceButton);
        mManageVideoCallConferenceButton.setOnClickListener(this);

        /// M: for call button feature. @{
        mSetEctButton = (ImageButton) parent.findViewById(R.id.setEctButton);
        mSetEctButton.setOnClickListener(this);
        mHangupAllCallsButton = (ImageButton) parent.findViewById(R.id.hangupAllCallsButton);
        mHangupAllCallsButton.setOnClickListener(this);
        mHangupAllHoldCallsButton = (ImageButton) parent.findViewById(
            R.id.hangupAllHoldCallsButton);
        mHangupAllHoldCallsButton.setOnClickListener(this);
        mHangupActiveAndAnswerWaitingButton = (ImageButton) parent.findViewById(
            R.id.hangupActiveAndAnswerWaitingButton);
        mHangupActiveAndAnswerWaitingButton.setOnClickListener(this);
        /** [Voice Record] add recording button click listener @{ */
        mRecordVoiceButton = (CompoundButton) parent
                .findViewById(R.id.switch_voice_record);
        mRecordVoiceButton.setOnClickListener(this);
        /** @} */
        /// @}

        /// M: for blind ECT feature
        mBlindEctButton = (ImageButton) parent.findViewById(R.id.blindEctButton);
        mBlindEctButton.setOnClickListener(this);
        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set the buttons
        updateAudioButtons(getPresenter().getSupportedAudio());
        /// M: [Voice Record] @{
        mContext = getActivity();
        /// @}
//        ExtensionManager.getVilteAutoTestHelperExt().registerReceiverForUpgradeAndDowngrade(
//                mContext,getPresenter());
    }

    @Override
    public void onResume() {
        if (getPresenter() != null) {
            getPresenter().refreshMuteState();
        }
        super.onResume();

        updateColors();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d(this, "onClick(View " + view + ", id " + id + ")...");
        ///M: when current call is video call, click callbutton we should
        //disable VideoCallFullScreen.
//        InCallPresenter.getInstance().notifyDisableVideoCallFullScreen();
        if (id == R.id.audioButton) {
            onAudioButtonClicked();
        } else if (id == R.id.addButton) {
            getPresenter().addCallClicked();
        } else if (id == R.id.muteButton) {
            getPresenter().muteClicked(!mMuteButton.isSelected());
        } else if (id == R.id.mergeButton) {
            getPresenter().mergeClicked();
            mMergeButton.setEnabled(false);
        } else if (id == R.id.holdButton) {
            getPresenter().holdClicked(!mHoldButton.isSelected());
        } else if (id == R.id.swapButton) {
            getPresenter().swapClicked();
        } else if (id == R.id.dialpadButton) {
            getPresenter().showDialpadClicked(!mShowDialpadButton.isSelected());
        } else if (id == R.id.changeToVideoButton) {
            getPresenter().changeToVideoClicked();
        } else if (id == R.id.changeToVoiceButton) {
            getPresenter().changeToVoiceClicked();
        /// M: [Hide button] @{
        } else if (id == R.id.hideOrShowLocalVideo) {
            onHideVideoCallPreviewClick(!mHideOrShowLocalVideoButton.isSelected());
       /// @}
       } else if (id == R.id.switchCameraButton) {
            getPresenter().switchCameraClicked(
                    mSwitchCameraButton.isSelected() /* useFrontFacingCamera */);
        } else if (id == R.id.pauseVideoButton) {
            getPresenter().pauseVideoClicked(
                    !mPauseVideoButton.isSelected() /* pause */);
        } else if (id == R.id.overflowButton) {
            if (mOverflowPopup != null) {
                    /// M: For ALPS01961019, Rapid continuous click twice. @{
                    mOverflowPopup.dismiss();
                    /// @}
                    mOverflowPopup.show();
                }
        } else if (id == R.id.manageVideoCallConferenceButton) {
            onManageVideoCallConferenceClicked();
        /// M: for call button feature. @{
        } else if (id == R.id.setEctButton) {
            getPresenter().onEctMenuSelected();
        } else if (id == R.id.hangupAllCallsButton) {
            getPresenter().hangupAllClicked();
        } else if (id == R.id.hangupAllHoldCallsButton) {
            getPresenter().hangupAllHoldCallsClicked();
        } else if (id == R.id.hangupActiveAndAnswerWaitingButton) {
            getPresenter().hangupActiveAndAnswerWaitingClicked();
        /// M: for [Voice Record]
        } else if (id == R.id.switch_voice_record) {
            onVoiceRecordClick((CompoundButton)view);
        /// @}
        /// M: for blind ECT
        } else if(id == R.id.blindEctButton) {
            getPresenter().onBlindEctMenuSelected();
        } else {
            Log.wtf(this, "onClick: unexpected");
            return;
        }

        view.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    @Override
    public void updateColors() {
//        MaterialPalette themeColors = InCallPresenter.getInstance().getThemeColors();

//        if (mCurrentThemeColors != null && mCurrentThemeColors.equals(themeColors)) {
//            return;
//        }
//        if (themeColors == null) {
//            return;
//        }

        View[] compoundButtons = {
                mAudioButton,
                mMuteButton,
                mShowDialpadButton,
                mHoldButton,
                mSwitchCameraButton,
                mPauseVideoButton,

                /// M: for call button feature. @{
                mRecordVoiceButton
                /// @}
        };

        for (View button : compoundButtons) {
            final LayerDrawable layers = (LayerDrawable) button.getBackground();
//            final RippleDrawable btnCompoundDrawable = compoundBackgroundDrawable(themeColors);
//            layers.setDrawableByLayerId(R.id.compoundBackgroundItem, btnCompoundDrawable);
            /// M: for ALPS01945830 & ALPS01976712. redraw the buttons. @{
//            btnCompoundDrawable.setState(layers.getState());
            layers.invalidateSelf();
            /// @}
        }

        ImageButton[] normalButtons = {
                mSwapButton,
                mChangeToVideoButton,
                mChangeToVoiceButton,
                mAddCallButton,
                mMergeButton,
                mOverflowButton,

            /// M: for call button feature. @{
            mSetEctButton,
            mHangupAllCallsButton,
            mHangupAllHoldCallsButton,
            mHangupActiveAndAnswerWaitingButton
            /// @}
        };

        for (ImageButton button : normalButtons) {
            final LayerDrawable layers = (LayerDrawable) button.getBackground();
//            final RippleDrawable btnDrawable = backgroundDrawable(themeColors);
//            layers.setDrawableByLayerId(R.id.backgroundItem, btnDrawable);
            /// M: for ALPS01945830 & ALPS01976712. redraw the buttons. @{
//            btnDrawable.setState(layers.getState());
            layers.invalidateSelf();
            /// @}
        }

        /// M: add for plug in. @{
//        mRCSeExt.updateNormalBgDrawable(backgroundDrawable(themeColors));
        /// @}
//        mCurrentThemeColors = themeColors;
    }

    /**
     * Generate a RippleDrawable which will be the background for a compound button, i.e.
     * a button with pressed and unpressed states. The unpressed state will be the same color
     * as the rest of the call card, the pressed state will be the dark version of that color.
     */
    private RippleDrawable compoundBackgroundDrawable(MaterialPalette palette) {
        Resources res = getResources();
        ColorStateList rippleColor =
                ColorStateList.valueOf(res.getColor(R.color.incall_accent_color));

        StateListDrawable stateListDrawable = new StateListDrawable();
        addSelectedAndFocused(res, stateListDrawable);
        addFocused(res, stateListDrawable);
        addSelected(res, stateListDrawable, palette);
        addUnselected(res, stateListDrawable, palette);

        return new RippleDrawable(rippleColor, stateListDrawable, null);
    }

    /**
     * Generate a RippleDrawable which will be the background of a button to ensure it
     * is the same color as the rest of the call card.
     */
    private RippleDrawable backgroundDrawable(MaterialPalette palette) {
        Resources res = getResources();
        ColorStateList rippleColor =
                ColorStateList.valueOf(res.getColor(R.color.incall_accent_color));

        StateListDrawable stateListDrawable = new StateListDrawable();
        addFocused(res, stateListDrawable);
        addUnselected(res, stateListDrawable, palette);

        return new RippleDrawable(rippleColor, stateListDrawable, null);
    }

    // state_selected and state_focused
    private void addSelectedAndFocused(Resources res, StateListDrawable drawable) {
        int[] selectedAndFocused = {android.R.attr.state_selected, android.R.attr.state_focused};
        Drawable selectedAndFocusedDrawable = res.getDrawable(R.drawable.btn_selected_focused);
        drawable.addState(selectedAndFocused, selectedAndFocusedDrawable);
    }

    // state_focused
    private void addFocused(Resources res, StateListDrawable drawable) {
        int[] focused = {android.R.attr.state_focused};
        Drawable focusedDrawable = res.getDrawable(R.drawable.btn_unselected_focused);
        drawable.addState(focused, focusedDrawable);
    }

    // state_selected
    private void addSelected(Resources res, StateListDrawable drawable, MaterialPalette palette) {
        int[] selected = {android.R.attr.state_selected};
        LayerDrawable selectedDrawable = (LayerDrawable) res.getDrawable(R.drawable.btn_selected);
        ((GradientDrawable) selectedDrawable.getDrawable(0)).setColor(palette.mSecondaryColor);
        drawable.addState(selected, selectedDrawable);
    }

    // default
    private void addUnselected(Resources res, StateListDrawable drawable, MaterialPalette palette) {
        LayerDrawable unselectedDrawable =
                (LayerDrawable) res.getDrawable(R.drawable.btn_unselected);
        ((GradientDrawable) unselectedDrawable.getDrawable(0)).setColor(palette.mPrimaryColor);
        drawable.addState(new int[0], unselectedDrawable);
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        mIsEnabled = isEnabled;

        mAudioButton.setEnabled(isEnabled);
        mMuteButton.setEnabled(isEnabled);
        mShowDialpadButton.setEnabled(isEnabled);
        mHoldButton.setEnabled(isEnabled);
        mSwapButton.setEnabled(isEnabled);
        mChangeToVideoButton.setEnabled(isEnabled);
        mChangeToVoiceButton.setEnabled(isEnabled);
        /// M: [Hide button] @{
        mHideOrShowLocalVideoButton.setEnabled(isEnabled);
        /// @}
        mSwitchCameraButton.setEnabled(isEnabled);
        mAddCallButton.setEnabled(isEnabled);
        mMergeButton.setEnabled(isEnabled);
        mPauseVideoButton.setEnabled(isEnabled);
        mOverflowButton.setEnabled(isEnabled);
        mManageVideoCallConferenceButton.setEnabled(isEnabled);

        /// M: for call button feature. @{
        mSetEctButton.setEnabled(isEnabled);
        mHangupAllCallsButton.setEnabled(isEnabled);
        mHangupAllHoldCallsButton.setEnabled(isEnabled);
        mHangupActiveAndAnswerWaitingButton.setEnabled(isEnabled);
        //[Voice Record]
        mRecordVoiceButton.setEnabled(isEnabled);
        /// @}
    }

    @Override
    public void showButton(int buttonId, boolean show) {
        mButtonVisibilityMap.put(buttonId, show ? BUTTON_VISIBLE : BUTTON_HIDDEN);
    }

    @Override
    public void enableButton(int buttonId, boolean enable) {
        final View button = getButtonById(buttonId);
        if (button != null) {
            button.setEnabled(enable);
        }
    }

    private View getButtonById(int id) {
        if (id == BUTTON_AUDIO) {
            return mAudioButton;
        } else if (id == BUTTON_MUTE) {
            return mMuteButton;
        } else if (id == BUTTON_DIALPAD) {
            return mShowDialpadButton;
        } else if (id == BUTTON_HOLD) {
            return mHoldButton;
        } else if (id == BUTTON_SWAP) {
            return mSwapButton;
        } else if (id == BUTTON_UPGRADE_TO_VIDEO) {
            return mChangeToVideoButton;
        } else if (id == BUTTON_DOWNGRADE_TO_AUDIO) {
            return mChangeToVoiceButton;
        /// M: [Hide button] @{
        } else if (id == BUTTON_HIDE_LOCAL_VIDEO) {
            return mHideOrShowLocalVideoButton;
        /// @}
        } else if (id == BUTTON_SWITCH_CAMERA) {
            return mSwitchCameraButton;
        } else if (id == BUTTON_ADD_CALL) {
            return mAddCallButton;
        } else if (id == BUTTON_MERGE) {
            return mMergeButton;
        } else if (id == BUTTON_PAUSE_VIDEO) {
            return mPauseVideoButton;
        } else if (id == BUTTON_MANAGE_VIDEO_CONFERENCE) {
            return mManageVideoCallConferenceButton;

        /// M: for call button feature. @{
        } else if (id == BUTTON_SET_ECT) {
            return mSetEctButton;
        } else if (id == BUTTON_HANGUP_ALL_CALLS) {
            return mHangupAllCallsButton;
        } else if (id == BUTTON_HANGUP_ALL_HOLD_CALLS) {
            return mHangupAllHoldCallsButton;
        } else if (id == BUTTON_HANGUP_ACTIVE_AND_ANSWER_WAITING) {
            return mHangupActiveAndAnswerWaitingButton;
        } else if (id == BUTTON_SWITCH_VOICE_RECORD) {
            return mRecordVoiceButton;
        /// @}

        /// M: for blind ECT
        } else if(id == BUTTON_BLIND_ECT) {
            return mBlindEctButton;
        } else {
            Log.w(this, "Invalid button id");
            return null;
        }
    }

    @Override
    public void setHold(boolean value) {
        if (mHoldButton.isSelected() != value) {
            mHoldButton.setSelected(value);
            mHoldButton.setContentDescription(getContext().getString(
                    value ? R.string.onscreenHoldText_selected
                            : R.string.onscreenHoldText_unselected));
        }
    }

    @Override
    public void setCameraSwitched(boolean isBackFacingCamera) {
        mSwitchCameraButton.setSelected(isBackFacingCamera);
    }

    @Override
    public void setVideoPaused(boolean isPaused) {
        mPauseVideoButton.setSelected(isPaused);
        ///M : @{
        String titleName = getResources().getString(R.string.onscreenPauseVideoText);
                /*isPaused ? getResources().getString(R.string.restartVideoPreview)
                : getResources().getString(R.string.onscreenPauseVideoText);*/
        mPauseVideoButton.setContentDescription(titleName);
        updatePopMenuItemTitle(BUTTON_PAUSE_VIDEO, titleName);
        ///  @}
    }

    @Override
    public void setMute(boolean value) {
        if (mMuteButton.isSelected() != value) {
            mMuteButton.setSelected(value);
            mMuteButton.setContentDescription(getContext().getString(
                    value ? R.string.onscreenMuteText_selected
                            : R.string.onscreenMuteText_unselected));
        }
    }

    private void addToOverflowMenu(int id, View button, PopupMenu menu) {
        button.setVisibility(View.GONE);
        menu.getMenu().add(Menu.NONE, id, Menu.NONE, button.getContentDescription());
        mButtonVisibilityMap.put(id, BUTTON_MENU);
    }

    private PopupMenu getPopupMenu() {
        return new PopupMenu(new ContextThemeWrapper(getActivity(), R.style.InCallPopupMenuStyle),
                mOverflowButton);
    }

    /**
     * Iterates through the list of buttons and toggles their visibility depending on the
     * setting configured by the CallButtonPresenter. If there are more visible buttons than
     * the allowed maximum, the excess buttons are collapsed into a single overflow menu.
     */
    @Override
    public void updateButtonStates() {
        /**
         *  M: Reset popup before update button state, make sure the dirty popup should be closed.
         *  To avoid an dead popup in UI. @{
         */
        if (mOverflowPopup != null) {
            mOverflowPopup.dismiss();
            Log.d(this,
                    "updateButtonStates(), dissmiss mOverflowPopup before start a new one..."
                    + " older: " + mOverflowPopup);
        }
        if (mAudioModePopup != null) {
            mAudioModePopup.dismiss();
            Log.d(this,
                    "updateButtonStates(), dissmiss mAudioModePopup before start a new one..."
                    + " older: " + mAudioModePopup);
        }
        /** @} */
        View prevVisibleButton = null;
        int prevVisibleId = -1;
        PopupMenu menu = null;
        int visibleCount = 0;
        for (int i = 0; i < BUTTON_COUNT; i++) {
            final int visibility = mButtonVisibilityMap.get(i);
            final View button = getButtonById(i);
            if (visibility == BUTTON_VISIBLE) {
                visibleCount++;
                if (visibleCount <= mButtonMaxVisible) {
                    button.setVisibility(View.VISIBLE);
                    prevVisibleButton = button;
                    prevVisibleId = i;
                } else {
                    if (menu == null) {
                        menu = getPopupMenu();
                    }
                    // Collapse the current button into the overflow menu. If is the first visible
                    // button that exceeds the threshold, also collapse the previous visible button
                    // so that the total number of visible buttons will never exceed the threshold.
                    if (prevVisibleButton != null) {
                        addToOverflowMenu(prevVisibleId, prevVisibleButton, menu);
                        prevVisibleButton = null;
                        prevVisibleId = -1;
                    }
                    addToOverflowMenu(i, button, menu);
                }
            } else if (visibility == BUTTON_HIDDEN) {
                button.setVisibility(View.GONE);
            }
        }

        mOverflowButton.setVisibility(menu != null ? View.VISIBLE : View.GONE);
        if (menu != null) {
            mOverflowPopup = menu;
            mOverflowPopup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    getButtonById(id).performClick();
                    return true;
                }
            });
            /// M:[RCS] plugin API @{
//            mRCSeExt.configureOverflowMenu(mContext, mOverflowPopup.getMenu());
          /// @}
        }
    }

    @Override
    public void setAudio(int mode) {
        updateAudioButtons(getPresenter().getSupportedAudio());
        /// M: For ALPS01825524 @{
        // Telecomm will trigger AudioMode popup refresh when supported Audio
        // has been changed. Here we only update Audio Button.
        // Original Code:
        // refreshAudioModePopup();
        /// @}

        if (mPrevAudioMode != mode) {
            updateAudioButtonContentDescription(mode);
            mPrevAudioMode = mode;
        }
    }

    @Override
    public void setSupportedAudio(int modeMask) {
        updateAudioButtons(modeMask);
        refreshAudioModePopup();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Log.d(this, "- onMenuItemClick: " + item);
        Log.d(this, "  id: " + item.getItemId());
        Log.d(this, "  title: '" + item.getTitle() + "'");

        // add for plug in. @{
//        if (mRCSeExt.handleMenuItemClick(item)) {
//            return true;
//        }
        // add for plug in. @}
        int mode = CallAudioState.ROUTE_WIRED_OR_EARPIECE;
        int resId = item.getItemId();

//        if (resId == R.id.audio_mode_speaker) {
//            mode = CallAudioState.ROUTE_SPEAKER;
//        } else if (resId == R.id.audio_mode_earpiece || resId == R.id.audio_mode_wired_headset) {
//            // InCallCallAudioState.ROUTE_EARPIECE means either the handset earpiece,
//            // or the wired headset (if connected.)
//            mode = CallAudioState.ROUTE_WIRED_OR_EARPIECE;
//        } else if (resId == R.id.audio_mode_bluetooth) {
//            mode = CallAudioState.ROUTE_BLUETOOTH;
//        } else {
//            Log.e(this, "onMenuItemClick:  unexpected View ID " + item.getItemId()
//                    + " (MenuItem = '" + item + "')");
//        }

        getPresenter().setAudioMode(mode);

        return true;
    }

    // PopupMenu.OnDismissListener implementation; see showAudioModePopup().
    // This gets called when the PopupMenu gets dismissed for *any* reason, like
    // the user tapping outside its bounds, or pressing Back, or selecting one
    // of the menu items.
    @Override
    public void onDismiss(PopupMenu menu) {
        Log.d(this, "- onDismiss: " + menu);
        mAudioModePopupVisible = false;
        updateAudioButtons(getPresenter().getSupportedAudio());
    }

    /**
     * Checks for supporting modes.  If bluetooth is supported, it uses the audio
     * pop up menu.  Otherwise, it toggles the speakerphone.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onAudioButtonClicked() {
        Log.d(this, "onAudioButtonClicked: " +
                CallAudioState.audioRouteToString(getPresenter().getSupportedAudio()));

        if (isSupported(CallAudioState.ROUTE_BLUETOOTH)) {
            showAudioModePopup();
        } else {
            getPresenter().toggleSpeakerphone();
        }
    }

    private void onManageVideoCallConferenceClicked() {
        Log.d(this, "onManageVideoCallConferenceClicked");
//        InCallPresenter.getInstance().showConferenceCallManager(true);
    }

    /**
     * Refreshes the "Audio mode" popup if it's visible.  This is useful
     * (for example) when a wired headset is plugged or unplugged,
     * since we need to switch back and forth between the "earpiece"
     * and "wired headset" items.
     *
     * This is safe to call even if the popup is already dismissed, or even if
     * you never called showAudioModePopup() in the first place.
     */
    public void refreshAudioModePopup() {
        if (mAudioModePopup != null && mAudioModePopupVisible) {
            // Dismiss the previous one
            mAudioModePopup.dismiss();  // safe even if already dismissed
            // And bring up a fresh PopupMenu
            showAudioModePopup();
        }
    }

    /**
     * Updates the audio button so that the appriopriate visual layers
     * are visible based on the supported audio formats.
     */
    private void updateAudioButtons(int supportedModes) {
        final boolean bluetoothSupported = isSupported(CallAudioState.ROUTE_BLUETOOTH);
        final boolean speakerSupported = isSupported(CallAudioState.ROUTE_SPEAKER);

        boolean audioButtonEnabled = false;
        boolean audioButtonChecked = false;
        boolean showMoreIndicator = false;

        boolean showBluetoothIcon = false;
        boolean showSpeakerphoneIcon = false;
        boolean showHandsetIcon = false;

        boolean showToggleIndicator = false;

        if (bluetoothSupported) {
            Log.d(this, "updateAudioButtons - popup menu mode");

            audioButtonEnabled = true;
            audioButtonChecked = true;
            showMoreIndicator = true;

            // Update desired layers:
            if (isAudio(CallAudioState.ROUTE_BLUETOOTH)) {
                showBluetoothIcon = true;
            } else if (isAudio(CallAudioState.ROUTE_SPEAKER)) {
                showSpeakerphoneIcon = true;
            } else {
                showHandsetIcon = true;
                // TODO: if a wired headset is plugged in, that takes precedence
                // over the handset earpiece.  If so, maybe we should show some
                // sort of "wired headset" icon here instead of the "handset
                // earpiece" icon.  (Still need an asset for that, though.)
            }

            // The audio button is NOT a toggle in this state, so set selected to false.
            mAudioButton.setSelected(false);
        } else if (speakerSupported) {
            Log.d(this, "updateAudioButtons - speaker toggle mode");

            audioButtonEnabled = true;

            // The audio button *is* a toggle in this state, and indicated the
            // current state of the speakerphone.
            audioButtonChecked = isAudio(CallAudioState.ROUTE_SPEAKER);
            mAudioButton.setSelected(audioButtonChecked);

            // update desired layers:
            showToggleIndicator = true;
            showSpeakerphoneIcon = true;
        } else {
            Log.d(this, "updateAudioButtons - disabled...");

            // The audio button is a toggle in this state, but that's mostly
            // irrelevant since it's always disabled and unchecked.
            audioButtonEnabled = false;
            audioButtonChecked = false;
            mAudioButton.setSelected(false);

            // update desired layers:
            showToggleIndicator = true;
            showSpeakerphoneIcon = true;
        }

        // Finally, update it all!
        /** M: log reduce, AOSP Logs
        Log.v(this, "audioButtonEnabled: " + audioButtonEnabled);
        Log.v(this, "audioButtonChecked: " + audioButtonChecked);
        Log.v(this, "showMoreIndicator: " + showMoreIndicator);
        Log.v(this, "showBluetoothIcon: " + showBluetoothIcon);
        Log.v(this, "showSpeakerphoneIcon: " + showSpeakerphoneIcon);
        Log.v(this, "showHandsetIcon: " + showHandsetIcon);
        @{ */
        Log.v(this, "audioButton[" + audioButtonEnabled + "/"
                + audioButtonChecked + "], More:" + showMoreIndicator
                + ", BtIcon:" + showBluetoothIcon + ", Speaker:"
                + showSpeakerphoneIcon + ", Handset:" + showHandsetIcon);
        /** @} */

        // Only enable the audio button if the fragment is enabled.
        mAudioButton.setEnabled(audioButtonEnabled && mIsEnabled);
        mAudioButton.setChecked(audioButtonChecked);

        final LayerDrawable layers = (LayerDrawable) mAudioButton.getBackground();
        Log.d(this, "'layers' drawable: " + layers);

        layers.findDrawableByLayerId(R.id.compoundBackgroundItem)
                .setAlpha(showToggleIndicator ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.moreIndicatorItem)
                .setAlpha(showMoreIndicator ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.bluetoothItem)
                .setAlpha(showBluetoothIcon ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.handsetItem)
                .setAlpha(showHandsetIcon ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.speakerphoneItem)
                .setAlpha(showSpeakerphoneIcon ? VISIBLE : HIDDEN);

    }

    /**
     * Update the content description of the audio button.
     */
    private void updateAudioButtonContentDescription(int mode) {
        int stringId = 0;

        // If bluetooth is not supported, the audio buttion will toggle, so use the label "speaker".
        // Otherwise, use the label of the currently selected audio mode.
        if (!isSupported(CallAudioState.ROUTE_BLUETOOTH)) {
            stringId = R.string.audio_mode_speaker;
        } else {
            switch (mode) {
                case CallAudioState.ROUTE_EARPIECE:
                    stringId = R.string.audio_mode_earpiece;
                    break;
                case CallAudioState.ROUTE_BLUETOOTH:
                    stringId = R.string.audio_mode_bluetooth;
                    break;
                case CallAudioState.ROUTE_WIRED_HEADSET:
                    stringId = R.string.audio_mode_wired_headset;
                    break;
                case CallAudioState.ROUTE_SPEAKER:
                    stringId = R.string.audio_mode_speaker;
                    break;
            }
        }

        if (stringId != 0) {
            mAudioButton.setContentDescription(getResources().getString(stringId));
        }
    }

    private void showAudioModePopup() {
        Log.d(this, "showAudioPopup()...");

        final ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(),
                R.style.InCallPopupMenuStyle);
        mAudioModePopup = new PopupMenu(contextWrapper, mAudioButton /* anchorView */);
        mAudioModePopup.getMenuInflater().inflate(R.menu.incall_audio_mode_menu,
                mAudioModePopup.getMenu());
        mAudioModePopup.setOnMenuItemClickListener(this);
        mAudioModePopup.setOnDismissListener(this);

        final Menu menu = mAudioModePopup.getMenu();

        // TODO: Still need to have the "currently active" audio mode come
        // up pre-selected (or focused?) with a blue highlight.  Still
        // need exact visual design, and possibly framework support for this.
        // See comments below for the exact logic.

        final MenuItem speakerItem = menu.findItem(R.id.audio_mode_speaker);
        speakerItem.setEnabled(isSupported(CallAudioState.ROUTE_SPEAKER));
        // TODO: Show speakerItem as initially "selected" if
        // speaker is on.

        // We display *either* "earpiece" or "wired headset", never both,
        // depending on whether a wired headset is physically plugged in.
        final MenuItem earpieceItem = menu.findItem(R.id.audio_mode_earpiece);
        final MenuItem wiredHeadsetItem = menu.findItem(R.id.audio_mode_wired_headset);

        final boolean usingHeadset = isSupported(CallAudioState.ROUTE_WIRED_HEADSET);
        earpieceItem.setVisible(!usingHeadset);
        earpieceItem.setEnabled(!usingHeadset);
        wiredHeadsetItem.setVisible(usingHeadset);
        wiredHeadsetItem.setEnabled(usingHeadset);
        // TODO: Show the above item (either earpieceItem or wiredHeadsetItem)
        // as initially "selected" if speakerOn and
        // bluetoothIndicatorOn are both false.

        final MenuItem bluetoothItem = menu.findItem(R.id.audio_mode_bluetooth);
        bluetoothItem.setEnabled(isSupported(CallAudioState.ROUTE_BLUETOOTH));
        // TODO: Show bluetoothItem as initially "selected" if
        // bluetoothIndicatorOn is true.

        mAudioModePopup.show();

        // Unfortunately we need to manually keep track of the popup menu's
        // visiblity, since PopupMenu doesn't have an isShowing() method like
        // Dialogs do.
        mAudioModePopupVisible = true;
    }

    private boolean isSupported(int mode) {
        return (mode == (getPresenter().getSupportedAudio() & mode));
    }

    private boolean isAudio(int mode) {
        return (mode == getPresenter().getAudioMode());
    }

    @Override
    public void displayDialpad(boolean value, boolean animate) {
        if (getActivity() != null && getActivity() instanceof InCallActivity) {
            boolean changed = ((InCallActivity) getActivity()).showDialpadFragment(value, animate);
            if (changed) {
                mShowDialpadButton.setSelected(value);
                mShowDialpadButton.setContentDescription(getContext().getString(
                        value /* show */ ? R.string.onscreenShowDialpadText_unselected
                                : R.string.onscreenShowDialpadText_selected));
            }
            //M: fix ALPS02526332, when displayDialpad should also update dialpad request.
            // 1 = DIALPAD_REQUEST_NONE, 2 = DIALPAD_REQUEST_SHOW;
            ((InCallActivity) getActivity()).setShowDialpadRequested(value ? 2 : 1);
        }
    }

    @Override
    public boolean isDialpadVisible() {
        if (getActivity() != null && getActivity() instanceof InCallActivity) {
            return ((InCallActivity) getActivity()).isDialpadVisible();
        }
        return false;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // ---------------------------------------Mediatek-------------------------------------

    /// M: for plugin.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        /// M: Add for plugin.
//        mRCSeExt.onViewCreated(getActivity(), view);
    }

    /** M: [Voice Record] @{ */
    private void onVoiceRecordClick(CompoundButton bt) {
        String desc = bt.getContentDescription().toString();
        Log.d(this, "onVoiceRecordClick " + desc);
        if (desc == null) {
            return;
        }
//        if (!PhoneRecorderUtils.isExternalStorageMounted(mContext)) {
//            Toast.makeText(
//                    mContext,
//                    mContext.getResources().getString(
//                            R.string.error_sdcard_access), Toast.LENGTH_LONG)
//                    .show();
//            return;
//        }
//        if (!PhoneRecorderUtils
//                .diskSpaceAvailable(PhoneRecorderUtils.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
//            InCallPresenter.getInstance().handleStorageFull(true); // true for
//                                                                   // checking
//                                                                   // case
//            return;
//        }
//
//        if (desc.equals(getString(R.string.start_record))) {
//            getPresenter().voiceRecordClicked();
//        } else if (desc.equals(getString(R.string.stop_record))) {
//            getPresenter().stopRecordClicked();
//        }
    }

    /**
     * M: configure recording button.
     */
    @Override
    public void configRecordingButton() {
//        boolean isRecording = InCallPresenter.getInstance().isRecording();
//        //update for tablet and CT require.
//        mRecordVoiceButton.setSelected(isRecording);
//
//        mRecordVoiceButton
//                .setContentDescription(getString(isRecording ? R.string.stop_record
//                        : R.string.start_record));
//
//        if (mOverflowPopup == null) {
//            return;
//        }
//        String recordTitle = isRecording ? getString(R.string.stop_record)
//                : getString(R.string.start_record);
//        updatePopMenuItemTitle(BUTTON_SWITCH_VOICE_RECORD, recordTitle);

    }
    /** @} */

    /// M: fix CR:ALPS02259658,"hang up active,answer waiting call"not display in 1A+1W @{
    public void enableOverflowButton() {
        if (mOverflowPopup == null) {
            return;
        }
        final Menu menu = mOverflowPopup.getMenu();
        mOverflowButton.setEnabled(menu.hasVisibleItems());
    }
    /// @}

    /**
     * M: updatePopMenuItemTitle according to the param title
     * @param itemId related with the button which you want to operate.
     * @param title  popmenu will show
     */
    public void updatePopMenuItemTitle(int itemId, String title) {
        if (mOverflowPopup == null) {
            Log.d(this, "updatePopMenuItemTitle mOverflowPopup is null ");
            return;
        }
        final Menu menu = mOverflowPopup.getMenu();
        if (menu != null) {
            final MenuItem findMenu = menu.findItem(itemId);
            if (findMenu == null) {
                return;
            }
            findMenu.setTitle(title);
        }
    }

    /// M: click event for hide local preview
    private void onHideVideoCallPreviewClick(boolean hide) {
//        Log.d(this, "onHideVideoCallPreviewClick hide: " + hide);
//        InCallPresenter.getInstance().notifyHideLocalVideoChanged(hide);
//        updateHideButtonStatus(hide);

    }

    public void updateHideButtonStatus(boolean hide) {
//        mHideOrShowLocalVideoButton.setSelected(hide);
//        String title = hide ?
//                getResources().getString(R.string.showVideoPreview)
//                : getResources().getString(R.string.hideVideoPreview);
//        mHideOrShowLocalVideoButton.setContentDescription(title);
//        updatePopMenuItemTitle(BUTTON_HIDE_LOCAL_VIDEO, title);
    }
}
