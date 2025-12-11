package com.bytedance.videoapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

@UnstableApi
public class VideoPagerAdapter extends RecyclerView.Adapter<VideoPagerAdapter.VideoViewHolder> {

    private List<VideoBean> mData = new ArrayList<>();
    private final PlayerManager playerManager;
    private final ExoPlayer player;
    // 记录当前 attach 的 position（视图可见且绑定了 player）
    private int attachedPosition = -1;

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

        // 点击暂停/播放
        holder.itemView.setOnClickListener(v -> {
            if (attachedPosition == position) {
                if (player.isPlaying()) {
                    player.pause();
                    holder.ivPlayIcon.setVisibility(View.VISIBLE);
                    holder.ivPlayIcon.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                }
                else {
                    player.play();
                    holder.ivPlayIcon.animate().scaleX(0f).scaleY(0f).setDuration(200)
                            .withEndAction(() -> holder.ivPlayIcon.setVisibility(View.GONE))
                            .start();
                }
            }
        });

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

    private OnCommentClickListener commentListener;
    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.commentListener = listener;
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

            // 监听视频渲染，画面出来后再隐藏封面
            player.addListener(new Player.Listener() {
                @Override
                public void onRenderedFirstFrame() {
                    // 只有当当前holder还是绑定的那个时，才隐藏封面
                    if (holder.getAdapterPosition() == attachedPosition) {
                        // 渐隐动画
                        holder.ivCover.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction(() -> holder.ivCover.setVisibility(View.GONE))
                                .start();
                    }
                    // 移除监听，防止内存泄漏和重复回调
                    player.removeListener(this);
                }
            });

            attachedPosition = position;
        }
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
        }
    }
}
