package com.bytedance.videoapp.repository;
import android.os.Handler;
import android.os.Looper;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.model.VideoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频数据仓库 (Repository)
 * <p>
 * 职责:
 * 1.  作为应用中视频数据的唯一真实来源 (Single Source of Truth)。
 * 2.  采用单例模式，确保在整个应用中只有一个实例，用于维护一份统一的数据缓存。
 * 3.  封装数据来源的细节。ViewModel 只向 Repository 请求数据，而无需关心数据是从网络、数据库还是内存缓存中获取的。
 * 4.  模拟异步从服务器获取数据，并支持分页加载。
 */
public class VideoRepository {

    // 使用 volatile 保证多线程环境下的可见性，配合双重检查锁定实现线程安全的懒加载单例
    private static volatile VideoRepository sInstance;

    // 一个简单的内存缓存，用于存储已加载的视频数据，避免重复生成和跨 Activity 重复加载
    private final List<VideoBean> cachedList = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int PAGE_SIZE = 5; // 定义每一页加载的数据量

    /**
     * 获取数据仓库的唯一实例。
     * 采用双重检查锁定 (Double-Checked Locking) 模式，确保线程安全和高性能。
     * @return VideoRepository的单例
     */
    public static VideoRepository getInstance() {
        if (sInstance == null) { // 第一次检查，不加锁，提高性能
            synchronized (VideoRepository.class) { // 加锁，保证线程安全
                if (sInstance == null) { // 第二次检查，防止重复创建实例
                    sInstance = new VideoRepository();
                }
            }
        }
        return sInstance;
    }

    // 私有构造函数，防止外部直接创建实例
    private VideoRepository() {}

    /**
     * 模拟从服务器异步获取分页数据。
     * @param page     要获取的页码 (从0开始)
     * @param callback 数据回调接口，用于通知调用方成功或失败
     */
    public void fetchVideoList(int page, DataCallback<List<VideoBean>> callback) {
        // 使用 Handler.postDelayed 模拟网络延迟
        mainHandler.postDelayed(() -> {
            List<VideoBean> pageData = getOrGeneratePage(page);
            if (callback != null) {
                // 总是返回一个新的列表副本，避免外部调用者直接修改内部缓存
                callback.onSuccess(new ArrayList<>(pageData));
            }
        }, 300); // 模拟300毫秒的网络延迟
    }

    /**
     * 数据回调接口，用于将异步获取的数据传递给调用者 (ViewModel)。
     */
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String msg);
    }

    /**
     * 获取或生成指定页码的数据。这是一个同步方法，用于管理内存缓存。
     * @param page 要获取的页码
     * @return 该页的数据列表
     */
    private synchronized List<VideoBean> getOrGeneratePage(int page) {
        int neededSize = (page + 1) * PAGE_SIZE;
        // 如果缓存中的数据量小于当前请求页所需的最大数据量，则生成新的数据补充到缓存中
        while (cachedList.size() < neededSize) {
            int pageIndexToGenerate = cachedList.size() / PAGE_SIZE;
            cachedList.addAll(generateBaseSet(pageIndexToGenerate));
        }
        int start = page * PAGE_SIZE;
        // 确保结束索引不会越界
        int end = Math.min(start + PAGE_SIZE, cachedList.size());
        return cachedList.subList(start, end);
    }

    /**
     * 获取所有已缓存的数据。
     * @return 包含所有已生成数据的列表副本
     */
    public List<VideoBean> getAllCachedVideos() {
        // 返回副本，防止外部意外修改缓存
        return new ArrayList<>(cachedList);
    }

    /**
     * 生成一组基础的模拟数据（5条）。
     * @param pageIndex 页码索引，用于在标题中添加后缀以作区分
     * @return 一个包含5条视频数据的新列表
     */
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
