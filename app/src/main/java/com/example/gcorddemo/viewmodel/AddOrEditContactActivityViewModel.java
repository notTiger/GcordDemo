package com.example.gcorddemo.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.example.gcorddemo.bean.ContactBean;
import com.example.gcorddemo.model.ContactModel;
import com.example.gcorddemo.util.ThreadModel;

public class AddOrEditContactActivityViewModel extends BaseViewModel {
    private MutableLiveData<Boolean> deletionLiveData;
    private MutableLiveData<Boolean> saveLiveData;
    private MutableLiveData<Integer> dialogButtonLiveData;

    public AddOrEditContactActivityViewModel() {
        this.deletionLiveData = new MutableLiveData<>();
        this.saveLiveData = new MutableLiveData<>();
        this.dialogButtonLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getDeletionLiveData() {
        return deletionLiveData;
    }

    public MutableLiveData<Boolean> getSaveLiveData() {
        return saveLiveData;
    }

    public void deleteContact(ContactBean contactBean) {
        ThreadModel.instance.execute(() -> {
            try {
                deletionLiveData.postValue(ContactModel.instance.delete(contactBean));
            } catch (Exception ex) {
                ex.printStackTrace();
                deletionLiveData.postValue(false);
            }
        });
    }

    public void addContact(String givenName, String familyName, String company, String phoneNumber) {
        ThreadModel.instance.execute(() -> {
            boolean result = false;
            try {
                result = ContactModel.instance.insert(givenName, familyName, company, phoneNumber);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.saveLiveData.postValue(result);
        });
    }

    public void update(ContactBean contactBean) {
        ThreadModel.instance.execute(() -> {
            boolean result = false;
            try {
                result = ContactModel.instance.update(contactBean);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.saveLiveData.postValue(result);
        });
    }

    public MutableLiveData<Integer> getDialogButtonLiveData() {
        return dialogButtonLiveData;
    }
}
