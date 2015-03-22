package com.jchun.mylauncher.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Bitmap管理
 * @author JChun
 *
 */
public class BitmapManager {

    public static Bitmap copy(Bitmap paramBitmap, Config config, boolean isMutable) {
        Bitmap bitmap;
        try {
            bitmap = paramBitmap.copy(config, isMutable);
        } catch (OutOfMemoryError localOutOfMemoryError) {
            gc();
            bitmap = paramBitmap.copy(config, isMutable);
        }
        return bitmap;
    }

    public static Bitmap createBitmap(int width, int height, Config config) {
        Bitmap bitmap;
        try {
            Bitmap localBitmap2 = Bitmap.createBitmap(width, height, config);
            bitmap = localBitmap2;
        } catch (OutOfMemoryError localOutOfMemoryError) {
            gc();
            bitmap = Bitmap.createBitmap(width, height, config);
        }
        return bitmap;
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m, boolean filter) {
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(source, x, y, width, height, m, filter);
        } catch (OutOfMemoryError localOutOfMemoryError) {
            gc();
            bitmap = Bitmap.createBitmap(source, x, y, width, height, m, filter);
        }
        return bitmap;
    }

    public static Bitmap getBitmap(Resources res, int id, Options opts) {
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeResource(res, id, opts);
        } catch (OutOfMemoryError localOutOfMemoryError) {
            gc();
            bitmap = BitmapFactory.decodeResource(res, id, opts);
        }
        return bitmap;
    }

    public static Bitmap getBitmap(Context context, int id) {
        return getBitmap(context.getResources(), id, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap getBitmap(Context context, int id, Config config) {
        return getBitmap(context.getResources(), id, config);
    }

    public static Bitmap getBitmap(Resources resource, int id, Config config) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = config;
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeResource(resource, id, opts);
        } catch (OutOfMemoryError localOutOfMemoryError) {
            gc();
            bitmap = BitmapFactory.decodeResource(resource, id, opts);
        }
        return bitmap;
    }

    public static Bitmap getBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            drawable.draw(new Canvas(bitmap));
        }
        return bitmap;
    }

    private static void gc() {
        System.gc();
        System.runFinalization();
    }

    public static Bitmap decodeByteArray(byte[] paramArrayOfByte) {
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length);
        } catch (OutOfMemoryError localOutOfMemoryError) {
            gc();
            bitmap = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length);
        }
        return bitmap;
    }

    public static Bitmap createScaledBitmap(Bitmap bitmap, int iconWidth, int iconHeight, boolean filter) {
        Bitmap bitmap2;
        try {
            bitmap2 = Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, filter);
        } catch (OutOfMemoryError localOutOfMemoryError) {
            gc();
            bitmap2 = Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, filter);
        }
        return bitmap2;
    }
}