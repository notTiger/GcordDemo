package com.example.gcorddemo.fragment.factory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

import com.example.gcorddemo.fragment.CallLogFragment;
import com.example.gcorddemo.fragment.ContactsFragment;
import com.example.gcorddemo.fragment.DialFragment;

public class CustomFragmentFactory extends FragmentFactory {
    private CallLogFragment callLogFragment = null;
    private ContactsFragment contactsFragment = null;
    private DialFragment dialFragment = null;
    private AppCompatActivity parentActivity;

    public CustomFragmentFactory(AppCompatActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        if (CallLogFragment.class.getName().equals(className))
            return callLogFragment != null ? callLogFragment : (callLogFragment = new CallLogFragment(parentActivity));
        if (ContactsFragment.class.getName().equals(className))
            return contactsFragment != null ? contactsFragment : (contactsFragment = new ContactsFragment(parentActivity));
        if (DialFragment.class.getName().equals(className))
            return dialFragment != null ? dialFragment : (dialFragment = new DialFragment(parentActivity));
        return super.instantiate(classLoader, className);
    }
}
