package com.bytedance.videoapp.model;

/**
 * 视频数据模型 (POJO / Bean)
 * <p>
 * 职责:
 * 这是一个简单的数据容器类，用于封装一个视频所需的所有属性。
 * 它不包含任何业务逻辑，只用于在程序的不同层之间传递数据。
 */
public class VideoBean {

    public String title;      // 视频标题
    public String author;      // 视频作者的名字
    public String likeCount;  // 视频的点赞数 (通常是格式化后的字符串，如 "1.2w")

    public int coverResId;     // 视频封面图的本地资源 ID (例如 R.drawable.cover_1)
    public int videoResId;     // 视频文件的本地资源 ID (例如 R.raw.video_1)
    public int avatarResId;    // 视频作者头像的本地资源 ID (例如 R.drawable.avatar_1)

    /**
     * 构造函数
     * @param title 标题
     * @param author 作者名
     * @param likeCount 点赞数
     * @param coverResId 封面图资源ID
     * @param videoResId 视频文件资源ID
     * @param avatarResId 头像资源ID
     */
    public VideoBean(String title, String author, String likeCount, int coverResId, int videoResId, int avatarResId) {
        this.title = title;
        this.author = author;
        this.likeCount = likeCount;
        this.coverResId = coverResId;
        this.videoResId = videoResId;
        this.avatarResId = avatarResId;
    }

}
