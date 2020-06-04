package com.example.gcorddemo.dialogFragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

public abstract class BaseDialogFragment extends DialogFragment {

    protected FragmentActivity parentActivity;

    protected MutableLiveData<Integer> liveData = null;

    public void setParentActivity(Activity parentActivity) {
        this.parentActivity = (FragmentActivity) parentActivity;
    }

    public void setLiveData(MutableLiveData<Integer> liveData) {
        this.liveData = liveData;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(1);
        dialog.setCanceledOnTouchOutside(this.cancelableOnTouchOutside());
        return dialog;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    protected boolean cancelableOnTouchOutside() {
        return true;
    }

    public void show() {
        if (this.parentActivity == null) {
            this.parentActivity = this.getActivity();
        }
        try {
            if (this.parentActivity != null) {
                this.show(this.parentActivity.getSupportFragmentManager(), "tag");
            } else {
                Log.e(getClass().getName(), "parentActivity == null, cannot show dialog");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        super.onResume();
        Dialog dialog = this.getDialog();
        if(dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(this.getWidth(), this.getHeight());
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }
}
