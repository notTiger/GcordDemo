package com.example.gcorddemo;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gcorddemo.databinding.ActivitySettingDemoBinding;
import com.example.gcorddemo.viewmodel.BaseViewModel;
import com.example.gcorddemo.viewmodel.SettingDemoActivityViewModel;
import com.excellence.basetoolslibrary.recycleradapter.BaseRecyclerAdapter;
import com.excellence.basetoolslibrary.recycleradapter.RecyclerViewHolder;

import java.util.List;

public class SettingDemoActivity extends BaseActivity {
    private ActivitySettingDemoBinding activitySettingDemoBinding;
    private BaseRecyclerAdapter<BaseViewModel.DemoItem> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySettingDemoBinding = ActivitySettingDemoBinding.inflate(getLayoutInflater());
        setContentView(activitySettingDemoBinding.getRoot());
        initViews();
        SettingDemoActivityViewModel settingDemoActivityViewModel = new ViewModelProvider(this).get(getClass().getName(), SettingDemoActivityViewModel.class);
        settingDemoActivityViewModel.getSettingDemoLiveData().observe(this, this::setupDemoItems);
        settingDemoActivityViewModel.loadSettingDemoItem();
    }

    private void initViews() {
        activitySettingDemoBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        activitySettingDemoBinding.recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom = 30;
            }
        });
    }

    private void setupDemoItems(List<BaseViewModel.DemoItem> demoItemList) {
        if (adapter == null) {
            adapter = new BaseRecyclerAdapter<BaseViewModel.DemoItem>(demoItemList, R.layout.layout_setting_demo_item) {
                @Override
                public void convert(RecyclerViewHolder viewHolder, BaseViewModel.DemoItem item, int position) {
                    TextView tvTitle = viewHolder.getView(R.id.tvTitle);
                    tvTitle.setText(item.getName());

                    TextView tvDescription = viewHolder.getView(R.id.tvDescription);
                    tvDescription.setText(item.getDescription());
                    viewHolder.getView(R.id.rootView).setOnClickListener(view -> {
                        Intent intent = null;
                        switch (item.getId()) {
                            case 1:
                                intent = new Intent("gcord.intent.activity.setting");
                                /*
                                    只显示wifi设置，以太网设置，时间设置，声音设置，sip设置等5项
                                    相关的可选项有：WiFi，Ethernet，Time，Sound , Sip, Update, Advance , About, Screen, Security，Gcord
                                 */
                                intent.putExtra("Activities", new String[]{"WiFi", "Ethernet", "Time", "Sound", "Sip"});
                                break;
                            case 2:
                                /*
                                    隐藏wifi设置，以太网设置，时间设置，声音设置，sip设置等5项
                                    相关的可选项有：WiFi，Ethernet，Time，Sound , Sip, Update, Advance , About, Screen, Security，Gcord
                                 */
                                intent = new Intent("cn.com.geartech.action.settings");
                                intent.putExtra("cn.com.geartech.extra_hide_home_key", true);
                                intent.putExtra("ExcludedActivity", new String[]{"WiFi", "Ethernet", "Time", "Sound", "Sip"});
                                break;
                            case 3:
                                //详细设置请看AndroidManifest.xml文件中的meta-data设置
                                intent = new Intent("cn.com.geartech.action.settings");
                                break;
                        }
                        if (intent != null) {
                            startActivity(intent);
                        }
                    });
                }
            };
            activitySettingDemoBinding.recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyNewData(demoItemList);
        }
    }
}
