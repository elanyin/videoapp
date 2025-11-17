package com.bytedance.videoapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bytedance.videoapp.model.MockData;
import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.view.VideoAdapter;
import com.bytedance.videoapp.view.VideoDetailActivity;
import com.bytedance.videoapp.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

//    private HomeViewModel viewModel;  // TODO
    private VideoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // init the list component (recyclerView)
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // set the layout manager: spanCount = 2, vertical
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);

        // init adapter
        adapter = new VideoAdapter();
        recyclerView.setAdapter(adapter);

        // MVP: mock data
        List<VideoBean> videoList = MockData.getVideoList();

        adapter.setData(videoList);

        adapter.setOnItemClickListener((video, position) -> {
            // 跳转Intent
            Intent intent = new Intent(MainActivity.this, VideoDetailActivity.class);

            intent.putExtra("pos", position);

            startActivity(intent);
        });
    }
}