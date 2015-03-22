package com.jchun.mylauncher.widget;

import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

public class SpringBoardPage extends View implements IPageView {
    private static final String TAG = "SpringBoardPage";
    private boolean jigging = false;
    private boolean messed = false;
    Vector<ApplicationInfo> icons = new Vector<ApplicationInfo>(LayoutCalculator.rows * LayoutCalculator.columns);
    private int selected = -1;
    private Point selectedLocation;
    public int shadowType = 1;
    LayoutCalculator lc;
    private ObjectPool pp;
    private boolean isAboveFolder = false;
    private int aboveIndex = -1;

    public SpringBoardPage(Context context) {
        super(context);
    }

    public SpringBoardPage(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public SpringBoardPage(Context context, AttributeSet attr, int style) {
        super(context, attr, style);
    }

    public void init(LayoutCalculator lc, ObjectPool pp) {
        this.lc = lc;
        this.pp = pp;
    }


    public boolean isJigging() {
        return jigging;
    }

    public void setJigging(boolean jigging) {
        this.jigging = jigging;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawNormal(canvas);
        if (jigging) {
            invalidate();
        }
    }

    private void drawNormal(Canvas canvas) {
        drawNormalRaw(canvas);
    }

    public void setIconIntoPage(int index, ApplicationInfo info) {
        if (index < 0 || index >= icons.size())
            return;
        ApplicationInfo oldApp = icons.get(index);
        if (oldApp != info) {
            messed = true;
        }
        icons.set(index, info);
        invalidateIcon(index);
    }

    private void invalidateIcon(int index) {
        if (index >= LayoutCalculator.iconsPerPage)
            return;
        Point point = getIconLocation(index);
        invalidate(lc.toItemRect(point.x, point.y));
    }

    public Point getIconLocation(int index) {
        int i = index / LayoutCalculator.columns;
        int j = index % LayoutCalculator.columns;
        Point point = new Point();
        int k = getVerticalGap();
        point.x = (lc.getMarginLeft() + lc.gapH + j * (lc.getIconWidth() + lc.gapH));
        point.y = (k + lc.getMarginTop() + i * (k + lc.getItemHeight()));
        return point;
    }

    private void drawNormalRaw(Canvas canvas) {
        Paint paint = pp.getPaintTextBlack();
        Paint iconPaint = null;
        int gapV = getVerticalGap();
        int y = gapV + lc.marginTop;
        int x;
        for (int i = 0; i < LayoutCalculator.rows; i++) {
            x = lc.marginLeft + lc.gapH;
            for (int j = 0; j < LayoutCalculator.columns; j++) {
                int index = j + i * LayoutCalculator.columns;
                if (index >= icons.size()) {
                    return;
                }
                if (index != selected) {
                    ApplicationInfo info = icons.get(index);
                    if (info != null) {
                        Transformation tFormation = new Transformation();
                        Transformation sFormation = new Transformation();
                        if (info.tAnim != null
                                && info.tAnim
                                        .getTransformation(AnimationUtils.currentAnimationTimeMillis(), tFormation)) {
                            float[] point = new float[2];
                            point[0] = 0.0F;
                            point[1] = 0.0F;
                            tFormation.getMatrix().mapPoints(point);
                            int x1 = (int) point[0];
                            int y1 = (int) point[1];
                            info.drawBoundIcon(lc, pp, canvas, x1, y1, paint, new Paint());
                            info.drawBlackCircle(lc, pp, canvas, x1 + lc.iconWidth, y1);
                        } else {
                            info.tAnim = null;
                            long time = AnimationUtils.currentAnimationTimeMillis();
                            if (info.sAnim != null && info.sAnim.getTransformation(time, sFormation)) {
                                if (info.getIsAboveFolder() == 1) {
                                    long startTime = info.sAnim.getStartTime();
                                    long duration = time - startTime;
                                    if (duration > info.sAnim.getDuration()) {
                                        duration = info.sAnim.getDuration();
                                    }
                                    float scale = 1 + (1.0f * duration / info.sAnim.getDuration()) * 0.4f;
                                    int x1 = (int) (lc.iconWidth * scale - lc.iconWidth) / 2;
                                    info.drawFolderBound(lc, pp, canvas, x - x1, y - x1, iconPaint, scale);
                                    info.drawIconContent(lc, pp, canvas, x, y, paint);
                                } else if (info.getIsAboveFolder() == 2) {
                                    long startTime = info.sAnim.getStartTime();
                                    long duration = time - startTime;
                                    if (duration > info.sAnim.getDuration()) {
                                        duration = info.sAnim.getDuration();
                                    }
                                    float scale = 1.4f - (1.0f * duration / info.sAnim.getDuration()) * 0.4f;
                                    int x1 = (int) (lc.iconWidth * scale - lc.iconWidth) / 2;
                                    info.drawFolderBound(lc, pp, canvas, x - x1, y - x1, iconPaint, scale);
                                    info.drawIconContent(lc, pp, canvas, x, y, paint);
                                } else {
                                    if (isAboveFolder && info.getIsAboveFolder() == 1) {
                                        int x1 = (int) (lc.iconWidth * 1.4f - lc.iconWidth) / 2;
                                        info.drawFolderBound(lc, pp, canvas, x - x1, y - x1, iconPaint, 1.4f);
                                        info.drawIconContent(lc, pp, canvas, x, y, paint);
                                    } else {
                                        drawNormalIcon(info, canvas, x, y, paint);
                                    }
                                }
                            } else {
                                info.sAnim = null;
                                if (isAboveFolder && info.getIsAboveFolder() == 1) {
                                    int x1 = (int) (lc.iconWidth * 1.4f - lc.iconWidth) / 2;
                                    info.drawFolderBound(lc, pp, canvas, x - x1, y - x1, iconPaint, 1.4f);
                                    info.drawIconContent(lc, pp, canvas, x, y, paint);
                                } else {
                                    drawNormalIcon(info, canvas, x, y, paint);
                                }

                            }
                        }

                    }
                } else {
                    ApplicationInfo info = icons.get(index);
                    if (info != null) {
                        iconPaint = pp.getPaintDarkener();
                        ApplicationInfo app = (ApplicationInfo) info;
                        app.drawBoundIcon(lc, pp, canvas, x, y, paint, iconPaint);
                        if (app.isJiggle()) {
                            app.drawBlackCircle(lc, pp, canvas, x + lc.iconWidth, y);
                        }
                    }
                }
                x += lc.iconWidth + lc.gapH;
            }
            y += gapV + lc.itemHeight;
        }
    }

    private void drawNormalIcon(ApplicationInfo info, Canvas canvas, int x, int y, Paint paint) {
        info.drawBoundIcon(lc, pp, canvas, x, y, paint, null);
        if (info.isJiggle()) {
            info.drawBlackCircle(lc, pp, canvas, x + lc.iconWidth, y);
        }
    }

    private int getVerticalGap() {
        return lc.gapV;
    }

    public void setIcons(ApplicationInfo[] icons) {
        for (ApplicationInfo applicationInfo : icons) {
            if (applicationInfo != null) {
                this.icons.add(applicationInfo);
            }
        }
    }

    public int getSelectedIndex() {
        return selected;
    }

    public void select(int index) {
        selected = index;
        Point point = getIconLocation(index);
        invalidate(lc.toItemRect(point.x, point.y));
        this.selectedLocation = point;
    }

    public void hitTest3(int x, int y, HitTestResult3 hitTest3) {
        hitTest3.index = -1;
        hitTest3.buttonRemove = false;
        //        Log.d(TAG, "X = " + x + "," + "Y = " + y);
        int i = lc.dpToPixel(12);
        int j = lc.dpToPixel(26) + i * 2;
        int k = lc.dpToPixel(26) + i * 2;
        int gapV = getVerticalGap();
        int top = gapV + lc.marginTop;
        int y1 = top;
        int left = lc.marginLeft + lc.gapH;
        //        Log.d(TAG, "gapV =" + gapV + ", top =" + top + ", y1 =" + y1 + ", left =" + left);
        Bitmap remove = pp.getBitmapBlackCircle();
        int rWidth = remove.getWidth();
        int rHeight = remove.getHeight();
        for (int row = 0; row < LayoutCalculator.rows; row++) {
            boolean inItemY = false;
            boolean inRemoveY = false;
            //            Log.d(TAG, "row = " + row + "     y1 = " + y1 + "," + "y2 = " + (y1 + k));
            //            Log.d(TAG,
            //                    "row = " + row + " remove    y1 = " + (y1 - lc.dpToPixel(10)) + "," + "y2 = "
            //                            + (y1 - lc.dpToPixel(10) + rHeight));
            if ((y >= y1) && (y < y1 + lc.iconHeight)) {
                inItemY = true;
            }
            if (y >= (y1 - lc.dpToPixel(10)) && y <= (y1 - lc.dpToPixel(10) + rHeight)) {
                inRemoveY = true;
            }
            //            Log.d(TAG, "inRemoveY " + inRemoveY + "inItemY " + inItemY);
            if (!inItemY && !inRemoveY) {
                y1 += top + lc.itemHeight;
                continue;
            }
            for (int column = 0; column < LayoutCalculator.columns; column++) {
                int index = column + row * LayoutCalculator.columns;
                int x1 = left;
                //                Log.d(TAG, "column = " + column + "    x1 = " + x1 + "," + "x2 = " + (x1 + j));
                if (inItemY && (x >= x1) && (x < x1 + lc.iconWidth)) {
                    hitTest3.index = index;
                    hitTest3.buttonRemove = false;
                }
                if (inRemoveY
                        && (x >= x1 + lc.iconWidth - lc.dpToPixel(10) && x < x1 + lc.iconWidth - lc.dpToPixel(10)
                                + rWidth)) {
                    hitTest3.index = index;
                    hitTest3.buttonRemove = true;
                }
                if (hitTest3.index != -1) {
                    return;
                }
                left += lc.iconWidth + lc.gapH;
            }
            y1 += top + lc.itemHeight;
        }
        hitTest3.index = -1;
    }

    public int hitTest2(int x, int y, HitTestResult3 hitTest2, boolean isFolder) {
        int left = lc.iconMarginLeft;
        int right = (lc.iconWidth + lc.gapH) * LayoutCalculator.columns - lc.iconMarginLeft;
        int top = getVerticalGap() + lc.marginTop;
        int y1 = top;
        //        Log.d(TAG, "x = " + x + ",y = " + y);
        if (x <= left) {
            return -1;
        }
        if (x >= right) {
            return 1;
        }
        int oldIndex = -1;
        if (icons.size() == 0) {
            hitTest2.index = 0;
            hitTest2.inIcon = false;
            return 0;
        }
        for (int i = 0; i < icons.size(); i++) {
            if (icons.get(i) == null) {
                oldIndex = i;
                break;
            }
        }
        if (oldIndex == -1) {
            oldIndex = icons.size();
        }
        int itemX1 = lc.gapH / 2;
        int count = icons.size();
        if (icons.get(count - 1) == null) {
            count--;
        }
        int currentRow = count / lc.columns + 1;
        if (y > currentRow * (top + lc.itemHeight)) {
          //如果y超过最后一行的y坐标，则放在最后一个。
            if (oldIndex < count) {
                hitTest2.index = count - 1;
            } else {
                hitTest2.index = count;
            }
            return 0;
        }
        for (int i = 0; i < LayoutCalculator.rows; i++) {
            //            Log.d(TAG, "y1 = " + y1 + "y2 = " + (y1 + lc.itemHeight));
            //            Log.d(TAG, "`````` y1 = " + y1 + ",y2 = " + (y1 + lc.itemHeight));
            if ((y >= y1) && (y < y1 + lc.itemHeight)) {

            } else {
                y1 += top + lc.itemHeight;
                continue;
            }
            for (int j = 0; j < LayoutCalculator.columns; j++) {
                int inType = -1;
                //                Log.d(TAG, "1111111111 x1 = " + left + ",x2 = " + (itemX1));
                //                Log.d(TAG, "2222222222 x1 = " + (itemX1) + ",x2 = " + (lc.iconWidth + itemX1));
                //                Log.d(TAG, "3333333333 x1 = " + (lc.iconWidth + itemX1) + ",x2 = "
                //                        + (lc.iconWidth + itemX1 - lc.gapH / 2));
                if (x > left && x <= itemX1) {
                    //item左半边
                    inType = 0;
                } else if (x > itemX1 && x < lc.iconWidth + itemX1) {
                    //item里面
                    inType = 2;
                } else if (x > lc.iconWidth + itemX1 && x < (lc.iconWidth + itemX1 + lc.gapH / 2)) {
                    //item右半边
                    inType = 1;
                }
                int position = j + i * LayoutCalculator.columns;
                if (position >= count) {
                  //如果y超过最后一行的y坐标，则放在最后一个。
                    if (oldIndex < count) {
                        hitTest2.index = count - 1;
                    } else {
                        hitTest2.index = count;
                    }
                    return 0;
                }
                if (inType == 0) {
                    if (position > oldIndex) {
                        if (position % LayoutCalculator.columns != 0) {
                            hitTest2.index = position - 1;
                            hitTest2.inIcon = false;
                            return 0;
                        }
                        return 2;
                    }
                    hitTest2.index = position;
                    hitTest2.inIcon = false;
                    return 0;
                } else if (inType == 1) {
                    if (position < oldIndex) {
                        if (position % LayoutCalculator.columns != LayoutCalculator.columns - 1) {
                            hitTest2.index = position + 1;
                            hitTest2.inIcon = false;
                            return 0;
                        }
                        return 2;
                    }
                    hitTest2.index = position;
                    hitTest2.inIcon = false;
                    return 0;
                } else if (inType == 2) {
                    hitTest2.index = position;
                    hitTest2.inIcon = false;
                    if (!isFolder) {
                        hitTest2.inIcon = true;
                        if (position >= icons.size() || (position < icons.size() && icons.get(position) == null)) {
                            hitTest2.inIcon = false;
                        }
                        return 0;
                    } else {
                        hitTest2.inIcon = false;
                        if (position < oldIndex) {
                            if (position % LayoutCalculator.columns != LayoutCalculator.columns - 1) {
                                hitTest2.index = position + 1;
                                return 0;
                            }
                            return 2;
                        } else if (position > oldIndex) {
                            if (position % LayoutCalculator.columns != 0) {
                                hitTest2.index = position - 1;
                                return 0;
                            }
                            return 2;
                        }
                    }

                    return 2;
                }
                if (j == 0) {
                    left = 0;
                }
                left += lc.iconWidth + lc.gapH;
                itemX1 += lc.iconWidth + lc.gapH;
            }
            y1 += top + lc.itemHeight;
        }
        return 2;
    }

    public ApplicationInfo getIcon(int index) {
        if ((index < 0) || (index >= icons.size()))
            return null;
        ApplicationInfo info = icons.get(index);
        return info;
    }

    public ApplicationInfo getSelectedApp() {
        if (selected >= 0 && selected < icons.size()) {
            return icons.get(selected);
        }
        return null;
    }

    public void deselect() {
        if (selected < 0)
            return;
        selected = -1;
        Point point = selectedLocation;
        invalidate(lc.toItemRect(point.x, point.y));
    }

    public void jiggle() {
        jigging = true;
        for (int i = 0; i < icons.size(); i++) {
            ApplicationInfo info = icons.get(i);
            if (info != null) {
                info.jiggle();
            }
        }
        invalidate();
    }

    public void unJiggle() {
        for (int i = 0; i < icons.size(); i++) {
            ApplicationInfo info = icons.get(i);
            if (info != null) {
                info.unJiggle();
            }
        }
        jigging = false;
    }

    public int getIconsCount() {
        int count = 0;
        for (int i = 0; i < icons.size(); i++) {
            if (icons.get(i) != null) {
                count++;
            }
        }
        return count;
    }

    public boolean setMoveTo(int index) {
        if (!icons.contains(null) && icons.size() >= LayoutCalculator.rows * LayoutCalculator.columns) {
            return false;
        }
        int oldIndex = -1;
        if (index < 0) {
            return false;
        }
        if (index >= icons.size() && index < LayoutCalculator.rows * LayoutCalculator.columns) {
            for (int i = 0; i < icons.size(); i++) {
                if (icons.get(i) == null) {
                    oldIndex = i;
                }
            }
            if (oldIndex == -1) {
                oldIndex = icons.size();
                icons.add(null);
            }
            int i = oldIndex;
            for (; i < icons.size() - 1; i++) {
                moveIcon(i, i + 1, 50 * (i - oldIndex));
            }
            icons.set(i, null);
            return true;
        }
        ApplicationInfo info = icons.get(index);
        if (info == null) {
            return true;
        }
        for (int i = 0; i < icons.size(); i++) {
            if (icons.get(i) == null) {
                oldIndex = i;
            }
        }
        if (oldIndex == -1) {
            oldIndex = icons.size();
            icons.add(null);
        }
        int i = 0;
        if (oldIndex > index) {
            for (i = oldIndex; i > index; i--) {
                moveIcon(i, i - 1, 50 * (oldIndex - i));
            }
        } else if (oldIndex < index) {
            for (i = oldIndex; i < index; i++) {
                moveIcon(i, i + 1, 50 * (i - oldIndex));
            }
        }
        icons.set(i, null);
        return true;
    }

    public void clearUp(ApplicationInfo info) {
        boolean isAdd = info == null ? true : false;
        for (int i = 0; i < icons.size(); i++) {
            ApplicationInfo app = icons.get(i);
            if (app == null) {
                if (info != null) {
                    icons.set(i, info);
                    isAdd = true;
                } else {
                    int j = i;
                    for (; j < icons.size() - 1; j++) {
                        moveIcon(j, j + 1, 50 * (j - i + 1));
                    }
                    icons.remove(j);
                }
                messed = true;
            } else {
                if (app.getType() == ItemInfo.TYPE_FOLDER) {
                    FolderInfo folder = (FolderInfo) app;
                    if (folder.getIcons().size() == 0) {
                        int j = i;
                        for (; j < icons.size() - 1; j++) {
                            moveIcon(j, j + 1, 50 * (j - i + 1));
                        }
                        icons.remove(j);
                    }
                    messed = true;
                }
            }
        }
        if (!isAdd) {
            icons.add(info);
            messed = true;
        }
    }

    public void addToFolder(int i, ApplicationInfo app) {
        ApplicationInfo info = icons.get(i);
        FolderInfo folder;
        if (info != null) {
            if (info.getType() == ItemInfo.TYPE_FOLDER) {
                folder = (FolderInfo) info;
                folder.addIcon(app);
            } else {
                folder = new FolderInfo();
                folder.addIcon(info);
                folder.addIcon(app);
                folder.setTitle("未命名");
            }
            setIconIntoPage(i, folder);
        }
    }

    private void moveIcon(int to, int from, long offset) {
        Point toP = getIconLocation(to);
        Point fromP = getIconLocation(from);
        ApplicationInfo app = icons.get(from);
        if ((to < LayoutCalculator.iconsPerPage) && (app != null)) {
            app.tAnim = new TranslateAnimation(fromP.x, toP.x, fromP.y, toP.y);
            app.tAnim.initialize(lc.getItemWidth() + lc.gapH, lc.getItemHeight(), getWidth(), getHeight());
            app.tAnim.setFillAfter(true);
            app.tAnim.setFillBefore(true);
            app.tAnim.setStartOffset(offset);
            app.tAnim.setDuration(200L);
            app.tAnim.startNow();
        }
        setIconIntoPage(to, app);
    }

    public void createFolderBound(int index) {
        aboveIndex = index;
        ApplicationInfo app = icons.get(index);
        if (isAboveFolder == false) {
            app.setIsAboveFolder(1);
            app.sAnim = new ScaleAnimation(1, 1.4F, 1, 1.4F);
            app.sAnim.initialize(lc.getItemWidth() + lc.gapH, lc.getItemHeight(), getWidth(), getHeight());
            app.sAnim.setFillAfter(true);
            app.sAnim.setFillBefore(true);
            app.sAnim.setDuration(200L);
            app.sAnim.startNow();
        }
        isAboveFolder = true;
    }

    public void removeFolderBound() {
        isAboveFolder = false;
        if (aboveIndex < 0) {
            return;
        }
        final ApplicationInfo app = icons.get(aboveIndex);
        if (app == null) {
            return;
        }
        aboveIndex = -1;
        app.setIsAboveFolder(2);
        app.sAnim = new ScaleAnimation(1.4F, 1, 1.4F, 1, 50, 50);
        app.sAnim.initialize(lc.getItemWidth() + lc.gapH, lc.getItemHeight(), getWidth(), getHeight());
        app.sAnim.setFillAfter(true);
        app.sAnim.setFillBefore(true);
        app.sAnim.setDuration(200L);
        app.sAnim.startNow();
        app.sAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                app.setIsAboveFolder(-1);
            }
        });
    }

    @Override
    public boolean isMessed() {
        return messed;
    }

    @Override
    public void removeApp(int index) {
        setIconIntoPage(index, null);
        clearUp(null);
    }

    @Override
    public void setMessed(boolean isMessed) {
        messed = isMessed;
    }

}
