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

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private EditText etContent;
    private ImageView ivSend;
    private ImageView ivClose;

    @Override
    public void onStart() {
        super.onStart();
        // 设置弹窗高度为屏幕的 70%
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_comments);
        etContent = view.findViewById(R.id.et_content);
        ivSend = view.findViewById(R.id.iv_send);
        ivClose = view.findViewById(R.id.iv_close);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 关闭按钮
        ivClose.setOnClickListener(v -> dismiss());

        // 发送按钮
        ivSend.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(getContext(), "写点什么吧...", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. 添加到列表
            CommentBean newComment = new CommentBean(content, "我", "刚刚", "0", R.drawable.avatar_1);
            adapter.addComment(newComment);

            // 2. 滚动到顶部
            recyclerView.scrollToPosition(0);

            // 3. 清空输入框并收起键盘
            etContent.setText("");
            hideKeyboard();
        });
    }

    private void initData() {
        // 模拟数据
        List<CommentBean> list = new ArrayList<>();
        list.add(new CommentBean("这光影效果绝了，每一帧截下来都能当壁纸！👍", "摄影爱好者", "刚刚", "1.2w", R.drawable.avatar_1));
        list.add(new CommentBean("视频剪辑的节奏感很好，转场太丝滑了。", "剪辑练习生", "5分钟前", "4521", R.drawable.avatar_2));
        list.add(new CommentBean("这是在哪里拍的呀？风景看起来好治愈。", "旅行日记", "1小时前", "899", R.drawable.avatar_3));
        list.add(new CommentBean("背景音乐配得恰到好处，瞬间氛围感拉满。🎵", "听风者", "2小时前", "125", R.drawable.avatar_4));
        list.add(new CommentBean("期待博主更新，希望能多出一些这样的高质量内容。", "路人甲", "3小时前", "66", R.drawable.avatar_5));

        adapter = new CommentAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
        }
    }
}