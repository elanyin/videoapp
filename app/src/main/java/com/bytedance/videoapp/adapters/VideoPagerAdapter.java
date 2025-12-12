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
    private int attachedPosition = -1;
    private Player.Listener renderListener;
    private RecyclerView recyclerView;

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

        holder.ivCover.setVisibility(View.VISIBLE);
        Glide.with(holder.itemView.getContext())
                .load(video.coverResId)
                .into(holder.ivCover);

        Glide.with(holder.itemView.getContext())
                .load(video.avatarResId)
                .circleCrop()
                .into(holder.ivAvatar);

        holder.tvLikeCount.setText(video.likeCount);
        holder.tvCommentCount.setText("289");

        GestureDetector gestureDetector = new GestureDetector(holder.itemView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(@NonNull MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                        int currentPos = holder.getBindingAdapterPosition();
                        if (currentPos == RecyclerView.NO_POSITION) return false;

                        if (attachedPosition == currentPos) {
                            if (player.isPlaying()) {
                                player.pause();
                                if (holder.ivPlayIcon != null) {
                                    holder.ivPlayIcon.setVisibility(View.VISIBLE);
                                    holder.ivPlayIcon.animate().scaleX(1f).scaleY(1f).start();
                                }
                            } else {
                                player.play();
                                if (holder.ivPlayIcon != null) {
                                    holder.ivPlayIcon.animate().scaleX(0f).scaleY(0f)
                                            .withEndAction(() -> holder.ivPlayIcon.setVisibility(View.GONE)).start();
                                }
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(@NonNull MotionEvent e) {
                        int currentPos = holder.getBindingAdapterPosition();
                        if (currentPos == RecyclerView.NO_POSITION) return false;

                        showLoveAnim(holder.rootView, e.getRawX(), e.getRawY());
                        holder.ivLike.setImageResource(R.drawable.ic_heart_red);

                        if (doubleClickLikeListener != null) {
                            if (currentPos >= 0 && currentPos < mData.size()) {
                                VideoBean currentVideo = mData.get(currentPos);
                                doubleClickLikeListener.onDoubleClickLike(currentVideo, currentPos);
                            }
                        }

                        return true;
                    }
                });

        holder.itemView.setClickable(true);
        holder.itemView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

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
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).start();
                Toast.makeText(v.getContext(), "点击了作者: " + video.author, Toast.LENGTH_SHORT).show();
            }).start();
        });

        holder.ivFollow.setOnClickListener(v -> {
            v.animate().scaleX(0f).scaleY(0f).setDuration(200).start();
            Toast.makeText(v.getContext(), "关注了作者: " + video.author, Toast.LENGTH_SHORT).show();
        });

        holder.ivComment.setOnClickListener(v -> {
            if (commentListener != null) {
                commentListener.onCommentClick(video);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public interface OnFirstFrameRenderedListener {
        void onFirstFrameRendered();
    }

    private OnFirstFrameRenderedListener firstFrameRenderedListener;

    public void setOnFirstFrameRenderedListener(OnFirstFrameRenderedListener listener) {
        this.firstFrameRenderedListener = listener;
    }

    public interface OnCommentClickListener {
        void onCommentClick(VideoBean video);
    }

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
        if (holder.playerView.getPlayer() == player) {
            holder.playerView.setPlayer(null);
            attachedPosition = -1;
        }
    }

    public void attachPlayerToHolder(int position) {
        if (recyclerView == null) return;
        if (position < 0 || position >= getItemCount()) return;

        if (attachedPosition != -1 && attachedPosition != position) {
            RecyclerView.ViewHolder old = recyclerView.findViewHolderForAdapterPosition(attachedPosition);
            if (old instanceof VideoViewHolder) {
                ((VideoViewHolder) old).playerView.setPlayer(null);
                ((VideoViewHolder) old).ivCover.setVisibility(View.VISIBLE);
            }
        }

        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
        if (vh instanceof VideoViewHolder) {
            VideoViewHolder holder = (VideoViewHolder) vh;

            holder.playerView.setPlayer(player);
            holder.ivCover.setAlpha(1f);
            holder.ivCover.setVisibility(View.VISIBLE);

            if (renderListener != null) {
                player.removeListener(renderListener);
            }
            renderListener = new Player.Listener() {
                @Override
                public void onRenderedFirstFrame() {
                    // Hide the ViewHolder's cover
                    if (holder.getAdapterPosition() == attachedPosition) {
                        holder.ivCover.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction(() -> holder.ivCover.setVisibility(View.GONE))
                                .start();
                    }
                    // Notify the Activity
                    if (firstFrameRenderedListener != null) {
                        firstFrameRenderedListener.onFirstFrameRendered();
                    }
                }
            };
            player.addListener(renderListener);

            attachedPosition = position;
        }
    }

    private void showLoveAnim(ViewGroup parentView, float x, float y) {
        Context context = parentView.getContext();
        ImageView loveIv = new ImageView(context);
        loveIv.setImageResource(R.drawable.ic_heart_red);

        int size = (int) (100 * context.getResources().getDisplayMetrics().density);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(size, size);
        loveIv.setLayoutParams(params);

        int[] parentLocation = new int[2];
        parentView.getLocationOnScreen(parentLocation);

        float relativeX = x - parentLocation[0];
        float relativeY = y - parentLocation[1];

        loveIv.setX(relativeX - size / 2f);
        loveIv.setY(relativeY - size / 2f);

        parentView.addView(loveIv);

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
