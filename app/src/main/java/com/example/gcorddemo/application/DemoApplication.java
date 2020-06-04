package com.example.gcorddemo.application;

import android.app.Application;

import com.example.gcorddemo.model.CallModel;

import cn.com.geartech.gcordsdk.GcordSDK;
import cn.com.geartech.gcordui.GcordUI;

public final class DemoApplication extends Application {

    private static DemoApplication application;

    public static DemoApplication getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        initGcordComponents();
        CallModel.instance.init();
    }

    private void initGcordComponents() {
        GcordSDK.getInstance()
                .initSDK(this,
                        "demo@geartech.com",
                        "06D80892-D30B-451D-B678-675F751F2D83");
        GcordUI.getInstance().init(this);
    }


    @Override
    public void onTerminate() {
        CallModel.instance.release();
        super.onTerminate();
    }

}
