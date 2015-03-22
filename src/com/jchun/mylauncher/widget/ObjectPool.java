package com.jchun.mylauncher.widget;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.jchun.mylauncher.R;

public class ObjectPool {
    private Context ctx;
    private LayoutCalculator lc;
    private SoftReference<Paint> paintText;
    private SoftReference<Paint> paintTextBlack;
    private SoftReference<Paint> paintDarkener;
    private SoftReference<Bitmap> bitmapBlackCircle;
    private SoftReference<Bitmap> bitmapFolder;

    public ObjectPool(Context context, LayoutCalculator lc) {
        this.ctx = context;
        this.lc = lc;
    }

    public Paint getPaintTextMain() {
        Paint paint = null;
        if (paintText != null && paintText.get() != null) {
            paint = paintText.get();
        }
        if (paint == null) {
            paint = createPaintText();
            paintText = new SoftReference<Paint>(paint);
        }
        return paint;
    }

    public Paint getPaintTextBlack() {
        Paint paint = null;
        if (paintTextBlack != null && paintTextBlack.get() != null) {
            paint = paintTextBlack.get();
        }
        if (paint == null) {
            paint = createPaintTextBlack();
            paintTextBlack = new SoftReference<Paint>(paint);
        }
        return paint;
    }

    private Paint createPaintTextBlack() {
        Paint paint = new Paint();
        paint.setColor(ctx.getResources().getColor(android.R.color.black));
        paint.setTextSize(Math.min(lc.propIconHeight(lc.dpToPixel(14)), lc.dpToPixel(14)));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        return paint;
    }

    public Paint createPaintText() {
        Paint paint = new Paint();
        paint.setColor(-1);
        paint.setTextSize(Math.min(lc.propIconHeight(lc.dpToPixel(14)), lc.dpToPixel(14)));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        return paint;
    }

    public Paint getPaintDarkener() {
        Paint paint = null;
        if (paintDarkener != null && paintDarkener.get() != null) {
            paint = paintDarkener.get();
        } else {
            float[] array = new float[20];
            array[0] = 0.75F;
            array[1] = 0.0F;
            array[2] = 0.0F;
            array[3] = 0.0F;
            array[4] = 0.0F;
            array[5] = 0.0F;
            array[6] = 0.75F;
            array[7] = 0.0F;
            array[8] = 0.0F;
            array[9] = 0.0F;
            array[10] = 0.0F;
            array[11] = 0.0F;
            array[12] = 0.75F;
            array[13] = 0.0F;
            array[14] = 0.0F;
            array[15] = 0.0F;
            array[16] = 0.0F;
            array[17] = 0.0F;
            array[18] = 1.0F;
            array[19] = 0.0F;
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(array);
            paint = new Paint();
            paint.setColorFilter(filter);
            paint.setAlpha(224);
            paintDarkener = new SoftReference<Paint>(paint);
        }
        return paint;
    }

    public Animation createAnimationIconContract(int x, int y) {
        AnimationSet animation = new AnimationSet(true);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        float f1 = 0.2000001F * lc.iconWidth / 2.0F;
        float f2 = 0.2000001F * lc.iconHeight / 2.0F;
        TranslateAnimation translateAnimation = new TranslateAnimation(-f1, x, -f2, y);
        translateAnimation.setDuration(200L);
        animation.addAnimation(translateAnimation);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.2F, 1.0F, 1.2F, 1.0F);
        scaleAnimation.setDuration(200L);
        animation.addAnimation(scaleAnimation);
        return animation;
    }

    public Animation createAnimationAboveFolder() {
        AnimationSet animation = new AnimationSet(true);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.2F, 1.0F, 1.2F, 1.0F);
        scaleAnimation.setDuration(200L);
        animation.addAnimation(scaleAnimation);
        return animation;
    }

    public Animation createAnimationBisideFolder() {
        AnimationSet animation = new AnimationSet(true);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0F, 1.2F, 1.0F, 1.2F);
        scaleAnimation.setDuration(200L);
        animation.addAnimation(scaleAnimation);
        return animation;
    }

    public Animation createAnimationIconExpand() {
        AnimationSet animation = new AnimationSet(true);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        float f1 = 0.2000001F * lc.iconWidth / 2.0F;
        float f2 = 0.2000001F * lc.iconHeight / 2.0F;
        TranslateAnimation translateAnimation = new TranslateAnimation(0.0F, -f1, 0.0F, -f2);
        translateAnimation.setDuration(200L);
        animation.addAnimation(translateAnimation);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0F, 1.2F, 1.0F, 1.2F);
        scaleAnimation.setDuration(200L);
        animation.addAnimation(scaleAnimation);
        return animation;
    }

    public Animation createAnimationIconChange(int x1, int y1, int x2, int y2) {
        AnimationSet animation = new AnimationSet(true);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        TranslateAnimation translateAnimation = new TranslateAnimation(x1, x2, y1, y2);
        translateAnimation.setDuration(300L);
        animation.addAnimation(translateAnimation);
        return animation;
    }

    public Animation createAnimationIconIntoFolder(int x, int y, int left, int top) {
        AnimationSet animation = new AnimationSet(true);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        float scaleX = lc.folderIconSize / lc.iconWidth;
        float scaleY = lc.folderIconSize / lc.iconHeight;
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0F, scaleX, 1.0F, scaleY);
        scaleAnimation.setDuration(300L);
        animation.addAnimation(scaleAnimation);
        int toX = x - (int) (scaleX * lc.fullMarginLeftBlackCircle) - left + (lc.iconWidth / 2);
        int toY = y - (int) (scaleY * lc.cMarginTop) - top + +(lc.iconHeight / 2);
        TranslateAnimation translateAnimation = new TranslateAnimation(0.0F, toX, 0.0F, toY);
        translateAnimation.setDuration(300L);
        animation.addAnimation(translateAnimation);
        return animation;
    }

    public Animation createAnimationOpenFolder(int x, int y, float screenWidth, float screenHeight, boolean open) {
        AnimationSet animation = new AnimationSet(true);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        if (open) {
            float scaleX = lc.iconWidth / screenWidth;
            float scaleY = lc.iconHeight / screenHeight;
            ScaleAnimation scaleAnimation = new ScaleAnimation(scaleX, 1, scaleY, 1);
            scaleAnimation.setDuration(300L);
            animation.addAnimation(scaleAnimation);
            TranslateAnimation translateAnimation = new TranslateAnimation(x, 0, y, 0);
            translateAnimation.setDuration(300L);
            animation.addAnimation(translateAnimation);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0F, 1F);
            alphaAnimation.setDuration(300L);
            animation.addAnimation(alphaAnimation);
        } else {
            float scaleX = lc.iconWidth / screenWidth;
            float scaleY = lc.iconHeight / screenHeight;
            ScaleAnimation scaleAnimation = new ScaleAnimation(1, scaleX, 1, scaleY);
            scaleAnimation.setDuration(300L);
            animation.addAnimation(scaleAnimation);
            TranslateAnimation translateAnimation = new TranslateAnimation(0, x, 0, y);
            translateAnimation.setDuration(300L);
            animation.addAnimation(translateAnimation);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1F, 0F);
            alphaAnimation.setDuration(300L);
            animation.addAnimation(alphaAnimation);
        }

        return animation;
    }

    public Animation createAnimationPageShow(boolean show) {
        AnimationSet animation = new AnimationSet(true);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        AlphaAnimation alphaAnimation;
        if (show) {
            alphaAnimation = new AlphaAnimation(0F, 1F);
        } else {
            alphaAnimation = new AlphaAnimation(1F, 0F);
        }
        alphaAnimation.setDuration(300L);
        animation.addAnimation(alphaAnimation);
        return animation;
    }

    public Bitmap getBitmapBlackCircle() {
        Bitmap bitmap = null;
        if (bitmapBlackCircle != null) {
            bitmap = bitmapBlackCircle.get();
        } else {
            bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.black_circle_x);
        }
        return bitmap;
    }

    public Bitmap getFolderIcon() {
        Bitmap bitmap;
        if (bitmapFolder != null && bitmapFolder.get() != null) {
            bitmap = (Bitmap) this.bitmapFolder.get();
        } else {
            bitmap = stretch(BitmapManager.getBitmap(this.ctx, R.drawable.folder));
            bitmapFolder = new SoftReference<Bitmap>(bitmap);
        }
        return bitmap;
    }

    public Bitmap stretch(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getWidth() != lc.iconWidth || bitmap.getHeight() != lc.iconHeight) {
            bitmap = BitmapManager.createScaledBitmap(bitmap, lc.iconWidth, lc.iconHeight, true);
        }
        return bitmap;
    }

    public Bitmap stretch(Bitmap bitmap, float scaled) {
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getWidth() != lc.iconWidth * scaled || bitmap.getHeight() != lc.iconHeight * scaled) {
            bitmap = BitmapManager.createScaledBitmap(bitmap, (int) (lc.iconWidth * scaled),
                    (int) (lc.iconHeight * scaled), true);
        }
        return bitmap;
    }
}
