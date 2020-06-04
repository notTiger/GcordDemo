package com.example.gcorddemo.fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {
    AppCompatActivity parentActivity;


    BaseFragment(AppCompatActivity parentActivity) {
        this.parentActivity = parentActivity;
    }
}
