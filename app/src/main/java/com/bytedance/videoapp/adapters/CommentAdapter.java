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

/**
 * 评论列表的 RecyclerView 适配器。
 * <p>
 * 职责:
 * 1.  为评论区的 RecyclerView 提供每一条评论的视图 (ViewHolder)。
 * 2.  将评论数据 (CommentBean) 绑定到每个列表项上。
 * 3.  提供在列表顶部添加新评论的功能。
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<CommentBean> mData;

    /**
     * 构造函数
     * @param data 初始评论数据列表
     */
    public CommentAdapter(List<CommentBean> data) {
        this.mData = data;
    }

    /**
     * 添加一条新评论到列表顶部。
     * @param bean 要添加的评论对象
     */
    public void addComment(CommentBean bean) {
        if (mData != null) {
            // 在数据列表的第一个位置插入新评论
            mData.add(0, bean);
            // 通知适配器在位置0插入了一个新项，这会有动画效果
            notifyItemInserted(0);
        }
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
        
        // --- 数据绑定 ---
        holder.tvName.setText(bean.authorName);
        holder.tvContent.setText(bean.content);
        holder.tvDate.setText(bean.date);
        holder.tvLikeCount.setText(bean.likeCount);
        
        // 使用 Glide 加载头像
        Glide.with(holder.itemView.getContext())
                .load(bean.avatarResId)
                .circleCrop() // 应用圆形裁剪
                .into(holder.ivAvatar);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    /**
     * ViewHolder 定义，持有每个评论项的所有UI组件引用。
     */
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
