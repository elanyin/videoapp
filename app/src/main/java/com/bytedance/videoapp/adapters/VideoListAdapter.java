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
 * 首页视频列表的 RecyclerView 适配器。
 * <p>
 * 职责:
 * 1.  为首页的 RecyclerView 提供视频卡片视图 (ViewHolder)。
 * 2.  将视频数据 (VideoBean) 绑定到每个卡片视图上，包括封面、标题、作者等。
 * 3.  管理数据列表，提供全量刷新 (setData) 和增量更新 (appendData) 的方法。
 * 4.  处理列表项的点击事件，并通过回调接口通知外部 (Activity)。
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    private List<VideoBean> mData = new ArrayList<>();
    private OnItemClickListener mListener;

    /**
     * 列表项点击事件的回调接口。
     */
    public interface OnItemClickListener {
        void onItemClick(VideoBean video, int position);
    }

    /**
     * 设置点击事件的监听器。
     * @param listener 监听器实例
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 设置并刷新整个列表的数据。此方法会清空旧数据。
     * @param list 新的视频数据列表
     */
    public void setData(List<VideoBean> list) {
        mData.clear();
        if (list != null) {
            mData.addAll(list);
        }
        // 使用 notifyDataSetChanged() 进行全量刷新。适用于初次加载或下拉刷新。
        notifyDataSetChanged();
    }

    /**
     * 在列表末尾追加数据。
     * @param list 要追加的视频数据列表
     */
    public void appendData(List<VideoBean> list) {
        if (list == null || list.isEmpty()) return;
        int start = mData.size();
        mData.addAll(list);
        // 使用 notifyItemRangeInserted() 进行增量更新，可以获得更好的性能和动画效果，
        // 避免了列表的整体闪烁和位置跳动。适用于上拉加载更多。
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

        // --- 数据绑定 ---
        holder.tvTitle.setText(video.title != null ? video.title : "");
        holder.tvAuthor.setText(video.author != null ? "@" + video.author : "");
        holder.tvLike.setText(video.likeCount != null ? video.likeCount : "0");

        // 使用 Glide 加载网络或本地图片资源
        Glide.with(holder.itemView.getContext())
                .load(video.coverResId)
                .into(holder.ivCover);

        Glide.with(holder.itemView.getContext())
                .load(video.avatarResId)
                .circleCrop() // 应用圆形裁剪
                .into(holder.ivAvatar);

        // --- 事件绑定 ---
        holder.itemView.setOnClickListener(v -> {
            // 使用 holder.getBindingAdapterPosition() 获取 item 在适配器中的最新位置，
            // 这是一个更安全的选择，可以避免因数据变动导致的 ViewHolder 位置与数据不一致的问题。
            int adapterPosition = holder.getBindingAdapterPosition();
            if (mListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                mListener.onItemClick(mData.get(adapterPosition), adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    /**
     * ViewHolder 定义，持有每个视频卡片的所有UI组件引用，以避免重复调用 findViewById。
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
