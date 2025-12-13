package com.bytedance.videoapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.videoapp.R;
import com.bytedance.videoapp.model.VideoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频列表adapter
 * 首页的 RecyclerView, 负责展示视频瀑布流列表
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    private List<VideoBean> mData = new ArrayList<>();
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(VideoBean video, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 刷新全量数据
     */
    public void setData(List<VideoBean> list) {
        if (list == null) {
            this.mData.clear();
        } else {
            this.mData.clear();
            this.mData.addAll(list);
        }
        notifyDataSetChanged();
    }

    /**
     * 追加数据，使用增量添加避免位置跳动
     */
    public void appendData(List<VideoBean> list) {
        if (list == null || list.isEmpty()) return;
        int start = mData.size();
        mData.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video_card, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        if (position < 0 || position >= mData.size()) {
            return;
        }

        VideoBean video = mData.get(position);
        if (video == null) {
            return;
        }

        // 绑定文本数据
        holder.tvTitle.setText(video.title != null ? video.title : "");
        holder.tvAuthor.setText(video.author != null ? video.author : "");
        holder.tvLike.setText(video.likeCount != null ? video.likeCount : "0");

        // 加载封面图
        Glide.with(holder.itemView.getContext())
                .load(video.coverResId)
                .into(holder.ivCover);

        // 加载头像
        Glide.with(holder.itemView.getContext())
                .load(video.avatarResId)
                .circleCrop()
                .into(holder.ivAvatar);

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (mListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                mListener.onItemClick(video, adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    /**
     * ViewHolder类
     */
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivCover;
        final ImageView ivAvatar;
        final TextView tvTitle;
        final TextView tvAuthor;
        final TextView tvLike;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvLike = itemView.findViewById(R.id.tv_like);
        }
    }
}
