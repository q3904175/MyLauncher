package com.jchun.mylauncher.widget;

import java.lang.ref.SoftReference;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jchun.mylauncher.R;

/**
 * 拖拽图标管理
 * @author JChun
 *
 */
public class IconMover {
    private static final int STATE_CONTRACTING = 2;
    private static final int STATE_DEACTIVE = 0;
    private static final int STATE_EXPANDING = 1;
    private static final int STATE_MOVE_INTO_FOLDER = 4;
    private ApplicationInfo app;
    private Handler handler;
    private int sPageIndex;//起始页
    private int sIndex; //起始位置
    private int pageIndex; // 当前页
    private int index; // 当前位置
    private int indexInFolder; //文件夹内位置
    private boolean isAboveFolder = false; //是否在文件夹上
    private SoftReference<Bitmap> image;
    private LayoutCalculator lc;
    private ObjectPool op;
    private ImageView panel;
    private int relX;
    private int relY;
    private int state = STATE_DEACTIVE;

    private Runnable hider = new Runnable() {
        public void run() {
            panel.setVisibility(View.GONE);
            panel.setImageDrawable(null);
        }
    };

    public IconMover(View view, LayoutCalculator lc, ObjectPool op, Handler handler) {
        this.lc = lc;
        this.op = op;
        this.handler = handler;
        panel = ((ImageView) view.findViewById(R.id.panelMoving));
    }

    /**
     * 拖拽当前页
     * @param pageIndex
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * 拖拽当前位置
     * @param pageIndex
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 返回拖拽位置
     * @return
     */
    public int getIndex() {
        return index;
    }

    /**
     * 返回拖拽页
     * @return
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * 绘图标
     * @param bitmap
     */
    private void drawIcon(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0, PorterDuff.Mode.SRC);
        Paint darkPaint = op.getPaintDarkener();
        app.drawBoundIcon(lc, op, canvas, lc.fullMarginLeftBlackCircle, lc.cMarginTop, op.getPaintTextBlack(),
                darkPaint);
    }

    /**
     * 返回图片
     * @return
     */
    private Bitmap getImageBitmap() {
        Bitmap bitmap = null;
        if (image != null && image.get() != null) {
            bitmap = image.get();
        } else {
            int top = lc.gapV + lc.marginTop;
            bitmap = BitmapManager.createBitmap(lc.getIconWidth() + lc.gapH, lc.itemHeight + top,
                    Bitmap.Config.ARGB_8888);
            image = new SoftReference<Bitmap>(bitmap);
        }
        return bitmap;
    }

    public Rect getBounds() {
        return new Rect(panel.getLeft(), panel.getTop() - lc.cMarginTop, panel.getRight(), panel.getBottom());
    }

    /**
     * 返回当前拖拽的应用
     * @return
     */
    public ApplicationInfo hook() {
        return app;
    }

    /**
     * 是否正在拖拽
     */
    public boolean isMoving() {
        if (app != null)
            return true;
        return false;
    }

    /**
     * 移动到文件夹内
     * @param x
     * @param y
     * @param onMovingStopped
     */
    public void moveIntoFolder(int x, int y, final OnMovingStopped onMovingStopped) {
        Animation animation = op.createAnimationIconIntoFolder(x, y, panel.getLeft(), panel.getTop());
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation paramAnimation) {
                state = STATE_DEACTIVE;
                ApplicationInfo appInfo = app;
                app = null;
                if (onMovingStopped != null)
                    onMovingStopped.movingStopped(appInfo);
                handler.post(hider);
            }

            public void onAnimationRepeat(Animation paramAnimation) {
            }

            public void onAnimationStart(Animation paramAnimation) {
            }
        });
        state = STATE_MOVE_INTO_FOLDER;
        drawIcon(getImageBitmap());
        panel.startAnimation(animation);
        isAboveFolder = false;
    }

    /**
     * 拖拽至x、y坐标
     * @param x
     * @param y
     */
    public void moveTo(int x, int y) {
        int left = x - relX;
        int top = y - relY;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) panel.getLayoutParams();
        layoutParams.leftMargin = left;
        layoutParams.topMargin = top;
        panel.setLayoutParams(layoutParams);
        panel.layout(left, top, left + panel.getWidth(), top + panel.getHeight());
    }
    /**
     * 开始移动
     * @param info
     * @param x1
     * @param y1
     * @param x2 
     * @param y2
     */
    public void startMoving(ApplicationInfo info, int x1, int y1, int x2, int y2) {
        app = info;
        int i = x1 - lc.fullMarginLeftBlackCircle;
        int j = y1 - lc.cMarginTop;
        relX = (x2 - i);
        relY = (y2 - j);
        Bitmap bitmap = getImageBitmap();
        drawIcon(bitmap);
        panel.setImageBitmap(bitmap);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) panel.getLayoutParams();
        layoutParams.width = bitmap.getWidth();
        layoutParams.height = bitmap.getHeight();
        layoutParams.leftMargin = i;
        layoutParams.topMargin = j;
        panel.setLayoutParams(layoutParams);
        panel.layout(i, j, i + layoutParams.width, layoutParams.height);
        panel.setVisibility(View.VISIBLE);
        Animation animation = op.createAnimationIconExpand();
        panel.startAnimation(animation);
        state = STATE_EXPANDING;
    }

    /**
     * 停止拖拽，初始化数据
     * @return
     */
    public ApplicationInfo stopMoving() {
        ApplicationInfo appInfo = app;
        app = null;
        index = -1;
        pageIndex = -1;
        indexInFolder = -1;
        Drawable drawable = panel.getDrawable();
        panel.setImageDrawable(null);
        if (drawable != null)
            drawable.setCallback(null);
        return appInfo;
    }

    public void stopMoving(int x, int y, final OnMovingStopped onMovingStopped) {
        drawIcon(getImageBitmap());
        int i = x - (panel.getLeft() + lc.fullMarginLeftBlackCircle);
        int j = y - (panel.getTop() + lc.cMarginTop);
        Animation animation = op.createAnimationIconContract(i, j);
        if (onMovingStopped != null)
            animation.setAnimationListener(new Animation.AnimationListener() {

                public void onAnimationEnd(Animation animation) {
                    animation.setAnimationListener(null);
                    onMovingStopped.movingStopped(stopMoving());
                }

                public void onAnimationRepeat(Animation paramAnimation) {
                }

                public void onAnimationStart(Animation paramAnimation) {
                }
            });
        panel.startAnimation(animation);
        state = STATE_CONTRACTING;
    }

    public int getIndexInFolder() {
        return indexInFolder;
    }

    public void setIndexInFolder(int indexInFolder) {
        this.indexInFolder = indexInFolder;
    }

    public static abstract interface OnMovingStopped {
        public abstract void movingStopped(ApplicationInfo appInfo);
    }

    public void aboveFolder() {
        setAboveFolder(true);
        drawIcon(getImageBitmap());
        Animation animation = op.createAnimationAboveFolder();
        animation.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationEnd(Animation animation) {
                animation.setAnimationListener(null);
            }

            public void onAnimationRepeat(Animation paramAnimation) {
            }

            public void onAnimationStart(Animation paramAnimation) {
            }
        });
        panel.startAnimation(animation);
    }

    public void bisideFolder() {
        setAboveFolder(false);
        drawIcon(getImageBitmap());
        Animation animation = op.createAnimationBisideFolder();
        animation.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationEnd(Animation animation) {
                animation.setAnimationListener(null);
            }

            public void onAnimationRepeat(Animation paramAnimation) {
            }

            public void onAnimationStart(Animation paramAnimation) {
            }
        });
        panel.startAnimation(animation);
    }

    public boolean isAboveFolder() {
        return isAboveFolder;
    }

    public void setAboveFolder(boolean isAboveFolder) {
        this.isAboveFolder = isAboveFolder;
    }

    public int getsPageIndex() {
        return sPageIndex;
    }

    public void setsPageIndex(int sPageIndex) {
        this.sPageIndex = sPageIndex;
    }

    public int getsIndex() {
        return sIndex;
    }

    public void setsIndex(int sIndex) {
        this.sIndex = sIndex;
    }
}