package com.example.gcorddemo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.gcorddemo.bean.ContactBean;
import com.example.gcorddemo.model.ContactModel;
import com.example.gcorddemo.util.ThreadModel;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragmentViewModel extends ViewModel {
    private MutableLiveData<List<ContactBean>> contactLiveData;

    public ContactsFragmentViewModel() {
        this.contactLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    public MutableLiveData<List<ContactBean>> getContactLiveData() {
        return contactLiveData;
    }

    public void loadContacts() {
        ThreadModel.instance.execute(() -> {
            ArrayList<ContactBean> contactBeans = ContactModel.instance.queryAllContacts();
            contactLiveData.postValue(contactBeans);
        });
    }
}
