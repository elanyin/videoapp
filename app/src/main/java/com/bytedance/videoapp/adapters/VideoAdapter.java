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

// 负责把数据填进 item_video_card.xml
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoBean> mData = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(VideoBean video, int position);
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    // 让 MainActivity 把假数据传进来
    public void setData(List<VideoBean> list) {
        this.mData = list;
        notifyDataSetChanged(); // 告诉列表数据变了
    }

    @NonNull
    @Override
    // 创建子布局
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载卡片布局 item_video_card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_card, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    // 绑定数据
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoBean videoBean = mData.get(position);
        holder.tvTitle.setText(videoBean.title);
        holder.tvAuthor.setText(videoBean.author);
        holder.tvLike.setText(videoBean.likeCount);

        Glide.with(holder.itemView.getContext())
                .load(videoBean.coverResId)
                .into(holder.ivCover);

        Glide.with(holder.itemView.getContext())
                .load(videoBean.avatarResId)
                .circleCrop()
                .into(holder.ivAvatar);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(videoBean, holder.getAdapterPosition());
            }
        });


    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    // viewHolder就写成内部类了
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ivAvatar, ivPlaceholder;
        TextView tvTitle, tvAuthor, tvLike;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            // 这里的 ID 必须和 item_video_card.xml 里的一致
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvLike = itemView.findViewById(R.id.tv_like);
            ivPlaceholder = itemView.findViewById(R.id.iv_placeholder);
        }
    }
}
