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
import com.bytedance.videoapp.model.CommentBean;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<CommentBean> mData;

    public CommentAdapter(List<CommentBean> data) {
        this.mData = data;
    }

    public void addComment(CommentBean bean) {
        mData.add(0, bean); // 加到顶部
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentBean bean = mData.get(position);
        holder.tvName.setText(bean.authorName);
        holder.tvContent.setText(bean.content);
        holder.tvDate.setText(bean.date);
        holder.tvLikeCount.setText(bean.likeCount);
        // 这里可以加 Glide 加载头像逻辑
        Glide.with(holder.itemView.getContext())
                .load(bean.avatarResId)
                .circleCrop() // 圆形裁剪
                .into(holder.ivAvatar);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvDate, tvLikeCount;
        ImageView ivAvatar;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}