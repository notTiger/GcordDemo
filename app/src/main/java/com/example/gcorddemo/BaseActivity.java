package com.example.gcorddemo;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gcorddemo.databinding.ActivityBaseBinding;

import cn.com.geartech.gcordsdk.GcordSDK;
import cn.com.geartech.gcordsdk.HomeKeyManager;

public abstract class BaseActivity extends AppCompatActivity implements HomeKeyManager.HomeKeyListener {
    ActivityBaseBinding activityBaseBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBaseBinding = ActivityBaseBinding.inflate(getLayoutInflater());
        super.setContentView(activityBaseBinding.getRoot());
        initNavigationBar();
    }

    @Override
    public void setContentView(View view) {
        activityBaseBinding.contentLayout.addView(view);
    }

    private void initNavigationBar() {
        activityBaseBinding.navigationBar.imageNavBack.setOnClickListener(v -> onBackPressed());
        activityBaseBinding.navigationBar.imageNavHome.setOnClickListener(v -> GcordSDK.getInstance().getHomeKeyManager().performHomeEvent());
    }

    @Override
    public void onHomeClicked() {
        GcordSDK.getInstance().getHomeKeyManager().performHomeEvent();
    }

    @Override
    public void onResumedByHomeKey() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        GcordSDK.getInstance().getHomeKeyManager().setHomeKeyActionType(HomeKeyManager.HOME_KEY_ACTION_TYPE.CUSTOM_KEY_EVENT);
        GcordSDK.getInstance().getHomeKeyManager().addHomeKeyEventListener(this);
    }

    @Override
    protected void onPause() {
        GcordSDK.getInstance().getHomeKeyManager().removeHomeKeyEventListener(this);
        super.onPause();
    }

    protected void hideNavigationBar() {
        activityBaseBinding.navigationBar.getRoot().setVisibility(View.GONE);
    }

    protected void changeStatusBarToTransparent() {
        activityBaseBinding.statusBar.changeToTransparentBackground();
    }
}
