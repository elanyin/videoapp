package com.bytedance.videoapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.adapters.VideoPagerAdapter;
import com.bytedance.videoapp.model.MockData;
import com.bytedance.videoapp.model.VideoBean;

import java.util.List;

public class VideoDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_detail);

        // 从intent获取目标视频位置
        int targetPosition = getIntent().getIntExtra("pos", 0);

        List<VideoBean> videoList = MockData.getVideoList();

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new VideoPagerAdapter(videoList));
        viewPager.setCurrentItem(targetPosition, false);

        // 自动播放
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                playVideo(position);
            }
        });

        viewPager.post(() -> playVideo(targetPosition));
    }

    private void playVideo(int position) {
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        View currentView = recyclerView.getLayoutManager().findViewByPosition(position);

        if (currentView != null) {
            VideoView videoView = currentView.findViewById(R.id.videoView);
            if (videoView != null) {
                videoView.start();
                videoView.setOnCompletionListener(mp -> mp.start()); // 循环播放
            }
        }
    }
}
