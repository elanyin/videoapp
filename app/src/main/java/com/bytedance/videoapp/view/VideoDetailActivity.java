package com.bytedance.videoapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.adapters.VideoPagerAdapter;
import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.model.VideoRepository;
import com.bytedance.videoapp.player.PlayerManager;

import java.util.List;

@UnstableApi
public class VideoDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private VideoPagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        // 接收传递过来的位置
        int targetPosition = getIntent().getIntExtra("pos", 0);
        List<VideoBean> videoList = VideoRepository.getVideoList();

        initViewPager(videoList, targetPosition);
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager(List<VideoBean> videoList, int initialPosition) {
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager.setOffscreenPageLimit(1);

        adapter = new VideoPagerAdapter(videoList, this);
        viewPager.setAdapter(adapter);

        // 设置初始位置，第二个参数 false 表示不要平滑滚动，直接闪现过去
        viewPager.setCurrentItem(initialPosition, false);

        // 页面切换回调
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // 1. 播放视频逻辑
                VideoBean vb = videoList.get(position);
                String uri = "android.resource://" + getPackageName() + "/" + vb.videoResId;

                // 准备播放 (playWhenReady = true)
                PlayerManager.getInstance(VideoDetailActivity.this).prepareMedia(uri, true, 0);

                // 2. 将 PlayerView 绑定到当前页面
                // post一下，确保 Layout 已经完成，能找到 ViewHolder
                viewPager.post(() -> adapter.attachPlayerToHolder((RecyclerView) viewPager.getChildAt(0), position));
            }
        });

        // 如果是进入页面第一次加载，手动触发一次逻辑 (因为 onPageSelected 有时初始化不触发)
        viewPager.post(() -> {
            // 模拟触发一次选中逻辑
            VideoBean vb = videoList.get(initialPosition);
            String uri = "android.resource://" + getPackageName() + "/" + vb.videoResId;
            PlayerManager.getInstance(this).prepareMedia(uri, true, 0);
            adapter.attachPlayerToHolder((RecyclerView) viewPager.getChildAt(0), initialPosition);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 页面不可见时暂停播放
        PlayerManager.getInstance(this).getPlayer().pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 页面销毁时停止播放，释放资源
        PlayerManager.getInstance(this).getPlayer().stop();
    }

}
