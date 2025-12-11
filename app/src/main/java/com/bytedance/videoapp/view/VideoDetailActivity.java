package com.bytedance.videoapp.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.adapters.VideoPagerAdapter;
import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.repository.VideoRepository;
import com.bytedance.videoapp.player.PlayerManager;
import com.bytedance.videoapp.viewmodel.VideoViewModel;

import java.util.List;

@UnstableApi
public class VideoDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private VideoPagerAdapter adapter;

    private VideoViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        // 接收传递过来的位置
        int targetPosition = getIntent().getIntExtra("pos", 0);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager.setOffscreenPageLimit(1);

        viewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        // 观察数据
        viewModel.videoList.observe(this, videoList -> {
            // 当数据回来时，才设置 Adapter
            if (videoList != null && !videoList.isEmpty()) {
                adapter = new VideoPagerAdapter(videoList, this);

                // 评论区点击
                adapter.setOnCommentClickListener(video -> {
                    CommentBottomSheet bottomSheet = new CommentBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "comment_sheet");
                });

                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(targetPosition, false);

                // Player 绑定逻辑
                initPlayerLogic(videoList, targetPosition);
            }
        });

        viewModel.loadVideos();

    }

    private void initPlayerLogic(List<VideoBean> videoList, int initialPosition) {
        // 必须等数据回来、Adapter设置好之后，才能配置播放逻辑
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                VideoBean vb = videoList.get(position);
                String uri = "android.resource://" + getPackageName() + "/" + vb.videoResId;
                PlayerManager.getInstance(VideoDetailActivity.this).prepareMedia(uri, true, 0);
                viewPager.post(() -> adapter.attachPlayerToHolder((RecyclerView) viewPager.getChildAt(0), position));
            }
        });

        // 手动播放第一次
        viewPager.post(() -> {
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
