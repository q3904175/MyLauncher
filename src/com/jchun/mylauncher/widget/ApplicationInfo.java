package com.jchun.mylauncher.widget;

import com.jchun.mylauncher.activity.MyApplication;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalBitmap.BitmapCallBack;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.FloatMath;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * 应用
 * @author JChun
 *
 */
public class ApplicationInfo extends ItemInfo {
    public static final String LOAD_ICON = "LOAD_ICON";
    private Intent intent;
    public TranslateAnimation tAnim; //位移动画
    public ScaleAnimation sAnim; //缩放动画
    private int isAboveFolder = -1; // 1放大， 2缩小
    
    public ApplicationInfo() {
        type = TYPE_APP;
    }


    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    /**
     * 绘图标和文字
     */
    public void drawBoundIcon(LayoutCalculator lc, ObjectPool op, Canvas canvas, int x, int y, Paint paint,
            Paint iconPaint) {
        drawIcon(lc, op, canvas, x, y, iconPaint);
        drawTitle(lc, paint, canvas, x, y);
    }

    /**
     * 绘图标
     */
    public void drawIcon(LayoutCalculator lc, ObjectPool op, Canvas canvas, int x, int y, Paint iconPaint) {
        Bitmap icon = getIcon(lc);
        if (icon != null) {
            icon = op.stretch(icon);
            canvas.drawBitmap(icon, x, y, iconPaint);
        }
    }

    /**
     * 绘文件夹外框
     */
    public void drawFolderBound(LayoutCalculator lc, ObjectPool pp, Canvas canvas, int x, int y, Paint paint,
            float scale) {
        if (isAboveFolder != -1) {
            Bitmap icon = pp.getFolderIcon();
            if (icon != null) {
                icon = pp.stretch(icon, scale);
                canvas.drawBitmap(icon, x, y, paint);
            }
        }
    }

    /**
     * 绘删除图标
     */
    public void drawBlackCircle(LayoutCalculator lc, ObjectPool pp, Canvas canvas, int x, int y) {
        Bitmap bitmap = pp.getBitmapBlackCircle();
        x = x - bitmap.getWidth() / 2;
        y = y - bitmap.getHeight() / 2;
        canvas.drawBitmap(bitmap, x, y, null);
    }

    /**
     * 绘文字
     */
    public void drawTitle(LayoutCalculator lc, Paint paint, Canvas canvas, int x, int y) {
        paint.setAlpha(255);
        int i = x + lc.getIconWidth() / 2;
        if (!titleMeasured)
            measureTitle(paint, lc);
        canvas.drawText(text.toString(), i, y + lc.textTop, paint);
    }

    /**
     * 格式化文字，超出部分替换...
     */
    public void measureTitle(Paint paint, LayoutCalculator lc) {
        int itemWidth = lc.getItemWidth();
        float length = paint.measureText(text, 0, text.length());
        float scale = 1;
        if (length > itemWidth)
            scale = length / text.length();
        for (int j = (text.length() - (int) FloatMath.ceil((length - itemWidth) / scale)); paint.measureText(text, 0,
                text.length()) > itemWidth; --j) {
            text = (text.subSequence(0, j).toString() + "...");
        }
        titleMeasured = true;
    }

    /**
     * 获取图片
     */
    public Bitmap getIcon(LayoutCalculator lc) {
        Bitmap bitmap = null;
        if (icon != null) {
            bitmap = icon;
            return bitmap;
        }
        Bitmap cache = null;
        if (iconRef != null && iconRef.get() != null) {
            cache = iconRef.get();
            icon = cache;
            bitmap = icon;
        }
        if (bitmap == null && getImgUrl() != null && !"".equals(getImgUrl())) {
            BitmapDisplayConfig config = new BitmapDisplayConfig();
            config.setBitmapHeight(lc.iconHeight);
            config.setBitmapWidth(lc.iconWidth);
            FinalBitmap fb = MyApplication.getInstance().fb;
            fb.decodeBitmap(getImgUrl(), config, new BitmapCallBack() {

                @Override
                public void onLoadSuccess(Bitmap bitmap) {
                    setIcon(bitmap);
                    Intent intent = new Intent(ApplicationInfo.LOAD_ICON);
                    MyApplication.getInstance().sendBroadcast(intent);
                }
            });
        }
        return bitmap;
    }

    /**
     * 绘图标内容
     */
    public void drawIconContent(LayoutCalculator lc, ObjectPool op, Canvas canvas, int x, int y, Paint iconPaint) {
        drawIcon(lc, op, canvas, x, y, iconPaint);
    }

    public int getIsAboveFolder() {
        return isAboveFolder;
    }

    public void setIsAboveFolder(int isAboveFolder) {
        this.isAboveFolder = isAboveFolder;
    }

}
