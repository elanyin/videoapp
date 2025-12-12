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

@UnstableApi
public class VideoDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private VideoPagerAdapter adapter;
    private VideoViewModel viewModel;

    private ImageView tempCover;
    private boolean isTransitioned = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        int targetPosition = getIntent().getIntExtra("pos", 0);
        int coverResId = getIntent().getIntExtra("cover_res_id", 0);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager.setOffscreenPageLimit(1);

        tempCover = findViewById(R.id.temp_cover);

        if (coverResId != 0) {
            tempCover.setImageResource(coverResId);
            tempCover.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE); // Make it visible but transparent
            viewPager.setAlpha(0f);
        } else {
            // If there's no cover, just show the ViewPager directly
            isTransitioned = true;
        }

        viewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        viewModel.videoList.observe(this, videoList -> {
            if (videoList != null && !videoList.isEmpty()) {
                adapter = new VideoPagerAdapter(videoList, this);

                adapter.setOnCommentClickListener(video -> {
                    CommentBottomSheet bottomSheet = new CommentBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "comment_sheet");
                });

                // Set the listener for the first frame render event
                adapter.setOnFirstFrameRenderedListener(() -> {
                    if (!isTransitioned) {
                        // This is the reliable moment to start the transition
                        viewPager.animate().alpha(1f).setDuration(300).start();
                        tempCover.animate().alpha(0f).setDuration(300)
                                .withEndAction(() -> tempCover.setVisibility(View.GONE)).start();
                        isTransitioned = true;
                    }
                });

                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(targetPosition, false);

                initPlayerLogic(videoList, targetPosition);
            }
        });

        viewModel.loadAllCachedData();
    }

    private void initPlayerLogic(List<VideoBean> videoList, int initialPosition) {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                playVideoAtPosition(position, videoList);
            }
        });

        viewPager.post(() -> {
            playVideoAtPosition(initialPosition, videoList);
        });
    }

    private void playVideoAtPosition(int position, List<VideoBean> videoList) {
        if (position < 0 || position >= videoList.size() || adapter == null) return;

        // The transition logic is now moved to the listener, so we don't do it here.

        VideoBean vb = videoList.get(position);
        String uri = "android.resource://" + getPackageName() + "/" + vb.videoResId;

        PlayerManager.getInstance(this).prepareMedia(uri, true, 0);
        adapter.attachPlayerToHolder(position);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Player player = PlayerManager.getInstance(this).getPlayer();
        if (player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlayerManager.getInstance(this).releasePlayer();
    }
}
