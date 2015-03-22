package com.jchun.mylauncher.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class PageScrollView extends HorizontalScrollView {
    private static final String TAG = "PageScrollView";
    private LinearLayout container;
    private GestureDetector gestureDetector;
    private boolean intense = false;
    private OnScrollChangedListener onScrollChanged;
    private OnGestureListener scrollDetector = new GestureListener();
    private Bitmap background;
    private Paint paint = new Paint(1);

    public PageScrollView(Context context) {
        super(context);
        init();
    }

    public PageScrollView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public PageScrollView(Context context, AttributeSet attr, int style) {
        super(context, attr, style);
        init();
    }

    private LinearLayout getContainer() {
        if (this.container == null)
            this.container = ((LinearLayout) getChildAt(0));
        return this.container;
    }

    private void init() {
        this.gestureDetector = new GestureDetector(getContext(), this.scrollDetector);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int scrollX;
        int j;
        LinearLayout mContainer;
        int mCount;
        scrollX = getScrollX();
        j = scrollX + getWidth();
        mContainer = getContainer();
        mCount = mContainer.getChildCount();
        for (int i = 0; i < mCount; i++) {
            View subView = mContainer.getChildAt(0);
            if ((subView.getLeft() < j) && (subView.getRight() > scrollX))
                subView.setVisibility(View.VISIBLE);
        }
        if (this.background != null) {
            paint.setFilterBitmap(true);
            int width = this.background.getWidth();
            int height = this.background.getHeight();
            int x = getScrollX();
            int n = height * getWidth() / getHeight();
            int w;
//            if (mCount == 1) {
//                w = x * (width - n) / 1 / getWidth();
//            } else {
//                w = x * ((width - n) / (mCount - 1)) / getWidth();
//            }
//            canvas.drawBitmap(this.background, new Rect(w, 0, n + w, height), new Rect(
//                    x, 0, x + getWidth(), getHeight()), this.paint);
            canvas.drawBitmap(this.background, new Rect(0, 0, n, height), new Rect(
                  x, 0, x + getWidth(), getHeight()), this.paint);
        }
        super.dispatchDraw(canvas);
    }
    public void setBackground(Bitmap background) {
        this.background = background;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onTouchEvent");
        try {
            super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
        }
        return false;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent");
        try {
            super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
        }
        if (gestureDetector.onTouchEvent(ev))
            return true;
        else {
            return false;
        }
    }

    @Override
    protected void onScrollChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        super.onScrollChanged(paramInt1, paramInt2, paramInt3, paramInt4);
        if (this.onScrollChanged == null)
            return;
        this.onScrollChanged.onScrollChanged();
    }

    @Override
    public void scrollTo(int distanceX, int distanceY) {
        if (distanceX < 0)
            return;
        super.scrollTo(distanceX, distanceY);
    }

    public void setIntenseModeEnabled(boolean enable) {
        this.intense = enable;
    }

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        this.onScrollChanged = onScrollChangedListener;
    }

    class GestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent paramMotionEvent) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent paramMotionEvent) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int i = 1;
            if (intense) {
                if (Math.abs(distanceX) <= 6.0F * Math.abs(distanceY))
                    return true;
            } else {
                if (Math.abs(distanceX) > Math.abs(distanceY))
                    return true;
            }
            return false;
        }

        @Override
        public void onShowPress(MotionEvent ev) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            return false;
        }
    }

    public void destory() {
        
    }

}
