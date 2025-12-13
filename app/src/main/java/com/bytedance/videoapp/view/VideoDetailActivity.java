package com.bytedance.videoapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.adapters.VideoPagerAdapter;
import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.player.PlayerManager;
import com.bytedance.videoapp.viewmodel.VideoViewModel;

import java.util.List;

/**
 * 视频详情页 (全屏播放)
 * <p>
 * 职责:
 * 1.  接收从首页点击的视频位置 (position) 和封面图 (coverResId)。
 * 2.  使用 ViewPager2 实现上下滑动切换视频的功能。
 * 3.  通过 ViewModel 获取视频数据列表。
 * 4.  管理视频的播放、暂停和释放，与 PlayerManager 单例进行交互。
 * 5.  处理从封面到视频播放的平滑过渡，避免黑屏。
 */
@UnstableApi
public class VideoDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private VideoPagerAdapter adapter;
    private VideoViewModel viewModel;

    // 用于在视频加载时显示的临时封面，这是解决初始黑屏问题的关键。
    private ImageView tempCover;
    // 标记是否已经执行过从封面到播放器的过渡动画，防止重复执行。
    private boolean isTransitioned = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        // 1. 从 Intent 中获取必要的数据
        int targetPosition = getIntent().getIntExtra("pos", 0);
        int coverResId = getIntent().getIntExtra("cover_res_id", 0);

        // 2. 初始化UI组件
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        // 设置预加载，提前创建和绑定相邻的 ViewHolder，让滑动更流畅
        viewPager.setOffscreenPageLimit(1);

        // 3. 黑屏问题解决方案：立即显示封面
        tempCover = findViewById(R.id.temp_cover);
        if (coverResId != 0) {
            // 立刻将传递过来的封面图设置给临时的 ImageView 并显示它
            tempCover.setImageResource(coverResId);
            tempCover.setVisibility(View.VISIBLE);
            // 同时让 ViewPager 也可见但完全透明，为后续的淡入动画做准备
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setAlpha(0f);
        } else {
            // 如果没有封面图，则直接认为过渡已完成
            isTransitioned = true;
        }

        // 4. 初始化 ViewModel 并观察数据变化
        viewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        viewModel.videoList.observe(this, videoList -> {
            if (videoList != null && !videoList.isEmpty()) {
                // 5. 数据回来后，设置 Adapter
                adapter = new VideoPagerAdapter(videoList, this);

                // 设置评论区点击监听
                adapter.setOnCommentClickListener(video -> {
                    CommentBottomSheet bottomSheet = new CommentBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "comment_sheet");
                });

                // 6.【关键】设置播放器渲染回调，这是保证平滑过渡的核心
                adapter.setOnFirstFrameRenderedListener(() -> {
                    // 只有在播放器真正渲染出第一帧画面时，才执行过渡动画
                    if (!isTransitioned) {
                        // ViewPager 淡入
                        viewPager.animate().alpha(1f).setDuration(300).start();
                        // 临时封面图淡出
                        tempCover.animate().alpha(0f).setDuration(300)
                                .withEndAction(() -> tempCover.setVisibility(View.GONE)).start();
                        isTransitioned = true; // 标记动画已执行
                    }
                });

                viewPager.setAdapter(adapter);
                // 跳转到用户点击的视频位置，false表示无切换动画
                viewPager.setCurrentItem(targetPosition, false);

                // 7. 初始化播放相关逻辑
                initPlayerLogic(videoList, targetPosition);
            }
        });

        // 8. 触发 ViewModel 加载数据
        viewModel.loadAllCachedData();
    }

    /**
     * 初始化播放相关的所有逻辑，包括页面切换监听和首次播放的触发。
     */
    private void initPlayerLogic(List<VideoBean> videoList, int initialPosition) {
        // 监听 ViewPager2 的页面切换事件
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 当滑动到新页面时，播放对应位置的视频
                playVideoAtPosition(position, videoList);
            }
        });

        // 使用 post 确保在 ViewPager2 完成布局后再触发首次播放
        viewPager.post(() -> {
            playVideoAtPosition(initialPosition, videoList);
        });
    }

    /**
     * 在指定位置播放视频。
     * @param position 要播放的视频在列表中的位置
     * @param videoList 视频数据列表
     */
    private void playVideoAtPosition(int position, List<VideoBean> videoList) {
        if (position < 0 || position >= videoList.size() || adapter == null) return;

        VideoBean vb = videoList.get(position);
        String uri = "android.resource://" + getPackageName() + "/" + vb.videoResId;

        // 通知 PlayerManager 准备媒体资源，并设置为“准备好后立即播放”
        PlayerManager.getInstance(this).prepareMedia(uri, true, 0);
        
        // 通知 Adapter 将播放器实例附加到当前位置的 ViewHolder 上
        adapter.attachPlayerToHolder(position);
    }

    /**
     * Activity 生命周期：页面不可见时暂停播放
     */
    @Override
    protected void onPause() {
        super.onPause();
        // 从经过加固的 PlayerManager 中获取播放器，确保 player 实例总是有效
        Player player = PlayerManager.getInstance(this).getPlayer();
        if (player.isPlaying()) {
            player.pause();
        }
    }

    /**
     * Activity 生命周期：页面销毁时释放播放器资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 调用 releasePlayer() 而不是 shutdown()，只释放播放器内核，不销毁单例
        PlayerManager.getInstance(this).releasePlayer();
    }
}
