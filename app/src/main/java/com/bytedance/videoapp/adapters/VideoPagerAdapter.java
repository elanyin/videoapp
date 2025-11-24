package com.bytedance.videoapp.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.model.VideoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频全屏播放适配器
 * 用于ViewPager2中显示全屏视频
 */
public class VideoPagerAdapter extends RecyclerView.Adapter<VideoPagerAdapter.VideoViewHolder> {

    private List<VideoBean> mData = new ArrayList<>();

    public VideoPagerAdapter(List<VideoBean> data) {
        if (data != null) {
            this.mData = new ArrayList<>(data);
        }
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
        if (position < 0 || position >= mData.size()) {
            return;
        }

        VideoBean video = mData.get(position);
        if (video == null) {
            return;
        }

        // 设置文本信息
        holder.tvTitle.setText(video.title != null ? video.title : "");
        holder.tvAuthor.setText("@" + (video.author != null ? video.author : ""));

        // 设置视频播放路径
        String videoPath = "android.resource://" + holder.itemView.getContext().getPackageName() 
                + "/" + video.videoResId;
        holder.videoView.setVideoURI(Uri.parse(videoPath));

        // 点击屏幕切换播放/暂停
        holder.itemView.setOnClickListener(v -> {
            if (holder.videoView.isPlaying()) {
                holder.videoView.pause();
            } else {
                holder.videoView.start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        // 回收时停止播放，避免资源浪费
        if (holder.videoView.isPlaying()) {
            holder.videoView.stopPlayback();
        }
    }

    /**
     * ViewHolder类
     */
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        final VideoView videoView;
        final TextView tvTitle;
        final TextView tvAuthor;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            tvTitle = itemView.findViewById(R.id.tv_title_full);
            tvAuthor = itemView.findViewById(R.id.tv_author);
        }
    }
}
