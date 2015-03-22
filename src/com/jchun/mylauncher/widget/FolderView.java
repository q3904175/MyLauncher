package com.jchun.mylauncher.widget;

import android.content.Context;
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.jchun.mylauncher.R;

/**
 * 文件夹对话框
 * @author JChun
 *
 */
public class FolderView extends RelativeLayout {

    private FolderEditText mFolderName;//标题
    private LinearLayout mFolderContainer;
    private FolderContentView mContentView;//文件夹视图
    private FolderInfo mInfo; //
    private FolderScrollView mScrollView;
    private boolean messed = false;
    private View view;
    private boolean isInit = false;
    private LayoutCalculator lc;

    public FolderView(Context context) {
        super(context);
    }

    public FolderView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    private void init(LayoutCalculator lc, int screenHeight) {
        //设置对话框宽度,以排3列图标为宽度,高度根据外边框减去对应间距
        LayoutParams params = new LayoutParams((lc.iconWidth + lc.gapH) * 3, screenHeight - lc.dpToPixel(40) - 4
                * lc.folderMargin);
        //居中
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        view = View.inflate(getContext(), R.layout.launcher_folder, null);
        mFolderName = (FolderEditText) view.findViewById(R.id.folder_name);
        mFolderContainer = (LinearLayout) view.findViewById(R.id.folder_container);
        setScrollView((FolderScrollView) view.findViewById(R.id.folder_scrollview));
        mFolderName.setFolder(this);
        addView(view, params);
    }

    /**
     * 左边距
     * @return
     */
    public int getTranslateLeft() {
        return view.getLeft();
    }

    /**
     * 上边距
     * @return
     */
    public int getTranslateTop() {
        return view.getTop() + lc.dpToPixel(50);
    }

    /**
     * 初始化
     */
    public void onReday(LayoutCalculator lc, ObjectPool pp, int screenHeight) {
        this.lc = lc;
        if (!isInit) {
            init(lc, screenHeight);
            isInit = true;
        }
        mFolderName.setText(mInfo.getTitle());
        if (mContentView == null) {
            mContentView = new FolderContentView(getContext());
            mContentView.init(lc, pp);
            mContentView.setFolderInfo(mInfo);
            mFolderContainer
                    .addView(mContentView, LinearLayout.LayoutParams.MATCH_PARENT, mContentView.getViewHeight());
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
            mContentView.setFolderInfo(mInfo);
            params.height = mContentView.getViewHeight();
            mContentView.setLayoutParams(params);
        }
        if (messed) {
            jiggle();
        } else {
            unJiggle();
        }
    }

    /**
     * 重置layout高度
     */
    public void initLayout() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        mContentView.setFolderInfo(mInfo);
        params.height = mContentView.getViewHeight();
        mContentView.setLayoutParams(params);
    }

    /**
     * 重命名标题
     * @param commit
     */
    public void doneEditingFolderName(boolean commit) {
        String newTitle = mFolderName.getText().toString();
        mInfo.setTitle(newTitle);
        requestFocus();
        Selection.setSelection((Spannable) mFolderName.getText(), 0, 0);
    }

    public FolderInfo getInfo() {
        return mInfo;
    }

    public void setInfo(FolderInfo info) {
        this.mInfo = info;
    }

    public boolean isJiggling() {
        return messed;
    }

    public void jiggle() {
        messed = true;
        if (mContentView != null) {
            mContentView.jiggle();
        }
    }

    public void unJiggle() {
        messed = false;
        if (mContentView != null) {
            mContentView.unJiggle();
        }
    }

    public FolderContentView getContentView() {
        return mContentView;
    }

    public FolderScrollView getScrollView() {
        return mScrollView;
    }

    public void setScrollView(FolderScrollView mScrollView) {
        this.mScrollView = mScrollView;
    }

    public void setScrollOnTouchListener(OnTouchListener scrollContainer_OnTouch) {
        if (mScrollView != null) {
            mScrollView.setOnTouchListener(scrollContainer_OnTouch);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mScrollView.onTouchEvent(event)) {
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

}
