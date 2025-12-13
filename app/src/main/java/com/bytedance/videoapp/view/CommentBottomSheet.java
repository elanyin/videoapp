package com.bytedance.videoapp.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.videoapp.R;
import com.bytedance.videoapp.adapters.CommentAdapter;
import com.bytedance.videoapp.model.CommentBean;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论区底部弹窗 (BottomSheet)
 * <p>
 * 职责:
 * 1.  以从底部弹出的形式展示评论列表。
 * 2.  使用 RecyclerView 显示评论数据。
 * 3.  提供输入框和发送按钮，让用户可以发表新评论。
 * 4.  管理自身的显示和隐藏逻辑。
 */
public class CommentBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private EditText etContent;
    private ImageView ivSend;
    private ImageView ivClose;

    /**
     * 在 onStart() 生命周期中设置弹窗的初始状态和高度。
     * 这是官方推荐的用于修改 BottomSheetDialog 行为的时机。
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 获取 BottomSheet 的根视图
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // 将弹窗的初始高度设置为屏幕高度的 70%
                bottomSheet.getLayoutParams().height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
                // 获取 BottomSheet 的行为控制器
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                // 将其状态设置为完全展开
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载评论区弹窗的布局文件
        return inflater.inflate(R.layout.dialog_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 视图创建完成后，初始化所有子视图和数据
        initViews(view);
        initData();
    }

    /**
     * 初始化视图组件并设置点击事件。
     * @param view Fragment 的根视图
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_comments);
        etContent = view.findViewById(R.id.et_content);
        ivSend = view.findViewById(R.id.iv_send);
        ivClose = view.findViewById(R.id.iv_close);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 设置关闭按钮的点击事件
        ivClose.setOnClickListener(v -> dismiss()); // dismiss() 是 DialogFragment 关闭自身的标准方法

        // 设置发送按钮的点击事件
        ivSend.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(getContext(), "写点什么吧...", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. 创建一个新的评论对象
            CommentBean newComment = new CommentBean(content, "我", "刚刚", "0", R.drawable.avatar_1);
            // 2. 将新评论添加到 Adapter 的数据列表顶部
            adapter.addComment(newComment);

            // 3. 滚动 RecyclerView 到顶部，让用户能立刻看到自己的评论
            recyclerView.scrollToPosition(0);

            // 4. 清空输入框并收起软键盘
            etContent.setText("");
            hideKeyboard();
        });
    }

    /**
     * 初始化评论区的模拟数据。
     * 在真实项目中，这些数据通常会从 ViewModel 或网络请求中获取。
     */
    private void initData() {
        List<CommentBean> list = new ArrayList<>();
        list.add(new CommentBean("这光影效果绝了，每一帧截下来都能当壁纸！👍", "摄影爱好者", "刚刚", "1.2w", R.drawable.avatar_1));
        list.add(new CommentBean("视频剪辑的节奏感很好，转场太丝滑了。", "剪辑练习生", "5分钟前", "4521", R.drawable.avatar_2));
        list.add(new CommentBean("这是在哪里拍的呀？风景看起来好治愈。", "旅行日记", "1小时前", "899", R.drawable.avatar_3));
        list.add(new CommentBean("背景音乐配得恰到好处，瞬间氛围感拉满。🎵", "听风者", "2小时前", "125", R.drawable.avatar_4));
        list.add(new CommentBean("期待博主更新，希望能多出一些这样的高质量内容。", "路人甲", "3小时前", "66", R.drawable.avatar_5));

        adapter = new CommentAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 隐藏软键盘的辅助方法。
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && etContent != null) {
            imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
        }
    }
}
