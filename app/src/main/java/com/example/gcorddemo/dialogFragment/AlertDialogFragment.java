package com.example.gcorddemo.dialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gcorddemo.databinding.DialogAlertBinding;

public class AlertDialogFragment extends BaseDialogFragment {
    public static final Integer BUTTON_TYPE_LEFT = 0;
    public static final Integer BUTTON_TYPE_RIGHT = 1;
    private DialogAlertBinding dialogAlertBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dialogAlertBinding = DialogAlertBinding.inflate(inflater, container, false);
        return dialogAlertBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        dialogAlertBinding.tvCancel.setOnClickListener(v -> {
            if (liveData != null) {
                liveData.postValue(BUTTON_TYPE_LEFT);
            }
            dismiss();
        });
        dialogAlertBinding.tvDelete.setOnClickListener(v -> {
            if (liveData != null) {
                liveData.postValue(BUTTON_TYPE_RIGHT);
            }
            dismiss();
        });
    }

    @Override
    public int getWidth() {
        return 430;
    }

    @Override
    public int getHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }
}
