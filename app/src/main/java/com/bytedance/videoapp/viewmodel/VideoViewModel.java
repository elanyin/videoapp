package com.bytedance.videoapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.repository.VideoRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频数据的 ViewModel
 * <p>
 * 职责:
 * 1.  作为 MVVM 架构的核心，连接 View(Activity/Fragment) 和 Model(Repository)。
 * 2.  持有与UI相关的状态和数据，特别是视频列表，并使用 LiveData 暴露给 View 层。
 * 3.  从 Repository 获取数据，并处理所有业务逻辑，如分页、刷新等。
 * 4.  具有生命周期感知能力，在配置变更（如屏幕旋转）后依然存活，保证数据不丢失。
 * 5.  绝对不能持有任何 View(Activity/Context) 的引用，以避免内存泄漏。
 */
public class VideoViewModel extends ViewModel {

    // 持有数据仓库 Repository 的实例。
    private final VideoRepository repository;

    // --- LiveData 定义 ---

    // 供内部修改的视频列表 LiveData
    private final MutableLiveData<List<VideoBean>> _videoList = new MutableLiveData<>();
    // 暴露给 View 层的不可变视频列表 LiveData，保证数据流的单向性
    public LiveData<List<VideoBean>> videoList = _videoList;

    // 用于上拉加载的增量数据 LiveData。单独一个 LiveData 可以让 View 层清晰地知道这是追加数据，从而执行更高效的局部刷新。
    private final MutableLiveData<List<VideoBean>> _appendedVideos = new MutableLiveData<>();
    public LiveData<List<VideoBean>> appendedVideos = _appendedVideos;

    // 暴露给 View 层的刷新状态 LiveData，用于控制 SwipeRefreshLayout 的加载圈。
    public final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();

    // --- 内部状态管理 ---

    // 在 ViewModel 内部维护一份完整的数据拷贝，用于分页和数据管理。
    private final List<VideoBean> currentData = new ArrayList<>();

    private int currentPage = 0;       // 当前加载的页码
    private boolean isLoading = false; // 加载锁，防止因快速滑动或重复点击导致的并发加载

    /**
     * 构造函数，获取 Repository 的单例。
     */
    public VideoViewModel() {
        repository = VideoRepository.getInstance();
    }

    /**
     * 确保首次加载数据。
     * 如果已有数据，则直接将数据“回放”给新的观察者，避免重复加载。
     */
    public void ensureFirstLoad() {
        if (!currentData.isEmpty()) {
            _videoList.setValue(new ArrayList<>(currentData));
            return;
        }
        // 首次加载第一页数据，不清空旧数据，不显示刷新圈
        loadPage(0, false, false);
    }

    /**
     * 加载所有已缓存的数据。主要用于详情页，它可以一次性获取所有数据进行滑动。
     */
    public void loadAllCachedData() {
        List<VideoBean> allData = repository.getAllCachedVideos();
        if (!allData.isEmpty()) {
            currentData.clear();
            currentData.addAll(allData);
            _videoList.setValue(new ArrayList<>(currentData));
        } else {
            // 如果没有任何缓存，则执行一次标准的刷新操作
            refresh();
        }
    }

    /**
     * 供 View 层调用的“下拉刷新”逻辑。
     */
    public void refresh() {
        // 刷新总是加载第一页，需要清空旧数据，并显示刷新圈
        loadPage(0, true, true);
    }

    /**
     * 供 View 层调用的“上拉加载更多”逻辑。
     */
    public void loadMore() {
        if (isLoading) return; // 如果正在加载，则直接返回
        int nextPage = currentPage + 1;
        // 加载下一页，不清空旧数据，不显示刷新圈
        loadPage(nextPage, false, false);
    }

    /**
     * 核心的数据加载方法。
     * @param page        要加载的页码
     * @param clearOld    是否清空旧数据（用于刷新）
     * @param showRefresh 是否显示刷新圈（用于刷新）
     */
    private void loadPage(int page, boolean clearOld, boolean showRefresh) {
        if (isLoading) return;
        isLoading = true;
        if (showRefresh) {
            isRefreshing.setValue(true);
        }

        // 从 Repository 获取数据，并传入回调
        repository.fetchVideoList(page, new VideoRepository.DataCallback<List<VideoBean>>() {
            @Override
            public void onSuccess(List<VideoBean> data) {
                if (clearOld) {
                    currentData.clear();
                    currentPage = 0;
                }

                if (data != null && !data.isEmpty()) {
                    // 如果是加载更多，则更新页码
                    if (page > currentPage && !clearOld) {
                        currentPage = page;
                    }
                    currentData.addAll(data);

                    if (clearOld) {
                        // 刷新操作，更新整个列表
                        _videoList.postValue(new ArrayList<>(currentData));
                    } else {
                        // 加载更多操作，只更新追加的数据
                        _appendedVideos.postValue(new ArrayList<>(data));
                    }
                } else if (!clearOld) {
                    // 如果是加载更多但没有获取到数据，可以认为已到达最后一页，页码可以回退以允许重试
                    if (page > 0) {
                        currentPage = Math.max(0, page - 1);
                    }
                }

                // 结束刷新状态
                if (showRefresh) {
                    isRefreshing.postValue(false);
                }
                isLoading = false;
            }

            @Override
            public void onError(String msg) {
                // 错误处理
                if (showRefresh) {
                    isRefreshing.postValue(false);
                }
                // 如果是加载更多失败，页码回退
                if (!clearOld && page > 0) {
                    currentPage = Math.max(0, page - 1);
                }
                isLoading = false;
            }
        });
    }
}
