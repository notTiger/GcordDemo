package com.example.gcorddemo.model;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gcorddemo.BuildConfig;
import com.example.gcorddemo.InCallActivity;
import com.example.gcorddemo.application.DemoApplication;

import java.lang.ref.WeakReference;

import cn.com.geartech.gcordsdk.GcordSDK;
import cn.com.geartech.gcordsdk.UnifiedPhoneController;
import cn.com.geartech.gcordsdk.dataType.CallStatusItem;
import cn.com.geartech.gcordsdk.dataType.UnifiedPhoneCall;

public enum CallModel {
    instance;

    public static final String TAG = "Debug";
    public static final boolean DEBUG = BuildConfig.DEBUG;
    private UnifiedPhoneController unifiedPhoneController;
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
    private WeakReference<Activity> currentResumedActivity = null;
    private int activityCount = 0;

    public static void debug(String msg) {
        if (DEBUG) {
            if (null == msg)
                msg = "null";
            Log.d(TAG, msg);
        }
    }

    public void init() {
        initUnifiedPhoneEventListener();
        initActivityLifeCycle();
    }

    private void initUnifiedPhoneEventListener() {
        unifiedPhoneController = GcordSDK.getInstance().getUnifiedPhoneController();
        UnifiedPhoneController.UnifiedPhoneEventListener unifiedPhoneEventListener = new UnifiedPhoneController.UnifiedPhoneEventListener() {

            @Override
            public void onIncomingCall() {
                debug("On Incoming Call");
                UnifiedPhoneCall currentCall = unifiedPhoneController.getCurrentPhoneCall();

                String phoneNumber = currentCall.getOpponentPhoneNumber();
                InCallActivity.startInCallActivity(getCurrentResumedActivityOrApplication(),
                        phoneNumber, currentCall.getCurrentCallType().ordinal(),
                        InCallActivity.CallInOrCallOut.CALL_IN,
                        InCallActivity.CallStatus.CALL_STATUS_PENDING);
            }

            /**
             * 收到来电号码的事件
             * @param number 电话号码
             * 如果话机没有开通来显，或者由于其他原因收不到电话号码，这个回调不会被调用
             */
            @Override
            public void onGetPhoneNumber(String number) {
                debug("On Get PhoneNumber:" + number);
            }

            /**
             * 通话开始计时
             * @param seconds
             * 对于来电: 从接听的一刻开始计时
             * 对于去电: pstn没法得知对方是否接听电话，所以pstn的去电都是在拨号完成后大约4秒开始计时
             * cell call的话，电信卡是一拨出去就开始计时，联通跟移动卡则是在对方接听的时候开始计时。
             * sip要看具体交换机的实现，一般来说sip外线接的都是固话线路，所以也是不准确的。
             *
             * 可以在onTicking(0) 里面执行类似开始通话录音之类的操作。
             * 如果开发者自行播放来电铃声，需要在此关闭铃声
             */
            @Override
            public void onTicking(int seconds) {

            }

            /**
             * 通话结束。
             * 以下几个时机会触发通话结束:
             * 1) 来电无人接听
             * 2) 通话后主动挂断
             * 3) 在忙音检测生效的场合，通话被动挂断
             *
             * 如果开发者自行播放来电铃声，需要在此关闭铃声
             */
            @Override
            public void onCallTerminated() {
//            hasCall = false;
                debug("onCallTerminated");


            }

            /**
             * 拨号完成
             * @param number
             *
             * 如果需要自定义呼出界面，在此处弹出。
             */
            @Override
            public void onDialFinish(String number) {

                debug("onDialFinish:" + number);

            }

            @Override
            public void onBusyTone() {
                unifiedPhoneController.endCall();
            }

            @Override
            public void onResumeInCallStatus(CallStatusItem item) {

            }

        };
        UnifiedPhoneController.getInstance().addPhoneEventListener(unifiedPhoneEventListener);
    }

    private void initActivityLifeCycle() {
        activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                activityCount++;
                currentResumedActivity = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                activityCount--;
                if (activityCount <= 0) {
                    currentResumedActivity = null;
                    activityCount = 0;
                }
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @Nullable Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        };
        DemoApplication.getApplication().registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }


    public @Nullable
    Activity getCurrentResumedActivity() {
        return currentResumedActivity != null ? currentResumedActivity.get() : null;
    }

    public Context getCurrentResumedActivityOrApplication() {
        Activity activity = getCurrentResumedActivity();
        if (activity != null) {
            return activity;
        }
        return DemoApplication.getApplication();
    }

    public void release() {
        DemoApplication.getApplication().unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        currentResumedActivity = null;
    }

}
