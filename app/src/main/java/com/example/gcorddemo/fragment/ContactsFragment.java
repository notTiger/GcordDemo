package com.example.gcorddemo.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gcorddemo.AddOrEditContactActivity;
import com.example.gcorddemo.R;
import com.example.gcorddemo.bean.ContactBean;
import com.example.gcorddemo.databinding.ContactsFragmentBinding;
import com.example.gcorddemo.model.ContactModel;
import com.example.gcorddemo.viewmodel.ContactsFragmentViewModel;
import com.excellence.basetoolslibrary.recycleradapter.BaseRecyclerAdapter;
import com.excellence.basetoolslibrary.recycleradapter.RecyclerViewHolder;

import java.util.List;

public class ContactsFragment extends BaseFragment {

    public static final String TAG = ContactsFragment.class.getName();
    ContactsFragmentBinding contactsFragmentBinding;
    private ContactsFragmentViewModel mViewModel;
    private BaseRecyclerAdapter<ContactBean> adapter = null;

    public ContactsFragment(AppCompatActivity parentActivity) {
        super(parentActivity);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contactsFragmentBinding = ContactsFragmentBinding.inflate(inflater, container, false);
        return contactsFragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(ContactsFragmentViewModel.class);
        initViews();
        mViewModel.getContactLiveData().observe(this.getViewLifecycleOwner(), this::setupContactList);
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.loadContacts();
    }

    private void initViews() {
        contactsFragmentBinding.recyclerView.setLayoutManager(new LinearLayoutManager(parentActivity));
        contactsFragmentBinding.recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = outRect.right = 40;
            }
        });
        contactsFragmentBinding.ivAdd.setOnClickListener(v -> AddOrEditContactActivity.startAddOrEditContactActivity(parentActivity, null));
    }

    private void setupContactList(List<ContactBean> contactBeans) {
        if (adapter == null) {
            adapter = new BaseRecyclerAdapter<ContactBean>(contactBeans, R.layout.layout_contact_item) {
                @Override
                public void convert(RecyclerViewHolder viewHolder, ContactBean item, int position) {
                    TextView tvName = viewHolder.getView(R.id.tvName);
                    String name = item.getDisplayName();
                    if (ContactModel.isStringNullOrEmpty(name)) {
                        String familyName = item.getFamilyName();
                        String givenName = item.getGivenName();
                        name = familyName + " " + givenName;
                    }
                    tvName.setText(name);
                    View root = viewHolder.getView(R.id.rootView);
                    root.setOnClickListener(v -> AddOrEditContactActivity.startAddOrEditContactActivity(parentActivity, item));
                }
            };
            contactsFragmentBinding.recyclerView.setAdapter(adapter);
        } else {
            if (contactsFragmentBinding.recyclerView.getAdapter() == null)
                contactsFragmentBinding.recyclerView.setAdapter(adapter);
            adapter.notifyNewData(contactBeans);
        }
    }

}
