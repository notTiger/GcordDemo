package com.example.gcorddemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.gcorddemo.bean.ContactBean;
import com.example.gcorddemo.databinding.ActivityInCallBinding;
import com.example.gcorddemo.model.CallModel;
import com.example.gcorddemo.model.ContactModel;
import com.example.gcorddemo.viewmodel.InCallActivityViewModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import cn.com.geartech.gcordsdk.GcordSDK;
import cn.com.geartech.gcordsdk.HandleManager;
import cn.com.geartech.gcordsdk.PhoneAPI;
import cn.com.geartech.gcordsdk.UnifiedPhoneController;
import cn.com.geartech.gcordsdk.dataType.CallStatusItem;

public class InCallActivity extends BaseActivity implements HandleManager.HandleEventListener {
    public static final String EXTRA_KEY_CALL_TYPE = "callType";
    public static final String EXTRA_KEY_CALL_IN_OR_CALL_OUT = "callInOrCallOut";
    public static final String EXTRA_KEY_PHONE_NUMBER = "phoneNumber";
    public static final String EXTRA_KEY_CALL_STATUS = "callStatus";
    private UnifiedPhoneController.UnifiedPhoneEventListener unifiedPhoneEventListener;
    private @CallStatus
    int callStatus = CallStatus.CALL_STATUS_PENDING;
    private @CallInOrCallOut
    int callInOrCallOut = CallInOrCallOut.CALL_IN;
    private @CallType
    int callType = CallType.CALL_TYPE_PSTN;
    private UnifiedPhoneController unifiedPhoneController;
    private String phoneNumber;
    private PhoneAPI.PhoneEventListener phoneEventListener = null;
    private InCallActivityViewModel inCallActivityViewModel;
    private ActivityInCallBinding activityInCallBinding;
    private boolean callMuted = false;

    public static void startInCallActivity(@NonNull Context context,
                                           @Nullable String phoneNumber,
                                           @CallType int callType,
                                           @CallInOrCallOut int callInOrCallOut,
                                           @CallStatus int callStatus,
                                           OnInCallActivityStartedListener listener) {
        Runnable runnable = () -> {
            Intent intent = new Intent(context, InCallActivity.class);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.putExtra(EXTRA_KEY_CALL_TYPE, callType);
            intent.putExtra(EXTRA_KEY_CALL_IN_OR_CALL_OUT, callInOrCallOut);
            intent.putExtra(EXTRA_KEY_CALL_STATUS, callStatus);

            if (phoneNumber != null)
                intent.putExtra(EXTRA_KEY_PHONE_NUMBER, phoneNumber);

            context.startActivity(intent);
            if(listener != null)listener.onInCallActivityStarted();
        };
        if (callType != CallType.CALL_TYPE_SIM || callInOrCallOut == CallInOrCallOut.CALL_IN) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(runnable, 1000L);
        }
    }

    public static void startInCallActivity(@NonNull Context context,
                                           @Nullable String phoneNumber,
                                           @CallType int callType,
                                           @CallInOrCallOut int callInOrCallOut,
                                           @CallStatus int callStatus) {

        startInCallActivity(context, phoneNumber, callType, callInOrCallOut, callStatus, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        activityInCallBinding = ActivityInCallBinding.inflate(getLayoutInflater());
        setContentView(activityInCallBinding.getRoot());
        initVars();
        initViews();
    }

    private void initVars() {
        inCallActivityViewModel = new ViewModelProvider(this).get(InCallActivityViewModel.class);
        initCallRelatedVars();
        GcordSDK.getInstance().getHandleManager().addHandleEventListener(this);
        getVarsFromIntent();
        inCallActivityViewModel.getContactLiveData().observe(InCallActivity.this, this::onContactFound);
    }

    private void onContactFound(ContactBean contactBean) {
        if (contactBean != null) {
            String name = contactBean.getDisplayName();
            if (ContactModel.isStringNullOrEmpty(name)) {
                String familyName = contactBean.getFamilyName();
                if (ContactModel.isStringNullOrEmpty(familyName)) name = familyName;
                else name = "";
                String givenName = contactBean.getGivenName();
                if (!ContactModel.isStringNullOrEmpty(givenName)) name += givenName;
            }
            if (!ContactModel.isStringNullOrEmpty(name)) {
                activityInCallBinding.tvName.setText(name);
                GcordSDK.getInstance().getContactManager().notifyQueryContactDataResult(name, phoneNumber);
            }
        }
    }

    private void initCallRelatedVars() {
        unifiedPhoneController = GcordSDK.getInstance().getUnifiedPhoneController();
        unifiedPhoneEventListener = new UnifiedPhoneController.UnifiedPhoneEventListener() {
            @Override
            public void onIncomingCall() {

            }

            @Override
            public void onGetPhoneNumber(String s) {
                phoneNumber = s;
                if (phoneNumber != null) {
                    activityInCallBinding.tvName.setText(phoneNumber);
                    inCallActivityViewModel.findContactByPhoneNumber(phoneNumber);
                }
            }

            @Override
            public void onTicking(int seconds) {
                final int SECONDS_OF_AN_HOUR = 60 * 60;
                int hours = seconds / SECONDS_OF_AN_HOUR;
                seconds %= SECONDS_OF_AN_HOUR;
                final int SECONDS_OF_A_MINUTE = 60;
                int minutes = seconds / SECONDS_OF_A_MINUTE;
                seconds %= SECONDS_OF_A_MINUTE;
                String str;
                if (hours > 0) {
                    str = String.format(Locale.getDefault(), "%1$02d:%2$02d:%3$02d", hours, minutes, seconds);
                } else
                    str = String.format(Locale.getDefault(), "%1$02d:%2$02d", minutes, seconds);
                activityInCallBinding.tvTime.setText(str);
                if (callStatus == CallStatus.CALL_STATUS_PENDING) {
                    setupInCallViews();
                    callStatus = CallStatus.CALL_STATUS_IN_CALL;
                }
            }

            @Override
            public void onCallTerminated() {
                onCallEnd();
            }

            @Override
            public void onDialFinish(String s) {

            }

            @Override
            public void onBusyTone() {

            }

            @Override
            public void onResumeInCallStatus(CallStatusItem callStatusItem) {

            }
        };
        unifiedPhoneController.addPhoneEventListener(unifiedPhoneEventListener);

        if (callType == CallType.CALL_TYPE_PSTN) {
            phoneEventListener = new PhoneAPI.PhoneEventListener() {
                @Override
                public void onPickUp(PhoneAPI.PICKUP_STATE pickup_state) {

                }

                @Override
                public void onInComingCall() {

                }

                @Override
                public void onRingEnd() {

                }

                @Override
                public void onPhoneNumberReceived(String s) {

                }

                @Override
                public void onSwitchPhoneState(PhoneAPI.PICKUP_STATE pickup_state) {
                    boolean handsFree = (pickup_state == PhoneAPI.PICKUP_STATE.HANDS_FREE);
                    activityInCallBinding.btnHandsFree.setSelected(handsFree);
                }

                @Override
                public void onHangOff() {

                }
            };
            GcordSDK.getInstance().getPhoneAPI().addPhoneEventListener(phoneEventListener);
        }
    }

    private void onCallEnd() {
        if (unifiedPhoneController.isRecording()) {
            unifiedPhoneController.stopRecording();
        }
        finish();
    }

    private void initViews() {
        getWindow().getDecorView().setBackgroundResource(R.mipmap.in_call_bg);
        hideNavigationBar();
        if (phoneNumber != null) {
            activityInCallBinding.tvName.setText(phoneNumber);
            inCallActivityViewModel.findContactByPhoneNumber(phoneNumber);
        }
        if (callStatus == CallStatus.CALL_STATUS_PENDING) {
            if (callInOrCallOut == CallInOrCallOut.CALL_OUT) {
                setupOutGoingCallViews();
            } else {
                setupIncomingCallViews();
            }
        } else {
            setupInCallViews();
        }
        changeStatusBarToTransparent();
        initVolume();
    }

    private void initVolume() {
        unifiedPhoneController.getMaxVolume((channel, vol) ->
                runOnUiThread(() -> {
                    activityInCallBinding.volumeLayout.volumeLevel.setMax(vol);
                    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                            try {
                                if (unifiedPhoneController.getCurrentPhoneCall() != null) {
                                    unifiedPhoneController.setVolume(progress, (channel, vol) -> CallModel.debug("volume after set:" + vol));
                                }
                            } catch (Exception ignored) {

                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    };
                    activityInCallBinding.volumeLayout.volumeLevel.setOnSeekBarChangeListener(onSeekBarChangeListener);
                }));
    }

    private void setupIncomingCallViews() {
        activityInCallBinding.outGoingCallBtnGroup.setVisibility(View.GONE);
        activityInCallBinding.incomingCallBtnGroup.setVisibility(View.VISIBLE);
        activityInCallBinding.inCallBtnGroup.setVisibility(View.GONE);

        activityInCallBinding.tvTime.setText("");
    }

    private void setupOutGoingCallViews() {
        activityInCallBinding.outGoingCallBtnGroup.setVisibility(View.VISIBLE);
        activityInCallBinding.incomingCallBtnGroup.setVisibility(View.GONE);
        activityInCallBinding.inCallBtnGroup.setVisibility(View.GONE);

        activityInCallBinding.tvTime.setText(R.string.dialing);
    }

    private void setupInCallViews() {
        activityInCallBinding.outGoingCallBtnGroup.setVisibility(View.GONE);
        activityInCallBinding.incomingCallBtnGroup.setVisibility(View.GONE);
        activityInCallBinding.inCallBtnGroup.setVisibility(View.VISIBLE);
        setupMusicChannel();
        setupVolumeLayout();
        activityInCallBinding.btnMute.setSelected(callMuted);
    }

    private void setupMusicChannel() {
        HandleManager handleManager = GcordSDK.getInstance().getHandleManager();
        boolean handlePickedUp = handleManager.isHandlePickedUp();
        if (handlePickedUp) {
            unifiedPhoneController.switchToHandle();
        } else {
            unifiedPhoneController.switchToHandsFree();
        }
        activityInCallBinding.btnHandsFree.setSelected(!handlePickedUp);
    }

    private void setupVolumeLayout() {
        unifiedPhoneController.getVolume((channel, volume) -> runOnUiThread(() -> {
            activityInCallBinding.volumeLayout.volumeLevel.setProgress(volume);
            activityInCallBinding.volumeLayout.getRoot().setVisibility(View.VISIBLE);
        }));
    }

    private void getVarsFromIntent() {
        Intent intent = getIntent();
        callStatus = intent.getIntExtra(EXTRA_KEY_CALL_STATUS, CallStatus.CALL_STATUS_PENDING);
        callInOrCallOut = intent.getIntExtra(EXTRA_KEY_CALL_IN_OR_CALL_OUT, CallInOrCallOut.CALL_IN);
        callType = intent.getIntExtra(EXTRA_KEY_CALL_TYPE, CallType.CALL_TYPE_PSTN);
        phoneNumber = intent.getStringExtra(EXTRA_KEY_PHONE_NUMBER);
    }

    @Override
    protected void onDestroy() {
        unifiedPhoneController.removePhoneEventListener(unifiedPhoneEventListener);
        GcordSDK gcordsdk = GcordSDK.getInstance();
        if (callType == CallType.CALL_TYPE_PSTN) {
            gcordsdk.getPhoneAPI().removePhoneEventListener(phoneEventListener);
        }
        gcordsdk.getHandleManager().removeHandleEventListener(this);
        super.onDestroy();
    }

    @Override
    public void onHandlePickedUp() {
        if (callInOrCallOut == CallInOrCallOut.CALL_IN
                && callStatus == CallStatus.CALL_STATUS_PENDING
                && callType != CallType.CALL_TYPE_PSTN) {
            onAnswerCallClick(null);
        } else {
            if (callStatus == CallStatus.CALL_STATUS_IN_CALL) {
                if (callType == CallType.CALL_TYPE_PSTN) {
                    setupVolumeLayout();
                }
            }
        }
        activityInCallBinding.btnHandsFree.setSelected(false);
    }

    @Override
    public void onHandlePutDown() {
        boolean isHandsFree = activityInCallBinding.btnHandsFree.isSelected();
        if (!isHandsFree) {
            if (callStatus == CallStatus.CALL_STATUS_IN_CALL) {
                onEndCallClick(activityInCallBinding.answerIncomingCall);
            }
        }
    }

    @Override
    public void onHomeClicked() {
        if (callStatus == CallStatus.CALL_STATUS_PENDING) {
            if (callInOrCallOut == CallInOrCallOut.CALL_IN) {
                onAnswerCallClick(activityInCallBinding.answerIncomingCall);
            }
        } else {
            if (activityInCallBinding.btnHandsFree.isSelected()) {
                onEndCallClick(null);
            } else {
                onHandsFreeClick(activityInCallBinding.btnHandsFree);
            }
        }
    }

    public void onEndCallClick(View view) {
        unifiedPhoneController.endCall();
    }

    public void onAnswerCallClick(View view) {
        unifiedPhoneController.answer();
    }

    public void onKeyboardCallClick(View view) {
        ConstraintLayout dtmfLayout = activityInCallBinding.dtmfLayout.getRoot();
        if (dtmfLayout.getVisibility() == View.GONE) {
            dtmfLayout.setVisibility(View.VISIBLE);
            if (callType == CallType.CALL_TYPE_PSTN) {
                activityInCallBinding.dtmfLayout.tvFlash.setVisibility(View.VISIBLE);
                activityInCallBinding.dtmfLayout.verticalLine.setVisibility(View.VISIBLE);
            } else {
                activityInCallBinding.dtmfLayout.tvFlash.setVisibility(View.GONE);
                activityInCallBinding.dtmfLayout.verticalLine.setVisibility(View.GONE);
            }
        } else {
            dtmfLayout.setVisibility(View.GONE);
        }
        view.setSelected(!view.isSelected());
    }

    public void onRecordClick(View view) {
        if (!unifiedPhoneController.isRecording()) {
            unifiedPhoneController.startRecordingMp3(new UnifiedPhoneController.UnifiedPhoneRecordCallback() {
                @Override
                public void onRecordSuccess(String s) {
                    CallModel.debug("onRecordSuccess, record file path is " + s);
                }

                @Override
                public void onRecordFailed(int i, String s) {

                }
            });
        } else {
            unifiedPhoneController.stopRecording();
        }
        view.setSelected(unifiedPhoneController.isRecording());
    }

    public void onMuteClick(View view) {
        callMuted = !callMuted;
        unifiedPhoneController.muteCall(callMuted);
        view.setSelected(callMuted);
    }

    public void onHandsFreeClick(View view) {
        boolean selected = !view.isSelected();
        if (selected) {
            unifiedPhoneController.switchToHandsFree();
        } else {
            unifiedPhoneController.switchToHandle();
        }
        view.setSelected(selected);
        if (callType == CallType.CALL_TYPE_PSTN) {
            setupVolumeLayout();
        }
    }

    public void onDtmfClick(View view) {
        String tag = (String) view.getTag();
        if (ContactModel.isStringNullOrEmpty(tag)) return;
        if ("star".equals(tag)) {
            tag = "*";
        } else if ("pound".equals(tag)) {
            tag = "#";
        }
        GcordSDK.getInstance().getUnifiedPhoneController().dialDTMF(tag);
    }

    public void onFlashClick(View view) {
        GcordSDK.getInstance().getPhoneAPI().flash(() -> runOnUiThread(() -> Toast.makeText(InCallActivity.this, R.string.please_enter_flash_number, Toast.LENGTH_LONG).show()));
    }

    public void onDismissClick(View view) {
        activityInCallBinding.dtmfLayout.getRoot().setVisibility(View.GONE);
    }

    public interface OnInCallActivityStartedListener {
        void onInCallActivityStarted();
    }

    @SuppressWarnings("unused")
    @Retention(RetentionPolicy.SOURCE)
    public @interface CallType {
        int CALL_TYPE_PSTN = 0;
        int CALL_TYPE_SIP = 1;
        int CALL_TYPE_SIM = 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CallInOrCallOut {
        int CALL_IN = 0;
        int CALL_OUT = 1;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CallStatus {
        int CALL_STATUS_PENDING = 0;
        int CALL_STATUS_IN_CALL = 1;
    }
}