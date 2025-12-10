package com.bytedance.videoapp.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.repository.VideoRepository;

import java.util.ArrayList;
import java.util.List;

public class VideoViewModel extends ViewModel {

    // 1. 持有 Repository
    private final VideoRepository repository;

    // 2. 定义可被观察的数据 (LiveData)
    // MutableLiveData 仅在 ViewModel 内部修改
    private final MutableLiveData<List<VideoBean>> _videoList = new MutableLiveData<>();

    // 暴露加载状态，用于控制 SwipeRefreshLayout 的旋转圈圈停止
    public final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();

    // 暴露给 Activity 的是不可修改的 LiveData，保证数据安全
    public LiveData<List<VideoBean>> videoList = _videoList;

    // 内部维护一个完整的数据集合
    private final List<VideoBean> currentData = new ArrayList<>();

    private int currentPage = 0; // 当前页码
    private boolean isLoading = false; // 防止多次触发加载

    public VideoViewModel() {
        repository = new VideoRepository();
    }

    // 3. 业务逻辑：请求数据
    public void loadVideos() {
        Log.d("VideoViewModel", "请求数据");
        repository.fetchVideoList(0, new VideoRepository.DataCallback<List<VideoBean>>() {
            @Override
            public void onSuccess(List<VideoBean> data) {
                // 将数据 post 出去，Activity 会自动收到通知
                _videoList.postValue(data);
            }

            @Override
            public void onError(String msg) {

            }
        });
    }

    // 1. 下拉刷新逻辑
    public void refresh() {
        if (isLoading) return;
        isLoading = true;
        currentPage = 0; // 重置页码
        isRefreshing.setValue(true); // 让 UI 显示加载圈

        repository.fetchVideoList(0, new VideoRepository.DataCallback<List<VideoBean>>() {
            @Override
            public void onSuccess(List<VideoBean> data) {
                currentData.clear(); // 清空老数据
                currentData.addAll(data);
                _videoList.setValue(currentData); // 通知 UI 更新

                isRefreshing.setValue(false); // 隐藏加载圈
                isLoading = false;
            }

            @Override
            public void onError(String msg) {
                isRefreshing.setValue(false);
                isLoading = false;
            }
        });
    }

    // 2. 上拉加载更多逻辑
    public void loadMore() {
        if (isLoading) return;
        isLoading = true;
        currentPage++; // 页码 +1

        repository.fetchVideoList(currentPage, new VideoRepository.DataCallback<List<VideoBean>>() {
            @Override
            public void onSuccess(List<VideoBean> data) {
                if (data != null && !data.isEmpty()) {
                    currentData.addAll(data); // 追加新数据
                    _videoList.setValue(currentData); // 通知 UI 更新
                } else {
                    // 没有更多数据了，可以给个提示
                    currentPage--; // 还原页码
                }
                isLoading = false;
            }

            @Override
            public void onError(String msg) {
                currentPage--; // 还原页码
                isLoading = false;
            }
        });
    }
}