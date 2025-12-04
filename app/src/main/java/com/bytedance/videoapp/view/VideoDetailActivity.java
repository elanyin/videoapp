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
import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.model.VideoRepository;

import java.util.List;

public class VideoDetailActivity extends AppCompatActivity {

    private static final String EXTRA_POSITION = "pos";
    
    private ViewPager2 viewPager;
    private VideoPagerAdapter adapter;
    private int currentPlayingPosition = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        int targetPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        List<VideoBean> videoList = VideoRepository.getVideoList();

        initViewPager(videoList, targetPosition);
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager(List<VideoBean> videoList, int initialPosition) {
        viewPager = findViewById(R.id.viewPager);
        
        adapter = new VideoPagerAdapter(videoList);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(initialPosition, false);

        // 设置页面切换监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                playVideoAtPosition(position);
            }
        });

        // 播放初始位置的视频
        viewPager.post(() -> playVideoAtPosition(initialPosition));
    }

    /**
     * 播放指定位置的视频
     */
    private void playVideoAtPosition(int position) {
        // 停止当前播放的视频
        stopCurrentVideo();

        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        if (recyclerView != null) {
            View currentView = recyclerView.getLayoutManager().findViewByPosition(position);
            if (currentView != null) {
                VideoView videoView = currentView.findViewById(R.id.videoView);
                if (videoView != null) {
                    currentPlayingPosition = position;
                    videoView.start();
                    videoView.setOnCompletionListener(mp -> mp.start()); // 循环播放
                }
            }
        }
    }

    /**
     * 停止当前播放的视频
     */
    private void stopCurrentVideo() {
        if (currentPlayingPosition < 0 || viewPager == null) {
            return;
        }

        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        if (recyclerView != null) {
            View currentView = recyclerView.getLayoutManager().findViewByPosition(currentPlayingPosition);
            if (currentView != null) {
                VideoView videoView = currentView.findViewById(R.id.videoView);
                if (videoView != null && videoView.isPlaying()) {
                    videoView.pause();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCurrentVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentPlayingPosition >= 0) {
            playVideoAtPosition(currentPlayingPosition);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCurrentVideo();
    }
}
