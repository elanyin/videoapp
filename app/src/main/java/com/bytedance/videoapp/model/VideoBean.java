package com.bytedance.videoapp.model;

public class VideoBean {

    public String title;      // 标题
    public String author;      // 作者名字
    public String likeCount;  // 点赞数

    public int coverResId;     // 封面图资源 ID (R.drawable.xxx)
    public int videoResId;     // 视频文件资源 ID (R.raw.xxx)
    public int avatarResId;    // 作者头像资源 ID (R.drawable.xxx)

    public VideoBean(String title, String author, String likeCount, int coverResId, int videoResId, int avatarResId) {
        this.title = title;
        this.author = author;
        this.likeCount = likeCount;
        this.coverResId = coverResId;
        this.videoResId = videoResId;
        this.avatarResId = avatarResId;
    }

}
