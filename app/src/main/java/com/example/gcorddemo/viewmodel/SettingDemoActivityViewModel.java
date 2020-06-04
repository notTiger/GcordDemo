package com.example.gcorddemo.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingDemoActivityViewModel extends BaseViewModel {
    private MutableLiveData<List<DemoItem>> settingDemoLiveData = new MutableLiveData<>(new ArrayList<>());

    public MutableLiveData<List<DemoItem>> getSettingDemoLiveData() {
        return settingDemoLiveData;
    }

    public void loadSettingDemoItem() {
        try {
            String string = getStringFromAssetsFile("setting_demo_item.json");
            if (!string.isEmpty()) {
                List<DemoItem> demoItemList = JSON.parseArray(string, DemoItem.class);
                settingDemoLiveData.postValue(demoItemList);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        settingDemoLiveData.postValue(new ArrayList<>());
    }
}
