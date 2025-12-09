package com.bytedance.videoapp.model;

public class CommentBean {
    public String content;
    public String authorName;
    public String avatarUrl; // 实际项目中用url，这里可用resId
    public long timestamp;
    public boolean isSelf; // 标记是否是自己发的

    public CommentBean(String content, String authorName, boolean isSelf) {
        this.content = content;
        this.authorName = authorName;
        this.isSelf = isSelf;
        this.timestamp = System.currentTimeMillis();
    }
}