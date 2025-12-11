package com.bytedance.videoapp.repository;
import android.os.Handler;
import android.os.Looper;


import java.util.ArrayList;
import java.util.List;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.model.VideoBean;

public class VideoRepository {

    // 模拟从服务器异步获取数据
    public void fetchVideoList(int page, DataCallback<List<VideoBean>> callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<VideoBean> videoList = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                videoList.add(new VideoBean(
                        "深夜治愈：点燃一支香薰蜡烛，放空自己#氛围感",
                        "生活美学家",
                        "12.5w",
                        R.drawable.cover_1,
                        R.raw.video_1,
                        R.drawable.avatar_1
                ));

                videoList.add(new VideoBean(
                        "把车窗摇下来，风里都是自由的味道#公路旅行",
                        "公路日记",
                        "8900",
                        R.drawable.cover_2,
                        R.raw.video_2,
                        R.drawable.avatar_2
                ));

                videoList.add(new VideoBean(
                        "这片橘子海送给你，想去海边了嘛？",
                        "海岛听风",
                        "3.4w",
                        R.drawable.cover_3,
                        R.raw.video_3,
                        R.drawable.avatar_3
                ));

                videoList.add(new VideoBean(
                        "家里的猫半夜偷偷练琴，这难道是莫扎特转世？",
                        "喵星人观察员",
                        "2.1w",
                        R.drawable.cover_4,
                        R.raw.video_4,
                        R.drawable.avatar_4
                ));

                videoList.add(new VideoBean(
                        "今年的第一场雪，大家来一起看雪",
                        "北方的冬",
                        "5.3w",
                        R.drawable.cover_5,
                        R.raw.video_5,
                        R.drawable.avatar_5
                ));
            }

            // 数据获取成功，回调
            if (callback != null) {
                callback.onSuccess(videoList);
            }
        }, 1000);
    }

    // 回调接口
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String msg);
    }

}
