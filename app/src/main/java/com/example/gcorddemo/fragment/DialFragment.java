package com.example.gcorddemo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gcorddemo.InCallActivity;
import com.example.gcorddemo.databinding.DialFragmentBinding;
import com.example.gcorddemo.model.CallModel;
import com.example.gcorddemo.model.ContactModel;

import cn.com.geartech.gcordsdk.UnifiedPhoneController;

public class DialFragment extends BaseFragment {

    public static final String TAG = DialFragment.class.getName();
    private DialFragmentBinding dialFragmentBinding;
    private volatile boolean isDialing = false;

    public DialFragment(AppCompatActivity parentActivity) {
        super(parentActivity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        dialFragmentBinding = DialFragmentBinding.inflate(inflater, container, false);
        return dialFragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews();
    }

    private void initViews() {
        dialFragmentBinding.dialPad0.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPad1.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPad2.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPad3.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPad4.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPad5.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPad6.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPad7.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPad8.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPad9.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPadStar.setOnClickListener(this::onDialPadItemClick);
        dialFragmentBinding.dialPadPound.setOnClickListener(this::onDialPadItemClick);

        dialFragmentBinding.dialPadDial.setOnClickListener(this::onDialButtonClick);
        dialFragmentBinding.dialPadDelete.setOnClickListener(this::onDeleteClick);
        dialFragmentBinding.dialPadDelete.setOnLongClickListener(this::handleDeleteLongClick);

    }

    private boolean handleDeleteLongClick(View v) {
        clearNumbers();
        return true;
    }

    private void onDeleteClick(View view) {
        if (isDialing) return;
        String numbers = dialFragmentBinding.tvPhoneNumber.getText().toString();
        if (numbers.length() <= 0) return;
        numbers = numbers.substring(0, numbers.length() - 1);
        dialFragmentBinding.tvPhoneNumber.setText(numbers);
        if (numbers.length() == 0) dialFragmentBinding.dialPadDelete.setVisibility(View.GONE);
    }

    private void onDialPadItemClick(View view) {
        if (isDialing) return;
        String tag = (String) view.getTag();
        if (ContactModel.isStringNullOrEmpty(tag)) return;
        if ("star".equals(tag)) {
            tag = "*";
        } else if ("pound".equals(tag)) {
            tag = "#";
        }
        String numbers = dialFragmentBinding.tvPhoneNumber.getText().toString();
        numbers = String.format("%s%s", numbers, tag);
        dialFragmentBinding.tvPhoneNumber.setText(numbers);
        if (dialFragmentBinding.dialPadDelete.getVisibility() != View.VISIBLE)
            dialFragmentBinding.dialPadDelete.setVisibility(View.VISIBLE);
    }

    private void onDialButtonClick(View view) {
        String numbers = dialFragmentBinding.tvPhoneNumber.getText().toString().trim();
        if (numbers.length() == 0) return;
        final UnifiedPhoneController.DIAL_MODE dialMode = UnifiedPhoneController.getInstance().checkDialMode();
        UnifiedPhoneController.getInstance().autoDial(numbers);
        int callType = dialMode.ordinal();
        startInCallActivity(numbers, callType);
    }

    private void startInCallActivity(final String number, final int callType) {
        isDialing = true;
        clearNumbers();
        InCallActivity.startInCallActivity(
                CallModel.instance.getCurrentResumedActivityOrApplication(), number,
                callType, InCallActivity.CallInOrCallOut.CALL_OUT,
                InCallActivity.CallStatus.CALL_STATUS_PENDING, () -> isDialing = false);
    }

    private void clearNumbers() {
        dialFragmentBinding.tvPhoneNumber.setText("");
        dialFragmentBinding.dialPadDelete.setVisibility(View.GONE);
    }
}
