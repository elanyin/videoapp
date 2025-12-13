package com.bytedance.videoapp.model;

/**
 * 评论数据模型 (POJO / Bean)
 * <p>
 * 职责:
 * 这是一个简单的数据容器类，用于封装一条评论所需的所有属性。
 * 它不包含任何业务逻辑，只用于在程序的不同层之间传递数据。
 */
public class CommentBean {
    public String id;          // 评论的唯一ID
    public String content;     // 评论内容
    public String authorName;  // 评论作者的用户名
    public int avatarResId;   // 评论作者的头像资源ID
    public String date;        // 评论发布的时间描述 (例如 "10分钟前")
    public String likeCount;   // 评论的点赞数
    public boolean isLiked;    // 当前用户是否已点赞该评论

    /**
     * 构造函数
     * @param content 评论内容
     * @param authorName 作者名
     * @param date 时间描述
     * @param likeCount 点赞数
     * @param avatarResId 头像资源ID
     */
    public CommentBean(String content, String authorName, String date, String likeCount, int avatarResId) {
        this.content = content;
        this.authorName = authorName;
        this.date = date;
        this.likeCount = likeCount;
        this.avatarResId = avatarResId;
    }
}
