package com.example.gcorddemo.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import cn.com.geartech.gcordsdk.CallLogManager;
import cn.com.geartech.gcordsdk.GcordSDK;
import cn.com.geartech.gcordsdk.dataType.CallLogItem;

public class CallLogFragmentViewModel extends BaseViewModel {
    private final ArrayList<CallLogItem> callLogItems;
    private MutableLiveData<List<CallLogItem>> callLogLiveData;
    private MutableLiveData<Integer> dialogButtonLiveData;

    public CallLogFragmentViewModel() {
        callLogLiveData = new MutableLiveData<>(callLogItems = new ArrayList<>());
        dialogButtonLiveData = new MutableLiveData<>();
        GcordSDK.getInstance().getCallLogManager().setCallLogUpdateCallback(new CallLogManager.CallLogUpdateCallback() {

            @Override
            public void onCallLogUpdated(List<CallLogItem> callLogs) {
//                callLogLiveData.postValue(callLogs != null ? callLogs : new ArrayList<>());
//                synchronized (callLogItems) {
//                    callLogItems.clear();
//                    if (callLogs != null && callLogs.size() > 0)
//                        callLogItems.addAll(callLogs);
//                }
            }

            @Override
            public void onNewCallLogAdded(CallLogItem callLogItem) {
                synchronized (callLogItems) {
                    if (callLogItems.size() > 0) callLogItems.add(0, callLogItem);
                    else {
                        callLogItems.add(callLogItem);
                    }
                }
                callLogLiveData.postValue(new ArrayList<>(callLogItems));
            }
        });
    }

    public MutableLiveData<Integer> getDialogButtonLiveData() {
        return dialogButtonLiveData;
    }

    public MutableLiveData<List<CallLogItem>> getCallLogLiveData() {
        return callLogLiveData;
    }

    public void loadCallLogs() {
        if (callLogItems.size() > 0) {
            callLogLiveData.postValue(new ArrayList<>(callLogItems));
        }
        GcordSDK.getInstance().getCallLogManager().getCallLogs(callLogs -> {
            synchronized (callLogItems) {
                try {
                    callLogItems.clear();
                    callLogItems.addAll(callLogs);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            callLogLiveData.postValue(callLogs);
        });
    }

    public void deleteCallLog(CallLogItem item) {
        GcordSDK.getInstance().getCallLogManager().deleteCallLog(item);
        new Handler(Looper.getMainLooper()).postDelayed(this::loadCallLogs, 1000L);
    }

    @Override
    protected void onCleared() {
        GcordSDK.getInstance().getCallLogManager().setCallLogUpdateCallback(null);
        super.onCleared();
    }
}
