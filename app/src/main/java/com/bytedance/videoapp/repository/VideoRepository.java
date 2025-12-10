package com.bytedance.videoapp.repository;
import android.os.Handler;
import android.os.Looper;


import java.util.ArrayList;
import java.util.List;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.model.VideoBean;

public class VideoRepository {

    // 模拟从服务器异步获取数据
    public void fetchVideoList(DataCallback<List<VideoBean>> callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<VideoBean> videoList = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                videoList.add(new VideoBean(
                        "重庆洪崖洞夜景，太美了！ #旅行",
                        "旅行博主小王",
                        "12.5w",
                        R.drawable.cover_1,
                        R.raw.video_1,
                        R.drawable.avatar_1
                ));

                videoList.add(new VideoBean(
                        "家里的小猫咪成精了，居然会开门",
                        "萌宠日记",
                        "8900",
                        R.drawable.cover_2,
                        R.raw.video_2,
                        R.drawable.avatar_2
                ));

                videoList.add(new VideoBean(
                        "由简入繁，十分钟学会这道菜",
                        "美食杰",
                        "3.4w",
                        R.drawable.cover_3,
                        R.raw.video_3,
                        R.drawable.avatar_3
                ));

                videoList.add(new VideoBean(
                        "沉浸式体验雨后的古镇，太治愈了",
                        "远方日记",
                        "2.1w",
                        R.drawable.cover_4,
                        R.raw.video_4,
                        R.drawable.avatar_4
                ));

                videoList.add(new VideoBean(
                        "程序员的桌面大揭秘，你的键盘发光吗？",
                        "极客阿Code",
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
