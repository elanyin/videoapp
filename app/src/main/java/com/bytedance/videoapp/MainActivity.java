package com.bytedance.videoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bytedance.videoapp.model.MockData;
import com.bytedance.videoapp.model.VideoBean;
import com.bytedance.videoapp.adapters.VideoAdapter;
import com.bytedance.videoapp.view.VideoDetailActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {

//    private HomeViewModel viewModel;  // TODO

    private RecyclerView mRecyclerView;
    private VideoAdapter adapter;

    private View groupHome;
    private View groupMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 对不同menu下的界面进行分组
        groupHome = findViewById(R.id.group_home);
        groupMe = findViewById(R.id.group_me);

        // init the list component (recyclerView)
        mRecyclerView = findViewById(R.id.recyclerView);

        // set the layout manager: spanCount = 2, vertical 2列垂直瀑布流
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRecyclerView.setLayoutManager(layoutManager);

        // init data adapter
        adapter = new VideoAdapter();
        mRecyclerView.setAdapter(adapter);

        // MVP: mock data
        List<VideoBean> videoList = MockData.getVideoList();

        adapter.setData(videoList);

        // 点击逻辑，进入视频内流，包括转场动画
        adapter.setOnItemClickListener((video, position) -> {
            // 跳转Intent
            Intent intent = new Intent(MainActivity.this, VideoDetailActivity.class);

            // 带一个点击位置数据到VideoDetailActivity
            intent.putExtra("pos", position);

            // 转场动画
            intent.putExtra("trans_name", "trans_" + position);

            View itemView = mRecyclerView.getLayoutManager().findViewByPosition(position);

            if (itemView != null) {
                // 从 item 里找到封面图控件
                View coverView = itemView.findViewById(R.id.iv_cover);

                // 创建动画
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this,
                        coverView,
                        "trans_" + position
                );
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }

        });

        // 初始化顶部Tab
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("商城"));
        tabLayout.addTab(tabLayout.newTab().setText("关注"));
        tabLayout.addTab(tabLayout.newTab().setText("推荐"));

        // 默认选中推荐tab
        tabLayout.getTabAt(2).select();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            final View layoutFollow = findViewById(R.id.layout_follow);
            final View layoutMall = findViewById(R.id.layout_mall);

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("商城")) {
                    mRecyclerView.setVisibility(View.GONE);
                    layoutFollow.setVisibility(View.GONE);
                    layoutMall.setVisibility(View.VISIBLE);
                } else if (tab.getText().equals("关注")){
                    mRecyclerView.setVisibility(View.GONE);
                    layoutFollow.setVisibility(View.VISIBLE);
                    layoutMall.setVisibility(View.GONE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    layoutFollow.setVisibility(View.GONE);
                    layoutMall.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        // 初始化底部 Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setItemIconTintList(null);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showHomeGroup();
            }
            else if (id == R.id.nav_me) {
                showMeGroup();
            }
            else {
                Toast.makeText(this, "功能开发中", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

    }

    //分组进行显示和隐藏
    private void showHomeGroup() {
        groupHome.setVisibility(View.VISIBLE);
        groupMe.setVisibility(View.GONE);
    }

    private void showMeGroup() {
        groupHome.setVisibility(View.GONE);
        groupMe.setVisibility(View.VISIBLE);
    }
}
