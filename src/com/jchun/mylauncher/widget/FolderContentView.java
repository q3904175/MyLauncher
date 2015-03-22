package com.jchun.mylauncher.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

/**
 * 文件夹拖拽View
 * @author JChun
 *
 */
public class FolderContentView extends View implements IPageView {
    private boolean jiggle = false;//是否为编辑模式
    private boolean messed = false;
    private int selected = -1;
    private Point selectedLocation;
    private LayoutCalculator lc;
    private ObjectPool pp;
    private int columns;
    private int rows;
    private int viewHeight;
    private FolderInfo mInfo;

    public FolderContentView(Context context) {
        super(context);
    }

    public FolderContentView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public FolderContentView(Context context, AttributeSet attr, int style) {
        super(context, attr, style);
    }

    public void init(LayoutCalculator lc, ObjectPool pp) {
        this.lc = lc;
        this.pp = pp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawNormal(canvas);
        if (jiggle) {
            //编辑状态时不断重绘
            invalidate();
        }
    }

    /**
     * 添加应用到视图
     */
    public void setIconIntoPage(int index, ApplicationInfo info) {
        if (index < 0 || index >= mInfo.getIcons().size())
            return;
        ApplicationInfo oldInfo = mInfo.getIcons().get(index);
        if (oldInfo != info) {
            messed = true;
        }
        mInfo.getIcons().set(index, info);
        invalidateIcon(index);
    }

    /**
     * 绘制应用所在范围
     * @param index
     */
    private void invalidateIcon(int index) {
        Point point = getIconLocation(index);
        invalidate(lc.toItemRect(point.x, point.y));
    }

    /**
     *  返回应用坐标
     */
    public Point getIconLocation(int index) {
        int i = index / columns;
        int j = index % columns;
        Point point = new Point();
        int k = getVerticalGap();
        point.x = (lc.getMarginLeft() + lc.gapH + j * (lc.getIconWidth() + lc.gapH));
        point.y = (k + lc.getMarginTop() + i * (k + lc.getItemHeight()));
        return point;
    }

    /**
     * 绘制文字和图片
     * @param canvas
     */
    private void drawNormal(Canvas canvas) {
        Paint paint = pp.getPaintTextBlack();//文字paint，字体大小和颜色等。
        Paint iconPaint = null;
        int gapV = getVerticalGap();
        int y = gapV;
        int x;
        for (int i = 0; i < rows; i++) {
            x = lc.marginLeft + lc.gapH;
            for (int j = 0; j < columns; j++) {
                int index = j + i * columns;
                if (index >= mInfo.getIcons().size()) {
                    //超出应用数位置不绘制。
                    return;
                }
                ApplicationInfo info = mInfo.getIcons().get(index);
                if (info != null) {
                    if (index != selected) {
                        Transformation animation = new Transformation();
                        //当前正在进行移动动画
                        if (info.tAnim != null
                                && info.tAnim.getTransformation(AnimationUtils.currentAnimationTimeMillis(), animation)) {
                            float[] point = new float[2];
                            point[0] = 0.0F;
                            point[1] = 0.0F;
                            //获取当前动画偏移位置
                            animation.getMatrix().mapPoints(point);
                            int x1 = (int) point[0];
                            int y1 = (int) point[1];
                            //向当前位置绘制应用
                            info.drawBoundIcon(lc, pp, canvas, x1, y1, paint, new Paint());
                            //绘制删除按钮
                            info.drawBlackCircle(lc, pp, canvas, x1 + lc.iconWidth, y1);
                        } else {
                            info.tAnim = null;
                            info.drawBoundIcon(lc, pp, canvas, x, y, paint, null);
                            if (info.isJiggle()) {
                                info.drawBlackCircle(lc, pp, canvas, x + lc.iconWidth, y);
                            }
                        }
                    } else {
                        //当前位置选中,图片颜色加深
                        iconPaint = pp.getPaintDarkener();
                        info.drawBoundIcon(lc, pp, canvas, x, y, paint, iconPaint);
                        if (info.isJiggle()) {
                            //编辑状态的显示删除图标
                            info.drawBlackCircle(lc, pp, canvas, x + lc.iconWidth, y);
                        }
                    }
                }
                x += lc.iconWidth + lc.gapH;
            }
            y += gapV + lc.itemHeight;
        }
    }

    /**
     * 获取行间距
     * @return
     */
    private int getVerticalGap() {
        return lc.dpToPixel(15);
    }

    /**
     * 返回当前选中位置
     */
    public int getSelectedIndex() {
        return selected;
    }

    /**
     * 选中应用位置
     */
    public void select(int index) {
        selected = index;
        Point point = getIconLocation(index);
        invalidate(lc.toItemRect(point.x, point.y));
        this.selectedLocation = point;
    }

    /**
     * 返回屏幕上点击的应用
     */
    public void hitTest3(int x, int y, HitTestResult3 hitTest3) {
        hitTest3.index = -1;
        hitTest3.buttonRemove = false;
        int gapV = getVerticalGap();
        int top = gapV + lc.marginTop;
        int y1 = top;
        int left = lc.marginLeft + lc.gapH;
        Bitmap remove = pp.getBitmapBlackCircle();
        int rWidth = remove.getWidth();//删除图标的宽
        int rHeight = remove.getHeight();//删除图标的高
        for (int row = 0; row < rows; row++) {
            boolean inItemY = false;
            boolean inRemoveY = false;
            if ((y >= y1) && (y < y1 + lc.iconHeight)) {
                //y在当前行范围内
                inItemY = true;
            }
            if (y >= (y1 - rHeight / 2) && y <= (y1 + rHeight / 2)) {
                //y在当前行删除按钮范围内
                inRemoveY = true;
            }
            if (!inItemY && !inRemoveY) {
                y1 += top + lc.itemHeight;//向下一行
                continue;
            }
            int x1 = left;
            for (int column = 0; column < columns; column++) {
                int index = column + row * columns;
                if (inItemY && x >= x1 && x < (x1 + lc.iconWidth)) {
                    //x在当前图标内
                    hitTest3.index = index;
                    hitTest3.buttonRemove = false;
                    return;
                }
                if (inRemoveY && x >= (x1 + lc.iconWidth - rWidth / 2) && x < (x1 + lc.iconWidth + rWidth / 2)) {
                    //x在当前图标的删除按钮内
                    hitTest3.index = index;
                    hitTest3.buttonRemove = true;
                    return;
                }
                x1 += lc.iconWidth + lc.gapH;//转向下一列
            }
            y1 += top + lc.itemHeight;//转向下一行
        }
        hitTest3.index = -1;//没有在范围内
    }

    /**
     * 判断拖拽到哪个位置
     */
    public int hitTest2(int x, int y, HitTestResult3 hitTest2, boolean isFolder) {
        int top = getVerticalGap() + lc.marginTop;
        int y1 = top;
        int oldIndex = -1;//起始位置
        if (mInfo.getIcons().size() == 0) {
            //如果当前没有应用，则目标位置为第一个
            hitTest2.index = 0;
            hitTest2.inIcon = false;
            return 0;
        }
        //查找起始位置，当前页中为null的位置即起始位置
        for (int i = 0; i < mInfo.getIcons().size(); i++) {
            if (mInfo.getIcons().get(i) == null) {
                oldIndex = i;
                break;
            }
        }
        if (oldIndex == -1) {
            //当前页中没有为空则表示这个应用是新增进来的，默认位置为最后一个。
            oldIndex = mInfo.getIcons().size();
        }
        int x1 = lc.gapH / 2;
        int count = mInfo.getIcons().size();//当前应用总数
        if (mInfo.getIcons().get(count - 1) == null) {
            //如果最后一个应用为null，则表示最后一个应用被拖拽出来，则当前总数-1
            count--;
        }
        //当前有应用的总行数
        int currentRow = count / columns + 1;
        if (y > currentRow * (top + lc.itemHeight)) {
            //如果y超过最后一行的y坐标，则放在最后一个。
            if (oldIndex < count) {
                hitTest2.index = count - 1;
            } else {
                hitTest2.index = count;
            }
            hitTest2.inIcon = false;
            return 0;//返回0表示有拖拽的位置可以放
        }
        for (int i = 0; i < rows; i++) {
            if (y < y1 || y > y1 + lc.itemHeight) {
                //超出当前行y坐标范围
                y1 += top + lc.itemHeight;
                continue;
            }
            for (int j = 0; j < columns; j++) {
                int inType = -1;
                if (x <= x1) {
                    //应用左半边
                    inType = 0;
                } else if (x > x1 && x < lc.iconWidth + x1) {
                    //应用里面
                    inType = 2;
                } else if (x > lc.iconWidth + x1 && x < (lc.iconWidth + x1 + lc.gapH / 2)) {
                    //应用右半边
                    inType = 1;
                }
                int position = j + i * columns;
                if (position > count - 1) {
                    //当前位置大于最后一个，则放在最后一个
                    if (oldIndex < count) {
                        hitTest2.index = count - 1;
                    } else {
                        hitTest2.index = count;
                    }
                    hitTest2.inIcon = false;
                    return 0;
                }
                if (inType == 0) {//在应用左半边
                    if (position > oldIndex) {
                        if (position % columns != 0) {
                            hitTest2.index = position - 1;
                            hitTest2.inIcon = false;
                            return 0;
                        }
                        return 2;//不操作
                    }
                    hitTest2.index = position;
                    hitTest2.inIcon = false;
                    return 0;
                } else if (inType == 1) {//在应用右半边
                    if (position < oldIndex) {
                        if (position % columns != columns - 1) {
                            hitTest2.index = position + 1;
                            hitTest2.inIcon = false;
                            return 0;
                        }
                        return 2;
                    }
                    hitTest2.index = position;
                    hitTest2.inIcon = false;
                    return 0;
                } else if (inType == 2) {//在应用里面
                    hitTest2.index = position;
                    hitTest2.inIcon = false;
                    return 0;
                }
                x1 += lc.iconWidth + lc.gapH;
            }
            y1 += top + lc.itemHeight;
        }
        return 2;
    }

    /**
     * 返回应用
     */
    public ApplicationInfo getIcon(int index) {
        if (index < 0 || index >= mInfo.getIcons().size())
            return null;
        ApplicationInfo info = mInfo.getIcons().get(index);
        return info;
    }

    /**
     * 返回当前选中的应用
     */
    public ApplicationInfo getSelectedApp() {
        if (selected >= 0 && selected < mInfo.getIcons().size()) {
            return mInfo.getIcons().get(selected);
        }
        return null;
    }

    /**
     * 取消选择
     */
    public void deselect() {
        if (selected < 0)
            return;
        selected = -1;
        Point point = selectedLocation;
        //重绘选择的位置
        invalidate(lc.toItemRect(point.x, point.y));
    }

    /**
     * 编辑状态
     */
    public void jiggle() {
        jiggle = true;
        for (int i = 0; i < mInfo.getIcons().size(); i++) {
            ApplicationInfo info = mInfo.getIcons().get(i);
            if (info != null) {
                info.jiggle();
            }
        }
        invalidate();
    }

    /**
     * 取消编辑状态
     */
    public void unJiggle() {
        for (int i = 0; i < mInfo.getIcons().size(); i++) {
            ApplicationInfo info = mInfo.getIcons().get(i);
            if (info != null) {
                info.unJiggle();
            }
        }
        jiggle = false;
    }

    /**
     * 返回应用数量
     */
    public int getIconsCount() {
        int count = 0;
        for (int i = 0; i < mInfo.getIcons().size(); i++) {
            if (mInfo.getIcons().get(i) != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * 移动应用
     */
    public boolean setMoveTo(int index) {
        int oldIndex = -1;//起始位置
        if (index < 0) {
            return false;
        }
        //查找起始位置，当前页中为null的位置即起始位置
        for (int i = 0; i < mInfo.getIcons().size(); i++) {
            if (mInfo.getIcons().get(i) == null) {
                oldIndex = i;
                break;
            }
        }
        if (oldIndex == -1) {
            //当前页中没有为空则表示这个应用是新增进来的，默认位置为最后一个。
            oldIndex = mInfo.getIcons().size();
            mInfo.getIcons().add(null);
        }
        if (index >= mInfo.getIcons().size()) {
            int i = oldIndex;
            for (; i < mInfo.getIcons().size() - 1; i++) {
                //向前移动应用
                moveIcon(i + 1, i, 50 * (i - oldIndex));
            }
            mInfo.getIcons().set(i, null);
            return true;//移动成功
        }
        ApplicationInfo info = mInfo.getIcons().get(index);
        //当前位置为null，则移动到当前位置
        if (info == null) {
            return true;
        }
        int i = 0;
        if (oldIndex > index) {
            for (i = oldIndex; i > index; i--) {
                //向后移动
                moveIcon(i - 1, i, 50 * (oldIndex - i));
            }
        } else if (oldIndex < index) {
            for (i = oldIndex; i < index; i++) {
                //向前移动
                moveIcon(i + 1, i, 50 * (i - oldIndex));
            }
        }
        mInfo.getIcons().set(i, null);
        return true;
    }

    /**
     * 清理应用列表
     */
    public void clearUp(ApplicationInfo info) {
        boolean isAdd = info == null ? true : false;
        for (int i = 0; i < mInfo.getIcons().size(); i++) {
            ApplicationInfo app = mInfo.getIcons().get(i);
            if (app == null) {
                if (info != null) {
                    mInfo.getIcons().set(i, info);
                    isAdd = true;
                } else {
                    int j = i;
                    for (; j < mInfo.getIcons().size() - 1; j++) {
                        //向前移动
                        moveIcon(j + 1, j, 50 * (j - i + 1));
                    }
                    //删除最后一个
                    mInfo.getIcons().remove(j);
                }
                messed = true;
            }
        }
        if (!isAdd) {
            mInfo.getIcons().add(info);
            messed = true;
        }
        //重新计算行数和视图高度
        rows = (mInfo.getIcons().size() - 1) / columns + 1;
        viewHeight = rows * (lc.getItemHeight() + getVerticalGap()) + getVerticalGap();
    }

    /**
     * 移动应用
     * @param from 起始位置
     * @param to 目标位置
     * @param offset 动画延迟
     */
    private void moveIcon(int from, int to, long offset) {
        Point fromP = getIconLocation(from);
        Point toP = getIconLocation(to);
        ApplicationInfo app = mInfo.getIcons().get(from);
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

    public int getViewHeight() {
        return viewHeight;
    }

    @Override
    public void removeFolderBound() {
    }

    @Override
    public void createFolderBound(int index) {
    }

    @Override
    public void addToFolder(int i, ApplicationInfo app) {

    }

    public void setFolderInfo(FolderInfo info) {
        mInfo = info;
        columns = 3;
        rows = (mInfo.getIcons().size() - 1) / columns + 1;
        viewHeight = rows * (lc.getItemHeight() + getVerticalGap()) + getVerticalGap();
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
