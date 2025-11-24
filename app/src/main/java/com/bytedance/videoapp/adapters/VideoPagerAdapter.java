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

import java.util.List;

public class VideoPagerAdapter extends RecyclerView.Adapter<VideoPagerAdapter.VideoViewHolder>{

    private List<VideoBean> mData;

    public VideoPagerAdapter(List<VideoBean> data) {
        this.mData = data;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载全屏视频的布局 item_video_full.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_full, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoBean video = mData.get(position);

        // 设置全屏页面的文字
        holder.tvTitle.setText(video.title);
        holder.tvAuthor.setText("@" + video.author);

        // 设置视频播放路径
        String videoPath = "android.resource://" + holder.itemView.getContext().getPackageName() + "/" + video.videoResId;
        holder.videoView.setVideoURI(Uri.parse(videoPath));

        // 点击屏幕：暂停/播放
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
        return mData.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        VideoView videoView;
        TextView tvTitle, tvAuthor;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            // 确保 item_video_full.xml 里有这些 ID
            videoView = itemView.findViewById(R.id.videoView);
            tvTitle = itemView.findViewById(R.id.tv_title_full);
            tvAuthor = itemView.findViewById(R.id.tv_author);
        }
    }

}
