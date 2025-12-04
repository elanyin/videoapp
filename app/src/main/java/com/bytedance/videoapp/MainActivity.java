package com.bytedance.videoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bytedance.videoapp.adapters.VideoAdapter;
import com.bytedance.videoapp.model.VideoRepository;
import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.view.VideoDetailActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private View groupHome;
    private View groupMe;
    private View layoutFollow;
    private View layoutMall;
    private TabLayout tabLayout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        initRecyclerView();
        initTabLayout();
        initBottomNavigation();
        loadVideoData();
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
        adapter = new VideoAdapter();
        recyclerView.setAdapter(adapter);

        // 设置点击事件
        adapter.setOnItemClickListener((video, position) -> navigateToVideoDetail(position));
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
     * 加载视频数据
     */
    private void loadVideoData() {
        List<VideoBean> videoList = VideoRepository.getVideoList();
        adapter.setData(videoList);
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
     * 导航到视频详情页
     */
    private void navigateToVideoDetail(int position) {
        Intent intent = new Intent(MainActivity.this, VideoDetailActivity.class);
        intent.putExtra("pos", position);
        startActivity(intent);
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
}
