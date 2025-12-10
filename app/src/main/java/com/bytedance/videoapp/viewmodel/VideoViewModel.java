package com.bytedance.videoapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.repository.VideoRepository;

import java.util.List;

public class VideoViewModel extends ViewModel {

    // 1. 持有 Repository
    private final VideoRepository repository;

    // 2. 定义可被观察的数据 (LiveData)
    // MutableLiveData 仅在 ViewModel 内部修改
    private final MutableLiveData<List<VideoBean>> _videoList = new MutableLiveData<>();

    // 暴露给 Activity 的是不可修改的 LiveData，保证数据安全
    public LiveData<List<VideoBean>> videoList = _videoList;

    public VideoViewModel() {
        repository = new VideoRepository();
    }

    // 3. 业务逻辑：请求数据
    public void loadVideos() {
        repository.fetchVideoList(new VideoRepository.DataCallback<List<VideoBean>>() {
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
}