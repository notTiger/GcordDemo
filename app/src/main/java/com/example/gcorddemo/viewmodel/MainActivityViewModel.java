package com.example.gcorddemo.viewmodel;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson.JSON;
import com.example.gcorddemo.application.DemoApplication;
import com.example.gcorddemo.model.ContactModel;

import java.util.ArrayList;
import java.util.List;

public final class MainActivityViewModel extends BaseViewModel {
    private MutableLiveData<List<IndexDemoItem>> demoLiveData = new MutableLiveData<>();

    public MutableLiveData<List<IndexDemoItem>> getDemoLiveData() {
        return demoLiveData;
    }

    public void loadDemoItem() {
        try {
            String string = getStringFromAssetsFile("index_demo_item.json");
            if (!string.isEmpty()) {
                List<IndexDemoItem> indexDemoItemList = JSON.parseArray(string, IndexDemoItem.class);
                demoLiveData.postValue(indexDemoItemList);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        demoLiveData.postValue(new ArrayList<>());
    }

    @SuppressWarnings({"unused"})
    public static class IndexDemoItem extends BaseViewModel.DemoItem {
        static final long serialVersionUID = 42L;
        private String icon;

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public Uri getIconResourceUri() {
            if (!ContactModel.isStringNullOrEmpty(icon)) {
                return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + DemoApplication.getApplication().getPackageName()
                        + "/" + icon);
            }
            return null;
        }
    }
}
