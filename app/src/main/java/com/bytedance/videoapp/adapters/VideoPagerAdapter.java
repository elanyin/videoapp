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

/**
 * 视频详情页的 ViewPager2 适配器
 * <p>
 * 职责:
 * 1.  为 ViewPager2 提供每个视频页面的视图 (ViewHolder)。
 * 2.  绑定视频的基本信息，如标题、作者、封面图等。
 * 3.  **核心职责**: 管理全局唯一的 ExoPlayer 实例与 ViewHolder 的动态绑定(attach)和解绑(detach)。
 * 4.  监听播放器事件（如第一帧渲染），并通知 Activity 以实现复杂的UI同步（如解决黑屏问题）。
 * 5.  处理用户交互，如单击暂停/播放、双击点赞等。
 */
@UnstableApi
public class VideoPagerAdapter extends RecyclerView.Adapter<VideoPagerAdapter.VideoViewHolder> {

    private List<VideoBean> mData = new ArrayList<>();
    private final ExoPlayer player;

    // 记录当前播放器附着(attach)的 ViewHolder 位置。-1表示未附着。
    private int attachedPosition = -1;
    // 播放器渲染监听器，用于在视频第一帧画面出现时执行UI操作。
    private Player.Listener renderListener;
    // 持有的 RecyclerView 实例，用于安全地查找 ViewHolder
    private RecyclerView recyclerView;

    /**
     * 构造函数
     * @param data 视频数据列表
     * @param context 上下文
     */
    public VideoPagerAdapter(List<VideoBean> data, android.content.Context context) {
        if (data != null) this.mData = new ArrayList<>(data);
        // 从 PlayerManager 获取全局唯一的播放器实例
        this.player = PlayerManager.getInstance(context).getPlayer();
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
        // 标准的数据绑定流程
        if (position < 0 || position >= mData.size()) return;

        VideoBean video = mData.get(position);
        holder.tvTitle.setText(video.title);
        holder.tvAuthor.setText("@" + video.author);

        // 关键：在绑定时，总是先显示封面图。这能防止快速滑动时出现黑屏。
        holder.ivCover.setVisibility(View.VISIBLE);
        Glide.with(holder.itemView.getContext())
                .load(video.coverResId)
                .into(holder.ivCover);

        Glide.with(holder.itemView.getContext())
                .load(video.avatarResId)
                .circleCrop()
                .into(holder.ivAvatar);

        holder.tvLikeCount.setText(video.likeCount);
        holder.tvCommentCount.setText("289"); // 模拟数据

        // --- 手势处理 ---
        GestureDetector gestureDetector = new GestureDetector(holder.itemView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(@NonNull MotionEvent e) {
                        return true; // 必须返回true，后续手势才能被识别
                    }

                    // 处理单击事件：播放/暂停
                    @Override
                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                        int currentPos = holder.getBindingAdapterPosition();
                        if (currentPos == RecyclerView.NO_POSITION) return false;

                        // 只有当播放器正附着在当前 ViewHolder 上时，才响应播放/暂停操作
                        if (attachedPosition == currentPos) {
                            if (player.isPlaying()) {
                                player.pause();
                                holder.ivPlayIcon.setVisibility(View.VISIBLE);
                                holder.ivPlayIcon.animate().scaleX(1f).scaleY(1f).start();
                            } else {
                                player.play();
                                holder.ivPlayIcon.animate().scaleX(0f).scaleY(0f)
                                        .withEndAction(() -> holder.ivPlayIcon.setVisibility(View.GONE)).start();
                            }
                        }
                        return true;
                    }

                    // 处理双击事件：点赞
                    @Override
                    public boolean onDoubleTap(@NonNull MotionEvent e) {
                        int currentPos = holder.getBindingAdapterPosition();
                        if (currentPos == RecyclerView.NO_POSITION) return false;

                        showLoveAnim(holder.rootView, e.getRawX(), e.getRawY());
                        holder.ivLike.setImageResource(R.drawable.ic_heart_red);

                        if (doubleClickLikeListener != null) {
                            VideoBean currentVideo = mData.get(currentPos);
                            doubleClickLikeListener.onDoubleClickLike(currentVideo, currentPos);
                        }
                        return true;
                    }
                });

        holder.itemView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // --- 其他点击事件 ---
        // 在 ViewHolder 中设置监听器时，应始终从 getBindingAdapterPosition() 获取当前项的最新位置，
        // 以避免因 ViewHolder 复用而导致的数据错乱问题。
        holder.ivLike.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                ((ImageView)v).setImageResource(R.drawable.ic_heart_red);
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(()->
                        v.animate().scaleX(1f).scaleY(1f).start()
                ).start();
            } else {
                ((ImageView)v).setImageResource(R.drawable.ic_heart_white);
            }
        });

        holder.ivAvatar.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if(currentPos != RecyclerView.NO_POSITION) {
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).start();
                    Toast.makeText(v.getContext(), "点击了作者: " + mData.get(currentPos).author, Toast.LENGTH_SHORT).show();
                }).start();
            }
        });

        holder.ivFollow.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if(currentPos != RecyclerView.NO_POSITION) {
                v.animate().scaleX(0f).scaleY(0f).setDuration(200).start();
                Toast.makeText(v.getContext(), "关注了作者: " + mData.get(currentPos).author, Toast.LENGTH_SHORT).show();
            }
        });

        holder.ivComment.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if(currentPos != RecyclerView.NO_POSITION) {
                if (commentListener != null) {
                    commentListener.onCommentClick(mData.get(currentPos));
                }
            }
        });
    }

    /**
     * RecyclerView 的回调，当 Adapter 附加到 RecyclerView 时调用。
     * 在这里获取并保存 RecyclerView 的引用，是为了后续能安全地通过 findViewHolderForAdapterPosition 查找 ViewHolder，
     * 避免了之前从外部传入 RecyclerView 实例可能引发的各种问题。
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    // --- 回调接口定义 ---

    /** 首次渲染回调接口，用于通知Activity播放器已开始渲染画面 */
    public interface OnFirstFrameRenderedListener {
        void onFirstFrameRendered();
    }
    private OnFirstFrameRenderedListener firstFrameRenderedListener;
    public void setOnFirstFrameRenderedListener(OnFirstFrameRenderedListener listener) {
        this.firstFrameRenderedListener = listener;
    }

    /** 评论区点击接口 */
    public interface OnCommentClickListener { void onCommentClick(VideoBean video); }
    private OnCommentClickListener commentListener;
    public void setOnCommentClickListener(OnCommentClickListener listener) { this.commentListener = listener; }

    /** 双击点赞接口 */
    public interface OnDoubleClickLikeListener { void onDoubleClickLike(VideoBean video, int position); }
    private OnDoubleClickLikeListener doubleClickLikeListener;
    public void setDoubleClickLikeListener(OnDoubleClickLikeListener listener) { this.doubleClickLikeListener = listener; }


    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    /**
     * 当一个 ViewHolder 被回收时调用。这是优化内存的关键。
     */
    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        // 如果被回收的 ViewHolder 正好是当前播放器附着的那个
        if (holder.playerView.getPlayer() == player && holder.getBindingAdapterPosition() == attachedPosition) {
            // 解绑播放器，并将 attachedPosition 重置
            holder.playerView.setPlayer(null);
            attachedPosition = -1;
        }
    }

    /**
     * 将播放器内核 (ExoPlayer) 附加到指定位置的 ViewHolder 上。
     * 这是整个类的核心逻辑。
     * @param position 要附加播放器的位置
     */
    public void attachPlayerToHolder(int position) {
        if (recyclerView == null) return;
        if (position < 0 || position >= getItemCount()) return;

        // 1. 如果播放器已附着在其他 ViewHolder 上，先解绑旧的
        if (attachedPosition != -1 && attachedPosition != position) {
            RecyclerView.ViewHolder oldVh = recyclerView.findViewHolderForAdapterPosition(attachedPosition);
            if (oldVh instanceof VideoViewHolder) {
                // 从旧的 PlayerView 上解绑 player
                ((VideoViewHolder) oldVh).playerView.setPlayer(null);
                // 【关键】恢复旧 ViewHolder 的封面图显示，防止滑回来时出现黑屏
                ((VideoViewHolder) oldVh).ivCover.setVisibility(View.VISIBLE);
            }
        }

        // 2. 找到新的 ViewHolder 并附着播放器
        RecyclerView.ViewHolder newVh = recyclerView.findViewHolderForAdapterPosition(position);
        if (newVh instanceof VideoViewHolder) {
            VideoViewHolder holder = (VideoViewHolder) newVh;

            // 绑定播放器到新的 PlayerView
            holder.playerView.setPlayer(player);
            // 确保封面图可见（即使之前被隐藏了）
            holder.ivCover.setAlpha(1f);
            holder.ivCover.setVisibility(View.VISIBLE);

            // 3. 设置第一帧渲染监听器
            // 确保每次都移除旧的监听器，防止内存泄漏和重复回调
            if (renderListener != null) {
                player.removeListener(renderListener);
            }
            renderListener = new Player.Listener() {
                @Override
                public void onRenderedFirstFrame() {
                    // 视频开始渲染，可以隐藏当前 ViewHolder 的封面图了
                    if (holder.getBindingAdapterPosition() == attachedPosition) {
                        holder.ivCover.animate()
                                .alpha(0f)
                                .setDuration(200) // 淡出动画时长200ms
                                .withEndAction(() -> holder.ivCover.setVisibility(View.GONE))
                                .start();
                    }
                    // 同时，通知 Activity，让它可以执行从“临时封面”到“播放器”的过渡动画
                    if (firstFrameRenderedListener != null) {
                        firstFrameRenderedListener.onFirstFrameRendered();
                    }
                }
            };
            player.addListener(renderListener);

            // 4. 更新当前附着的位置
            attachedPosition = position;
        }
    }

    /**
     * 在指定位置动态添加并展示爱心动画。
     * @param parentView 动画的父容器
     * @param x 点击的屏幕绝对 x 坐标
     * @param y 点击的屏幕绝对 y 坐标
     */
    private void showLoveAnim(ViewGroup parentView, float x, float y) {
        Context context = parentView.getContext();
        // 1. 动态创建一个 ImageView
        ImageView loveIv = new ImageView(context);
        loveIv.setImageResource(R.drawable.ic_heart_red);

        // 2. 设置爱心的大小
        int size = (int) (100 * context.getResources().getDisplayMetrics().density);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(size, size);
        loveIv.setLayoutParams(params);

        // 3. 计算精准位置
        int[] parentLocation = new int[2];
        parentView.getLocationOnScreen(parentLocation);
        float relativeX = x - parentLocation[0];
        float relativeY = y - parentLocation[1];
        loveIv.setX(relativeX - size / 2f);
        loveIv.setY(relativeY - size / 2f);

        // 4. 添加到父布局
        parentView.addView(loveIv);

        // 5. 准备动画
        AnimatorSet animatorSet = new AnimatorSet();
        float randomRotate = new Random().nextInt(60) - 30;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(loveIv, "scaleX", 0.8f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(loveIv, "scaleY", 0.8f, 1.2f, 1f);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(loveIv, "rotation", 0f, randomRotate);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(loveIv, "alpha", 1f, 1f, 0f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(loveIv, "translationY",
                loveIv.getTranslationY(), loveIv.getTranslationY() - 150f);
        animatorSet.playTogether(scaleX, scaleY, rotate, alpha, translationY);
        animatorSet.setDuration(800);

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

    /**
     * ViewHolder 定义，持有每个视频页面的所有UI组件引用。
     */
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        final PlayerView playerView;
        final ImageView ivCover;
        final TextView tvTitle, tvAuthor, tvLikeCount, tvCommentCount;
        final ImageView ivPlayIcon, ivLike, ivComment, ivShare, ivFollow, ivAvatar;
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
