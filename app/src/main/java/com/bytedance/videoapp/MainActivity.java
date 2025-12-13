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

@UnstableApi
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoListAdapter adapter;
    private View groupHome;
    private View groupMe;
    private View layoutFollow;
    private View layoutMall;
    private TabLayout tabLayout;
    private BottomNavigationView bottomNav;
    private VideoViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        initRecyclerView();
        initTabLayout();
        initBottomNavigation();
        initViewModel();
        initRefreshLayout();
        initScrollListener();
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        groupHome = findViewById(R.id.group_home);
        groupMe = findViewById(R.id.group_me);
        layoutFollow = findViewById(R.id.layout_follow);
        layoutMall = findViewById(R.id.layout_mall);
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        
        // 设置布局管理器：2列垂直瀑布流
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                2, 
                StaggeredGridLayoutManager.VERTICAL
        );
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);

        // 初始化Adapter
        adapter = new VideoListAdapter();
        recyclerView.setAdapter(adapter);

        // 设置点击事件
        adapter.setOnItemClickListener((video, position) -> {
            Intent intent = new Intent(MainActivity.this, VideoDetailActivity.class);
            intent.putExtra("pos", position);
            // 传递封面图避免加载黑屏
            intent.putExtra("cover_res_id", video.coverResId);
            startActivity(intent);
            // 去掉 Activity 默认的切换动画
            overridePendingTransition(0, 0);
        });
    }

    /**
     * 初始化TabLayout
     */
    private void initTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("商城"));
        tabLayout.addTab(tabLayout.newTab().setText("关注"));
        tabLayout.addTab(tabLayout.newTab().setText("推荐"));

        // 默认选中推荐tab
        tabLayout.getTabAt(2).select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                handleTabSelection(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 不需要处理
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 不需要处理
            }
        });
    }

    /**
     * 初始化底部导航
     */
    private void initBottomNavigation() {
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setItemIconTintList(null);

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
                return false;
            }
        });
    }

    /**
     * 处理Tab选择
     */
    private void handleTabSelection(String tabText) {
        // 隐藏所有内容
        recyclerView.setVisibility(View.GONE);
        layoutFollow.setVisibility(View.GONE);
        layoutMall.setVisibility(View.GONE);

        // 根据选中的tab显示对应内容
        if ("商城".equals(tabText)) {
            layoutMall.setVisibility(View.VISIBLE);
        } else if ("关注".equals(tabText)) {
            layoutFollow.setVisibility(View.VISIBLE);
        } else {
            // 推荐
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示首页组
     */
    private void showHomeGroup() {
        groupHome.setVisibility(View.VISIBLE);
        groupMe.setVisibility(View.GONE);
    }

    /**
     * 显示我的组
     */
    private void showMeGroup() {
        groupHome.setVisibility(View.GONE);
        groupMe.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化 ViewModel 并设置观察者
     */
    private void initViewModel() {
        // 获取 ViewModel 实例
        viewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        // 3. 观察数据变化
        viewModel.videoList.observe(this, videoBeans -> {
            // 当 ViewModel 里的数据获取成功并 postValue 时，这里会自动执行
            if (videoBeans != null) {
                adapter.setData(videoBeans);
            }
        });

        // 监听追加数据，做增量插入避免位置跳动
        viewModel.appendedVideos.observe(this, appended -> {
            if (appended != null) {
                adapter.appendData(appended);
            }
        });

        // 4. 发起加载请求（仅首次）
        viewModel.ensureFirstLoad();
    }


    /**
     * 初始化下拉刷新
     */
    private void initRefreshLayout() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        // 设置下拉圈圈的颜色
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light);

        // 监听下拉动作
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refresh();
        });

        // 监听 ViewModel 的加载状态，控制圈圈消失
        viewModel.isRefreshing.observe(this, isRefreshing -> {
            swipeRefreshLayout.setRefreshing(isRefreshing);
        });
    }


    /**
     * 初始化上拉加载
     */
    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 向下滑动时才处理 (dy > 0)
                if (dy > 0) {
                    StaggeredGridLayoutManager layoutManager =
                            (StaggeredGridLayoutManager) recyclerView.getLayoutManager();

                    if (layoutManager == null) return;

                    // 获取瀑布流每一列最后可见 item 的位置
                    int[] lastVisiblePositions = layoutManager.findLastVisibleItemPositions(null);

                    // 找到最大的那个位置（因为是瀑布流，底部可能参差不齐）
                    int lastVisibleItemPosition = getLastVisibleItem(lastVisiblePositions);

                    // 获取总 Item 数量
                    int totalItemCount = layoutManager.getItemCount();

                    // 触发加载阈值：如果还可以滚动的 item 少于 4 个，就开始预加载
                    if (totalItemCount > 0 && lastVisibleItemPosition >= totalItemCount - 4) {
                        viewModel.loadMore();
                    }
                }
            }
        });
    }

    // 辅助方法：获取数组中的最大值
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
