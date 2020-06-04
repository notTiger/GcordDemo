package com.example.gcorddemo.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.example.gcorddemo.bean.ContactBean;
import com.example.gcorddemo.model.ContactModel;
import com.example.gcorddemo.util.ThreadModel;

public class InCallActivityViewModel extends BaseViewModel {

    private MutableLiveData<ContactBean> contactLiveData;

    public InCallActivityViewModel() {
        this.contactLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<ContactBean> getContactLiveData() {
        return contactLiveData;
    }

    public void findContactByPhoneNumber(String number) {

        ThreadModel.instance.execute(() -> {
            ContactBean contactBean = ContactModel.instance.queryByPhoneNumber(number);
            contactLiveData.postValue(contactBean);

        });
    }
}
