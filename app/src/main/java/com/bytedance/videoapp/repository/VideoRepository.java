package com.bytedance.videoapp.repository;
import android.os.Handler;
import android.os.Looper;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.model.VideoBean;

import java.util.ArrayList;
import java.util.List;

public class VideoRepository {

    private static volatile VideoRepository sInstance;

    // 简单的内存缓存，避免跨 Activity 重复生成数据
    private final List<VideoBean> cachedList = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int PAGE_SIZE = 5;

    public static VideoRepository getInstance() {
        if (sInstance == null) {
            synchronized (VideoRepository.class) {
                if (sInstance == null) {
                    sInstance = new VideoRepository();
                }
            }
        }
        return sInstance;
    }

    private VideoRepository() {}

    // 模拟从服务器异步获取数据（带缓存）
    public void fetchVideoList(int page, DataCallback<List<VideoBean>> callback) {
        mainHandler.postDelayed(() -> {
            List<VideoBean> pageData = getOrGeneratePage(page);
            if (callback != null) {
                // 返回一个新的列表，避免外部修改缓存
                callback.onSuccess(new ArrayList<>(pageData));
            }
        }, 300);
    }

    // 回调接口
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String msg);
    }

    private synchronized List<VideoBean> getOrGeneratePage(int page) {
        int neededSize = (page + 1) * PAGE_SIZE;
        // 不足时生成新数据
        while (cachedList.size() < neededSize) {
            int index = cachedList.size() / PAGE_SIZE;
            cachedList.addAll(generateBaseSet(index));
        }
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, cachedList.size());
        return cachedList.subList(start, end);
    }

    public List<VideoBean> getAllCachedVideos() {
        // 返回副本，防止外部意外修改缓存
        return new ArrayList<>(cachedList);
    }

    // 基础 5 条数据，追加页码后缀以示区分
    private List<VideoBean> generateBaseSet(int pageIndex) {
        List<VideoBean> list = new ArrayList<>();
        String suffix = pageIndex == 0 ? "" : (" ·P" + pageIndex);
        list.add(new VideoBean(
                "深夜治愈：点燃一支香薰蜡烛，放空自己#氛围感" + suffix,
                "生活美学家",
                "12.5w",
                R.drawable.cover_1,
                R.raw.video_1,
                R.drawable.avatar_1
        ));
        list.add(new VideoBean(
                "把车窗摇下来，风里都是自由的味道#公路旅行" + suffix,
                "公路日记",
                "8900",
                R.drawable.cover_2,
                R.raw.video_2,
                R.drawable.avatar_2
        ));
        list.add(new VideoBean(
                "这片橘子海送给你，想去海边了嘛？" + suffix,
                "海岛听风",
                "3.4w",
                R.drawable.cover_3,
                R.raw.video_3,
                R.drawable.avatar_3
        ));
        list.add(new VideoBean(
                "家里的猫半夜偷偷练琴，这难道是莫扎特转世？" + suffix,
                "喵星人观察员",
                "2.1w",
                R.drawable.cover_4,
                R.raw.video_4,
                R.drawable.avatar_4
        ));
        list.add(new VideoBean(
                "今年的第一场雪，大家来一起看雪" + suffix,
                "北方的冬",
                "5.3w",
                R.drawable.cover_5,
                R.raw.video_5,
                R.drawable.avatar_5
        ));
        return list;
    }

}
