package com.example.gcorddemo;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gcorddemo.databinding.ActivityMainBinding;
import com.example.gcorddemo.viewmodel.MainActivityViewModel;
import com.excellence.basetoolslibrary.recycleradapter.BaseRecyclerAdapter;
import com.excellence.basetoolslibrary.recycleradapter.RecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends BaseActivity {
    private MainActivityViewModel viewModel;
    private ActivityMainBinding binding;
    private BaseRecyclerAdapter<MainActivityViewModel.IndexDemoItem> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        viewModel = new ViewModelProvider(this).get(getClass().getName(), MainActivityViewModel.class);
        viewModel.getDemoLiveData().observe(this, this::setupDemoItems);
        viewModel.loadDemoItem();
    }


    private void setupDemoItems(List<MainActivityViewModel.IndexDemoItem> indexDemoItemList) {
        if (adapter == null) {
            adapter = new BaseRecyclerAdapter<MainActivityViewModel.IndexDemoItem>(indexDemoItemList, R.layout.layout_index_demo_item) {

                @Override
                public void convert(RecyclerViewHolder viewHolder, MainActivityViewModel.IndexDemoItem item, int position) {
                    TextView tvTitle = viewHolder.getView(R.id.tvTitle);
                    tvTitle.setText(item.getName());

                    TextView tvDescription = viewHolder.getView(R.id.tvDescription);
                    tvDescription.setText(item.getDescription());

                    ImageView ivDemoIcon = viewHolder.getView(R.id.ivDemoIcon);
                    ivDemoIcon.setImageURI(item.getIconResourceUri());
                    ImageView ivBg = viewHolder.getView(R.id.ivBg);
                    ivBg.setOnClickListener(view -> {
                        Class<?> cls = null;
                        if (1 == item.getId()) {
                            cls = PhoneDemoActivity.class;
                        } else if (2 == item.getId()) {
                            cls = SettingDemoActivity.class;
                        }
                        if (cls != null)
                            startActivity(new Intent(MainActivity.this, cls));
                    });
                }
            };
            binding.recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyNewData(indexDemoItemList);
        }
    }

    private void initViews() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = 80;
                outRect.right = 80;
                outRect.bottom = 50;
            }
        });
        setupDemoItems(new ArrayList<>());
    }
}
