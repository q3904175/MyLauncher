package com.jchun.mylauncher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 文件夹滚动视图
 * @author JChun
 *
 */
public class FolderScrollView extends ScrollView {
    public FolderScrollView(Context context) {
        super(context);
    }

    public FolderScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FolderScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
        }
        return false;
    }
}
