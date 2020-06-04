package com.example.gcorddemo.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gcorddemo.InCallActivity;
import com.example.gcorddemo.R;
import com.example.gcorddemo.databinding.CallLogFragmentBinding;
import com.example.gcorddemo.dialogFragment.AlertDialogFragment;
import com.example.gcorddemo.dialogFragment.RecordPlayingDialogFragment;
import com.example.gcorddemo.model.ContactModel;
import com.example.gcorddemo.viewmodel.CallLogFragmentViewModel;
import com.excellence.basetoolslibrary.recycleradapter.BaseRecyclerAdapter;
import com.excellence.basetoolslibrary.recycleradapter.RecyclerViewHolder;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.com.geartech.gcordsdk.GcordSDK;
import cn.com.geartech.gcordsdk.UnifiedPhoneController;
import cn.com.geartech.gcordsdk.dataType.CallLogItem;


public class CallLogFragment extends BaseFragment {

    public static final String TAG = CallLogFragment.class.getName();
    private CallLogFragmentBinding callLogFragmentBinding;
    private CallLogFragmentViewModel mViewModel;
    private BaseRecyclerAdapter<CallLogItem> adapter;
    private CallLogItem itemToDelete = null;

    public CallLogFragment(AppCompatActivity parentActivity) {
        super(parentActivity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        callLogFragmentBinding = CallLogFragmentBinding.inflate(inflater, container, false);
        return callLogFragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(TAG, CallLogFragmentViewModel.class);
        mViewModel.getCallLogLiveData().observe(this.getViewLifecycleOwner(), this::onCallLogReceived);
        mViewModel.getDialogButtonLiveData().observe(this.getViewLifecycleOwner(), this::onDialogButtonClick);

        initViews();
        mViewModel.loadCallLogs();
    }

    private void onDialogButtonClick(Integer buttonType) {
        if (Objects.equals(buttonType, AlertDialogFragment.BUTTON_TYPE_RIGHT)) {
            if (itemToDelete != null)
                mViewModel.deleteCallLog(itemToDelete);
        }
    }

    private void showConfirmDialog() {
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.setParentActivity(parentActivity);
        alertDialogFragment.setLiveData(mViewModel.getDialogButtonLiveData());
        alertDialogFragment.show();
    }

    private void initViews() {
        callLogFragmentBinding.recyclerView.setLayoutManager(new LinearLayoutManager(parentActivity));
        callLogFragmentBinding.recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = 40;
                outRect.right = 30;
            }
        });
    }

    private void onCallLogReceived(List<CallLogItem> callLogItems) {
        Collections.sort(callLogItems, (o1, o2) -> {
            try {
                long delta = (o2.getCallBeginTimeStamp() - o1.getCallBeginTimeStamp());
                if (delta > 0) {
                    return 1;
                } else if (delta < 0) {
                    return -1;
                }
            } catch (Exception ignored) {

            }
            return 0;
        });
        if (adapter == null) {
            adapter = new BaseRecyclerAdapter<CallLogItem>(callLogItems, R.layout.layout_call_log_item) {
                @Override
                public void convert(RecyclerViewHolder viewHolder, CallLogItem item, int position) {
                    viewHolder.getView(R.id.rootView).setOnClickListener(v -> {
                        GcordSDK.getInstance().getUnifiedPhoneController().autoDial(item.getOpponentNumber());
                        startInCallActivity(item);
                    });
                    TextView tvName = viewHolder.getView(R.id.tvName);
                    String name = item.getContactName();

                    TextView tvNumber = viewHolder.getView(R.id.tvNumber);
                    String number = item.getOpponentNumber();
                    if (ContactModel.isStringNullOrEmpty(number))
                        number = parentActivity.getString(R.string.unknown_number);

                    if (ContactModel.isStringNullOrEmpty(name)) {
                        tvName.setText(number);
                        tvNumber.setVisibility(View.GONE);
                    } else {
                        tvName.setText(name);
                        tvNumber.setVisibility(View.VISIBLE);
                        tvNumber.setText(number);
                    }

                    TextView tvBeginTime = viewHolder.getView(R.id.tvBeginTime);
                    long begin = item.getCallBeginTimeStamp();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(begin);
                    String dateStr = String.format(Locale.getDefault(), "%1$02d-%2$02d %3$02d:%4$02d",
                            calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                    tvBeginTime.setText(dateStr);

                    ImageView ivCallType = viewHolder.getView(R.id.ivCallType);
                    String callResult = item.getCallResult();
                    String callType = item.getCallType();
                    if (CallLogItem.CALL_RESULT_MISSED.equals(callResult) && CallLogItem.CALL_TYPE_IN.equals(callType)) {
                        tvName.setSelected(true);
                        ivCallType.setImageLevel(0);
                    } else {
                        tvName.setSelected(false);
                        ivCallType.setImageLevel(CallLogItem.CALL_TYPE_IN.equals(callType) ? 2 : 1);
                    }

                    ImageView ivRecord = viewHolder.getView(R.id.ivRecord);
                    String callRecord = item.getCallRecord_id();
                    boolean fileExist = false;
                    if (!ContactModel.isStringNullOrEmpty(callRecord)) {
                        String filePath = item.getAudioRecordPath();
                        fileExist = (!ContactModel.isStringNullOrEmpty(filePath));
                    }
                    ivRecord.setSelected(fileExist);
                    ivRecord.setOnClickListener(v -> {
                        if (v.isSelected()) {
                            RecordPlayingDialogFragment.showDialog(parentActivity, item);
                        }
                    });

                    ImageView ivDelete = viewHolder.getView(R.id.ivDelete);
                    ivDelete.setOnClickListener(v -> {
                        itemToDelete = item;
                        showConfirmDialog();
                    });
                }
            };
            callLogFragmentBinding.recyclerView.setAdapter(adapter);
        } else {
            if (callLogFragmentBinding.recyclerView.getAdapter() == null)
                callLogFragmentBinding.recyclerView.setAdapter(adapter);
            adapter.notifyNewData(callLogItems);
        }
    }

    private void startInCallActivity(CallLogItem item) {
        final int callType = GcordSDK.getInstance().getUnifiedPhoneController().checkDialMode().ordinal();
        InCallActivity.startInCallActivity(parentActivity,
                item.getOpponentNumber(),
                callType, InCallActivity.CallInOrCallOut.CALL_OUT,
                InCallActivity.CallStatus.CALL_STATUS_PENDING);
    }
}
