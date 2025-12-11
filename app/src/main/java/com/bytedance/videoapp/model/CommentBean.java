package com.bytedance.videoapp.model;

public class CommentBean {
    public String id;
    public String content;     // 评论内容
    public String authorName;  // 用户名
    public int avatarResId;   // 头像
    public String date;        // 时间 (如 "10分钟前")
    public String likeCount;   // 点赞数
    public boolean isLiked;    // 是否已点赞

    public CommentBean(String content, String authorName, String date, String likeCount, int avatarResId) {
        this.content = content;
        this.authorName = authorName;
        this.date = date;
        this.likeCount = likeCount;
        this.avatarResId = avatarResId;
    }
}