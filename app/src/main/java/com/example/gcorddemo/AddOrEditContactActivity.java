package com.example.gcorddemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.gcorddemo.bean.ContactBean;
import com.example.gcorddemo.databinding.ActivityAddOrEditContactBinding;
import com.example.gcorddemo.dialogFragment.AlertDialogFragment;
import com.example.gcorddemo.viewmodel.AddOrEditContactActivityViewModel;

import java.util.Objects;

public class AddOrEditContactActivity extends BaseActivity {
    private static final String _CONTACT_ = "contact";
    private ActivityAddOrEditContactBinding activityAddOrEditContactBinding;
    private ContactBean contactBean;
    private AddOrEditContactActivityViewModel addOrEditContactActivityViewModel;

    public static void startAddOrEditContactActivity(Context context, ContactBean contactBean) {
        Intent intent = new Intent(context, AddOrEditContactActivity.class);
        if (contactBean != null) {
            intent.putExtra(_CONTACT_, contactBean);
        }
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityAddOrEditContactBinding = ActivityAddOrEditContactBinding.inflate(getLayoutInflater());
        setContentView(activityAddOrEditContactBinding.getRoot());
        setupViews();
        addOrEditContactActivityViewModel = new ViewModelProvider(this).get(getClass().getName(), AddOrEditContactActivityViewModel.class);
        addOrEditContactActivityViewModel.getDeletionLiveData().observe(this, this::onDeleted);
        addOrEditContactActivityViewModel.getDialogButtonLiveData().observe(this, this::onDialogButtonClick);
        addOrEditContactActivityViewModel.getSaveLiveData().observe(this, this::onSave);
        activityAddOrEditContactBinding.etNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                activityAddOrEditContactBinding.tvSave.setEnabled(s.length() > 0);
            }
        });
    }

    private void onDeleted(boolean result) {
        if (result) {
            Toast.makeText(this, R.string.deleted, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void onSave(boolean result) {
        if (result) {
            Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void onDialogButtonClick(Integer buttonType) {
        if (Objects.equals(buttonType, AlertDialogFragment.BUTTON_TYPE_RIGHT)) {
            addOrEditContactActivityViewModel.deleteContact(contactBean);
        }
    }

    private void showDeletionConfirmDialog() {
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.setParentActivity(this);
        alertDialogFragment.setLiveData(addOrEditContactActivityViewModel.getDialogButtonLiveData());
        alertDialogFragment.show();
    }

    private void setupViews() {
        Intent intent = getIntent();
        contactBean = (ContactBean) intent.getSerializableExtra(_CONTACT_);
        boolean toModify = contactBean != null;
        activityAddOrEditContactBinding.tvCancel.setSelected(toModify);
        activityAddOrEditContactBinding.tvCancel.setText(toModify ? R.string.delete : R.string.cancel);
        activityAddOrEditContactBinding.tvCancel.setOnClickListener(v -> {
            if (v.isSelected()) {
                if (contactBean != null) {
                    showDeletionConfirmDialog();
                }
            } else {
                finish();
            }
        });
        activityAddOrEditContactBinding.tvSave.setEnabled(toModify);
        activityAddOrEditContactBinding.tvSave.setOnClickListener(v -> {
            if (v.isEnabled()) {
                doSaveContact();
            }
        });
        activityAddOrEditContactBinding.tvTitle.setText(toModify ? contactBean.getDisplayName() : getString(R.string.add_contact));
        if (toModify) {
            activityAddOrEditContactBinding.etFamilyName.setText(contactBean.getFamilyName());
            activityAddOrEditContactBinding.etGivenName.setText(contactBean.getGivenName());
            activityAddOrEditContactBinding.etCompany.setText(contactBean.getCompany());
            if (activityAddOrEditContactBinding.etFamilyName.length() > 0)
                activityAddOrEditContactBinding.etFamilyName.setSelection(activityAddOrEditContactBinding.etFamilyName.length());
            if (contactBean.getPhoneDataList().size() > 0) {
                activityAddOrEditContactBinding.etNumber.setText(contactBean.getPhoneDataList().get(0).getPhoneNumber());
            }
        }
    }

    private void doSaveContact() {
        String familyName = activityAddOrEditContactBinding.etFamilyName.getText().toString().trim();
        String givenName = activityAddOrEditContactBinding.etGivenName.getText().toString().trim();

        String phoneNumber = activityAddOrEditContactBinding.etNumber.getText().toString().trim();
        if (phoneNumber.length() == 0) {
            Toast.makeText(this, R.string.phone_number_is_empty, Toast.LENGTH_LONG).show();
            return;
        }
        if (familyName.length() == 0 && givenName.length() == 0) {
            givenName = phoneNumber;
        }
        String company = activityAddOrEditContactBinding.etCompany.getText().toString().trim();
        ContactBean copy;
        if (contactBean != null) {
            copy = new ContactBean(contactBean);
            copy.setFamilyName(familyName);
            copy.setGivenName(givenName);
            copy.setCompany(company);
            if (copy.getPhoneDataList().size() > 0)
                copy.getPhoneDataList().get(0).setPhoneNumber(phoneNumber);
            else {
                copy.addPhoneNumber("", phoneNumber);
            }
            addOrEditContactActivityViewModel.update(copy);
        } else {
            addOrEditContactActivityViewModel.addContact(givenName, familyName, phoneNumber, company);
        }
    }
}