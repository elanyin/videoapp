package com.bytedance.videoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bytedance.videoapp.adapters.VideoListAdapter;
import com.bytedance.videoapp.view.VideoDetailActivity;
import com.bytedance.videoapp.viewmodel.VideoViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

/**
 * 应用主页面 (首页)
 * <p>
 * 职责:
 * 1.  作为应用的入口 Activity。
 * 2.  管理主要的UI布局，包括顶部的 TabLayout 和底部的 BottomNavigationView。
 * 3.  使用 RecyclerView 以瀑布流形式展示视频列表。
 * 4.  通过 ViewModel 获取和观察视频数据，并将其提交给 Adapter。
 * 5.  实现下拉刷新和上拉加载更多的功能。
 * 6.  处理用户交互，如点击视频项跳转到详情页。
 */
@UnstableApi
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoListAdapter adapter;
    private View groupHome, groupMe, layoutFollow, layoutMall;
    private TabLayout tabLayout;
    private BottomNavigationView bottomNav;
    private VideoViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 启用沉浸式体验
        setContentView(R.layout.activity_main);

        // 遵循单一职责原则，将初始化逻辑拆分到不同方法中
        initViews();
        initRecyclerView();
        initTabLayout();
        initBottomNavigation();
        initViewModel();
        initRefreshLayout();
        initScrollListener();
    }

    /**
     * 初始化基础视图组件的引用。
     */
    private void initViews() {
        groupHome = findViewById(R.id.group_home);
        groupMe = findViewById(R.id.group_me);
        layoutFollow = findViewById(R.id.layout_follow);
        layoutMall = findViewById(R.id.layout_mall);
    }

    /**
     * 初始化 RecyclerView，包括布局管理器、Adapter 和点击事件。
     */
    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        
        // 设置布局管理器：2列垂直瀑布流
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                2, 
                StaggeredGridLayoutManager.VERTICAL
        );
        // 防止 item 因为重用而发生位置交换
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);

        // 初始化Adapter
        adapter = new VideoListAdapter();
        recyclerView.setAdapter(adapter);

        // 设置列表项的点击事件
        adapter.setOnItemClickListener((video, position) -> {
            Intent intent = new Intent(MainActivity.this, VideoDetailActivity.class);
            // 传递视频在列表中的位置，方便详情页直接定位
            intent.putExtra("pos", position);
            // 【关键优化】传递封面图资源ID，让详情页可以立刻显示封面，避免加载视频时出现黑屏
            intent.putExtra("cover_res_id", video.coverResId);
            startActivity(intent);
            // 去掉 Activity 默认的切换动画，实现无缝切换的效果
            overridePendingTransition(0, 0);
        });
    }

    /**
     * 初始化顶部的 TabLayout。
     */
    private void initTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("商城"));
        tabLayout.addTab(tabLayout.newTab().setText("关注"));
        tabLayout.addTab(tabLayout.newTab().setText("推荐"));

        // 默认选中"推荐"tab
        tabLayout.getTabAt(2).select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    handleTabSelection(tab.getText().toString());
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /*无需处理*/ }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { /*无需处理*/ }
        });
    }

    /**
     * 初始化底部的 BottomNavigationView。
     */
    private void initBottomNavigation() {
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setItemIconTintList(null); // 允许菜单项的图标使用其原始颜色

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showHomeGroup();
                return true;
            } else if (id == R.id.nav_me) {
                showMeGroup();
                return true;
            } else {
                Toast.makeText(this, "功能开发中", Toast.LENGTH_SHORT).show();
                return false; // 返回false表示不选中该项
            }
        });
    }

    /**
     * 根据顶部Tab的选择，切换显示不同的内容区域。
     */
    private void handleTabSelection(String tabText) {
        // 先隐藏所有内容
        recyclerView.setVisibility(View.GONE);
        layoutFollow.setVisibility(View.GONE);
        layoutMall.setVisibility(View.GONE);

        // 根据选中的tab显示对应内容
        if ("商城".equals(tabText)) {
            layoutMall.setVisibility(View.VISIBLE);
        } else if ("关注".equals(tabText)) {
            layoutFollow.setVisibility(View.VISIBLE);
        } else { // "推荐"
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示"首页"相关视图，隐藏"我的"相关视图。
     */
    private void showHomeGroup() {
        groupHome.setVisibility(View.VISIBLE);
        groupMe.setVisibility(View.GONE);
    }

    /**
     * 显示"我的"相关视图，隐藏"首页"相关视图。
     */
    private void showMeGroup() {
        groupHome.setVisibility(View.GONE);
        groupMe.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化 ViewModel 并设置数据观察者。
     * 这是 MVVM 架构中 View 层与 ViewModel 层交互的核心。
     */
    private void initViewModel() {
        // 获取 ViewModel 实例。ViewModel的生命周期比Activity更长，能在屏幕旋转后保留数据。
        viewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        // 观察【完整视频列表】的变化
        viewModel.videoList.observe(this, videoBeans -> {
            // 当ViewModel中的数据获取成功并通过 postValue 更新时，此回调会自动执行
            if (videoBeans != null) {
                // 将新数据提交给 Adapter 进行全量刷新
                adapter.setData(videoBeans);
            }
        });

        // 观察【追加视频列表】的变化
        viewModel.appendedVideos.observe(this, appended -> {
            // 此 LiveData 专门用于上拉加载更多
            if (appended != null) {
                // 使用增量更新，避免列表跳动，提供更好的用户体验
                adapter.appendData(appended);
            }
        });

        // 触发 ViewModel 首次加载数据（如果数据尚未加载）
        viewModel.ensureFirstLoad();
    }


    /**
     * 初始化下拉刷新功能 (SwipeRefreshLayout)。
     */
    private void initRefreshLayout() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light); // 设置刷新圈圈的颜色

        // 监听用户的下拉手势
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 当用户下拉时，通知 ViewModel 执行刷新操作
            viewModel.refresh();
        });

        // 观察 ViewModel 中的刷新状态
        viewModel.isRefreshing.observe(this, isRefreshing -> {
            // 根据 ViewModel 的状态，控制刷新圈圈的显示与隐藏
            swipeRefreshLayout.setRefreshing(isRefreshing);
        });
    }


    /**
     * 初始化 RecyclerView 的滚动监听，用于实现上拉加载更多（无限滚动）。
     */
    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 只在向下滑动时处理 (dy > 0)
                if (dy > 0) {
                    StaggeredGridLayoutManager layoutManager =
                            (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager == null) return;

                    // 获取瀑布流所有列中，最后可见的 item 的位置
                    int[] lastVisiblePositions = layoutManager.findLastVisibleItemPositions(null);
                    int lastVisibleItemPosition = getLastVisibleItem(lastVisiblePositions);
                    int totalItemCount = layoutManager.getItemCount();

                    // 触发加载的阈值：如果列表底部剩余的可滚动项少于4个，就开始预加载
                    if (totalItemCount > 0 && lastVisibleItemPosition >= totalItemCount - 4) {
                        viewModel.loadMore();
                    }
                }
            }
        });
    }

    /**
     * 辅助方法：从StaggeredGridLayout的多个 lastVisiblePositions 中获取最大值。
     * @param lastVisiblePositions 每列的最后一个可见项位置数组
     * @return 最大的位置值
     */
    private int getLastVisibleItem(int[] lastVisiblePositions) {
        int max = 0;
        for (int value : lastVisiblePositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
