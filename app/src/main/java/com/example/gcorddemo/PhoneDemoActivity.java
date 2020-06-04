package com.example.gcorddemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.gcorddemo.databinding.ActivityPhoneDemoBinding;
import com.example.gcorddemo.fragment.CallLogFragment;
import com.example.gcorddemo.fragment.ContactsFragment;
import com.example.gcorddemo.fragment.DialFragment;
import com.example.gcorddemo.fragment.factory.CustomFragmentFactory;
import com.google.android.material.tabs.TabLayout;

public class PhoneDemoActivity extends BaseActivity {

    private ActivityPhoneDemoBinding activityPhoneDemoBinding;
    private LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportFragmentManager().setFragmentFactory(new CustomFragmentFactory(this));
        super.onCreate(savedInstanceState);
        activityPhoneDemoBinding = ActivityPhoneDemoBinding.inflate(getLayoutInflater());
        setContentView(activityPhoneDemoBinding.getRoot());
        initViews();
    }

    private void initViews() {
        layoutInflater = getLayoutInflater();
        activityPhoneDemoBinding.tabLayout.addTab(
                activityPhoneDemoBinding.tabLayout.newTab()
                        .setCustomView(generateTab(R.string.call_log, R.drawable.tab_call_log_bg, CallLogFragment.TAG)));
        activityPhoneDemoBinding.tabLayout.addTab(
                activityPhoneDemoBinding.tabLayout.newTab()
                        .setCustomView(generateTab(R.string.dial_pad, R.drawable.tab_dial_pad_bg, DialFragment.TAG)));
        activityPhoneDemoBinding.tabLayout.addTab(
                activityPhoneDemoBinding.tabLayout.newTab()
                        .setCustomView(generateTab(R.string.contact, R.drawable.tab_contact_bg, ContactsFragment.TAG)));
        activityPhoneDemoBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabSelected(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTabSelected(tab, false);
            }

            private void setTabSelected(TabLayout.Tab tab, boolean selected) {
                View view = tab.getCustomView();
                if (view != null) {
                    view.setSelected(selected);
                    if (selected)
                        showFragmentByTag((String) view.getTag(), tab.getPosition());
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void showFragmentByTag(String tag, int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Class<? extends Fragment> cls = null;
        switch (position) {
            case 0:
                cls = CallLogFragment.class;
                break;
            case 1:
                cls = DialFragment.class;
                break;
            case 2:
                cls = ContactsFragment.class;
                break;
        }
        if (cls != null)
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, cls, null, tag).commit();
    }

    private View generateTab(int text, int imageResource, String tag) {
        View view = layoutInflater.inflate(R.layout.layout_tab_item, activityPhoneDemoBinding.tabLayout, false);
        TextView tvName = view.findViewById(R.id.tvName);
        tvName.setText(text);
        ImageView iv = view.findViewById(R.id.iv);
        iv.setImageResource(imageResource);
        view.setTag(tag);
        return view;
    }
}
