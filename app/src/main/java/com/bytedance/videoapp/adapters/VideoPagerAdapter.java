package com.bytedance.videoapp.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.bytedance.videoapp.player.PlayerManager;
import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@UnstableApi
public class VideoPagerAdapter extends RecyclerView.Adapter<VideoPagerAdapter.VideoViewHolder> {

    private List<VideoBean> mData = new ArrayList<>();
    private final PlayerManager playerManager;
    private final ExoPlayer player;
    // 记录当前 attach 的 position（视图可见且绑定了 player）
    private int attachedPosition = -1;
    private Player.Listener renderListener;

    public VideoPagerAdapter(List<VideoBean> data, android.content.Context context) {
        if (data != null) this.mData = new ArrayList<>(data);
        playerManager = PlayerManager.getInstance(context);
        player = playerManager.getPlayer();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video_full, parent, false);
        return new VideoViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        if (position < 0 || position >= mData.size()) return;

        VideoBean video = mData.get(position);
        holder.tvTitle.setText(video.title);
        holder.tvAuthor.setText("@" + video.author);

        // 加载封面图，防止黑屏
        holder.ivCover.setVisibility(View.VISIBLE);
        Glide.with(holder.itemView.getContext())
                .load(video.coverResId)
                .into(holder.ivCover);

        // 设置头像
        Glide.with(holder.itemView.getContext())
                .load(video.avatarResId)
                .circleCrop()
                .into(holder.ivAvatar);

        // 设置点赞数
        holder.tvLikeCount.setText(video.likeCount);

        // 设置评论数
        holder.tvCommentCount.setText("289");

        // 手势检测器
        GestureDetector gestureDetector = new GestureDetector(holder.itemView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(@NonNull MotionEvent e) {
                        return true;
                    }

                    // 处理单击事件 (替代原来的 setOnClickListener)
                    @Override
                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {

                        int currentPos = holder.getBindingAdapterPosition();

                        if (currentPos == RecyclerView.NO_POSITION) return false;

                        // 只有当前播放的 item 才响应点击播放/暂停
                        if (attachedPosition == currentPos) {
                            if (player.isPlaying()) {
                                player.pause();
                                // 显示暂停图标 (假设你有这个逻辑)
                                if (holder.ivPlayIcon != null) {
                                    holder.ivPlayIcon.setVisibility(View.VISIBLE);
                                    holder.ivPlayIcon.animate().scaleX(1f).scaleY(1f).start();
                                }
                            } else {
                                player.play();
                                // 隐藏暂停图标
                                if (holder.ivPlayIcon != null) {
                                    holder.ivPlayIcon.animate().scaleX(0f).scaleY(0f)
                                            .withEndAction(() -> holder.ivPlayIcon.setVisibility(View.GONE)).start();
                                }
                            }
                        }
                        return true;
                    }

                    // 处理双击事件
                    @Override
                    public boolean onDoubleTap(@NonNull MotionEvent e) {
                        int currentPos = holder.getBindingAdapterPosition();
                        if (currentPos == RecyclerView.NO_POSITION) return false;

                        // (A) 在手指点击的位置执行爱心动画
                        // e.getX() 和 e.getY() 是手指在屏幕上的坐标
                        showLoveAnim(holder.rootView, e.getRawX(), e.getRawY());

                        // (B) 改变侧边栏点赞图标的状态
                        holder.ivLike.setImageResource(R.drawable.ic_heart_red);

                        // (C) 通知外部 (Activity/ViewModel) 更新数据
                        if (doubleClickLikeListener != null) {
                            if (currentPos >= 0 && currentPos < mData.size()) {
                                VideoBean currentVideo = mData.get(currentPos);
                                doubleClickLikeListener.onDoubleClickLike(currentVideo, currentPos);
                            }
                        }

                        return true; // 事件已处理
                    }
                });

        holder.itemView.setClickable(true);
        holder.itemView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));


        // 点击暂停/播放
//        holder.itemView.setOnClickListener(v -> {
//            if (attachedPosition == position) {
//                if (player.isPlaying()) {
//                    player.pause();
//                    holder.ivPlayIcon.setVisibility(View.VISIBLE);
//                    holder.ivPlayIcon.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
//                }
//                else {
//                    player.play();
//                    holder.ivPlayIcon.animate().scaleX(0f).scaleY(0f).setDuration(200)
//                            .withEndAction(() -> holder.ivPlayIcon.setVisibility(View.GONE))
//                            .start();
//                }
//            }
//        });

        // 点赞按钮点击变红效果（纯UI，不含双击逻辑）
        holder.ivLike.setOnClickListener(v -> {
            // 简单切换状态
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                ((ImageView)v).setImageResource(R.drawable.ic_heart_red);
                // 简单动画
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(()->
                        v.animate().scaleX(1f).scaleY(1f).start()
                ).start();
            } else {
                ((ImageView)v).setImageResource(R.drawable.ic_heart_white);
            }
        });

        // 头像点击事件
        holder.ivAvatar.setOnClickListener(v -> {
            // 这里可以加一个简单的缩放反馈动画
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).start();
                // TODO: 跳转到 UserProfileActivity
                Toast.makeText(v.getContext(), "点击了作者: " + video.author, Toast.LENGTH_SHORT).show();
            }).start();
        });

        // 关注按钮逻辑（模拟）
        // 点击后消失，表示已关注
        holder.ivFollow.setOnClickListener(v -> {
            v.animate().scaleX(0f).scaleY(0f).setDuration(200).start();
            Toast.makeText(v.getContext(), "关注了作者: " + video.author, Toast.LENGTH_SHORT).show();
        });

        // 点击评论
        holder.ivComment.setOnClickListener(v -> {
            if (commentListener != null) {
                commentListener.onCommentClick(video);
            }
        });
    }

    // 定义点击评论接口
    public interface OnCommentClickListener {
        void onCommentClick(VideoBean video);
    }

    // 定义双击点赞接口
    public interface OnDoubleClickLikeListener {
        void onDoubleClickLike(VideoBean video, int position);
    }

    private OnCommentClickListener commentListener;
    private OnDoubleClickLikeListener doubleClickLikeListener;

    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.commentListener = listener;
    }

    public void setDoubleClickLikeListener(OnDoubleClickLikeListener listener) {
        this.doubleClickLikeListener = listener;
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        // 回收视图时若 player 绑定在上面，解绑
        if (holder.playerView.getPlayer() == player) {
            holder.playerView.setPlayer(null);
            attachedPosition = -1;
        }
    }


    // 把 player attach 到某个 holder
    public void attachPlayerToHolder(RecyclerView recyclerView, int position) {
        // 防止越界
        if (position < 0 || position >= getItemCount()) return;

        // 1. 先解绑旧的
        if (attachedPosition != -1 && attachedPosition != position) {
            RecyclerView.ViewHolder old = recyclerView.findViewHolderForAdapterPosition(attachedPosition);
            if (old instanceof VideoViewHolder) {
                ((VideoViewHolder) old).playerView.setPlayer(null);
                // 恢复旧视图的封面显示，以免滑回来时黑屏
                ((VideoViewHolder) old).ivCover.setVisibility(View.VISIBLE);
            }
        }

        // 2. 绑定新的
        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
        if (vh instanceof VideoViewHolder) {
            VideoViewHolder holder = (VideoViewHolder) vh;

            // 绑定播放器
            holder.playerView.setPlayer(player);
            holder.ivCover.setAlpha(1f);
            holder.ivCover.setVisibility(View.VISIBLE);

            // 确保仅保留一个渲染监听，避免快速滑动时重复添加
            if (renderListener != null) {
                player.removeListener(renderListener);
            }
            renderListener = new Player.Listener() {
                @Override
                public void onRenderedFirstFrame() {
                    if (holder.getAdapterPosition() == attachedPosition) {
                        holder.ivCover.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction(() -> holder.ivCover.setVisibility(View.GONE))
                                .start();
                    }
                }
            };
            player.addListener(renderListener);

            attachedPosition = position;
        }
    }

    /**
     * 在指定位置动态添加并展示爱心动画
     */
    private void showLoveAnim(ViewGroup parentView, float x, float y) {
        Context context = parentView.getContext();
        // 1. 动态创建一个 ImageView
        ImageView loveIv = new ImageView(context);
        loveIv.setImageResource(R.drawable.ic_heart_red);

        // 2. 设置爱心的大小
        int size = (int) (100 * context.getResources().getDisplayMetrics().density);
        // 注意：这里要根据父布局类型设置 LayoutParams
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(size, size);
        loveIv.setLayoutParams(params);

        // 3. 计算精准位置

        // 获取父布局在屏幕上的绝对坐标
        int[] parentLocation = new int[2];
        parentView.getLocationOnScreen(parentLocation);

        // 计算点击点相对于父布局的坐标
        float relativeX = x - parentLocation[0];
        float relativeY = y - parentLocation[1];

        // 设置爱心的位置 (让爱心中心精准对准点击点)
        // setX/setY 设置的是 View 左上角的位置，所以要减去宽高的一半来实现居中
        loveIv.setX(relativeX - size / 2f);
        loveIv.setY(relativeY - size / 2f);

        // 4. 添加到父布局
        parentView.addView(loveIv);

        // 5. 准备动画
        AnimatorSet animatorSet = new AnimatorSet();

        // 随机旋转角度 (-30度 到 30度)
        float randomRotate = new Random().nextInt(60) - 30;

        // 缩放动画：快速放大再稍微回弹
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(loveIv, "scaleX", 0.8f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(loveIv, "scaleY", 0.8f, 1.2f, 1f);
        // 旋转动画
        ObjectAnimator rotate = ObjectAnimator.ofFloat(loveIv, "rotation", 0f, randomRotate);
        // 透明度动画：先保持不透明，最后快速消失
        ObjectAnimator alpha = ObjectAnimator.ofFloat(loveIv, "alpha", 1f, 1f, 0f);
        // 位移动画：向上飘动
        ObjectAnimator translationY = ObjectAnimator.ofFloat(loveIv, "translationY",
                loveIv.getTranslationY(), loveIv.getTranslationY() - 150f);

        animatorSet.playTogether(scaleX, scaleY, rotate, alpha, translationY);
        animatorSet.setDuration(800); // 动画总时长

        // 6. 动画结束后移除 View，防止内存溢出
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                parentView.removeView(loveIv);
            }
        });

        animatorSet.start();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        final PlayerView playerView;
        final ImageView ivCover;
        final TextView tvTitle;
        final TextView tvAuthor;
        final ImageView ivPlayIcon;
        final TextView tvLikeCount;
        final TextView tvCommentCount;
        final ImageView ivLike;
        final ImageView ivComment;
        final ImageView ivShare;
        final ImageView ivFollow;
        final ImageView ivAvatar;
        final ConstraintLayout rootView;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.player_view);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title_full);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            ivPlayIcon = itemView.findViewById(R.id.iv_play_icon);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivComment = itemView.findViewById(R.id.iv_comment);
            ivShare = itemView.findViewById(R.id.iv_share);
            ivFollow = itemView.findViewById(R.id.iv_follow);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            rootView = itemView.findViewById(R.id.root_view);
        }
    }
}
