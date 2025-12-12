package com.bytedance.videoapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.repository.VideoRepository;

import java.util.ArrayList;
import java.util.List;

public class VideoViewModel extends ViewModel {

    // 1. 持有单例 Repository，跨 Activity 复用缓存
    private final VideoRepository repository;

    // 2. 定义可被观察的数据 (LiveData)
    // MutableLiveData 仅在 ViewModel 内部修改
    private final MutableLiveData<List<VideoBean>> _videoList = new MutableLiveData<>();

    // 暴露加载状态，用于控制 SwipeRefreshLayout 的旋转圈圈停止
    public final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();

    // 追加数据事件，用于列表增量插入
    private final MutableLiveData<List<VideoBean>> _appendedVideos = new MutableLiveData<>();
    public LiveData<List<VideoBean>> appendedVideos = _appendedVideos;

    // 暴露给 Activity 的是不可修改的 LiveData，保证数据安全
    public LiveData<List<VideoBean>> videoList = _videoList;

    // 内部维护一个完整的数据集合
    private final List<VideoBean> currentData = new ArrayList<>();

    private int currentPage = 0; // 当前页码
    private boolean isLoading = false; // 防止多次触发加载

    public VideoViewModel() {
        repository = VideoRepository.getInstance();
    }

    /**
     * 只在首次进入时加载，已存在数据则直接回放到观察者
     */
    public void ensureFirstLoad() {
        if (!currentData.isEmpty()) {
            _videoList.setValue(new ArrayList<>(currentData));
            return;
        }
        loadPage(0, true, false);
    }

    public void loadAllCachedData() {
        List<VideoBean> allData = repository.getAllCachedVideos();
        if (!allData.isEmpty()) {
            _videoList.setValue(allData);
        } else {
            // 如果实在没数据（比如直接打开详情页），才去加载第一页
            refresh();
        }
    }

    // 下拉刷新逻辑
    public void refresh() {
        loadPage(0, true, true);
    }

    // 上拉加载更多逻辑
    public void loadMore() {
        if (isLoading) return;
        int nextPage = currentPage + 1;
        loadPage(nextPage, false, false);
    }


    private void loadPage(int page, boolean clearOld, boolean showRefresh) {
        if (isLoading) return;
        isLoading = true;
        if (showRefresh) {
            isRefreshing.setValue(true);
        }

        repository.fetchVideoList(page, new VideoRepository.DataCallback<List<VideoBean>>() {
            @Override
            public void onSuccess(List<VideoBean> data) {
                if (clearOld) {
                    currentData.clear();
                    currentPage = 0;
                }
                if (data != null && !data.isEmpty()) {
                    if (page > currentPage && !clearOld) {
                        currentPage = page;
                    }
                    currentData.addAll(data);
                    if (clearOld) {
                        _videoList.setValue(new ArrayList<>(currentData));
                    } else {
                        _appendedVideos.setValue(new ArrayList<>(data));
                    }
                } else if (!clearOld) {
                    // 没有更多数据则回退页码
                    if (page > 0) {
                        currentPage = Math.max(0, page - 1);
                    }
                }

                if (clearOld) {
                    _videoList.setValue(new ArrayList<>(currentData));
                }
                if (showRefresh) {
                    isRefreshing.setValue(false);
                }
                isLoading = false;
            }

            @Override
            public void onError(String msg) {
                if (showRefresh) {
                    isRefreshing.setValue(false);
                }
                if (!clearOld && page > 0) {
                    currentPage = Math.max(0, page - 1);
                }
                isLoading = false;
            }
        });
    }
}