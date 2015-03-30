package com.jchun.mylauncher.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.jchun.mylauncher.R;
import com.jchun.mylauncher.widget.ApplicationInfo;
import com.jchun.mylauncher.widget.CancellableQueueTimer;
import com.jchun.mylauncher.widget.Controller;
import com.jchun.mylauncher.widget.DotView;
import com.jchun.mylauncher.widget.FolderContentView;
import com.jchun.mylauncher.widget.FolderInfo;
import com.jchun.mylauncher.widget.FolderScrollView;
import com.jchun.mylauncher.widget.FolderView;
import com.jchun.mylauncher.widget.HitTestResult3;
import com.jchun.mylauncher.widget.IPageView;
import com.jchun.mylauncher.widget.IconMover;
import com.jchun.mylauncher.widget.IconMover.OnMovingStopped;
import com.jchun.mylauncher.widget.ItemInfo;
import com.jchun.mylauncher.widget.JiggleModeActivator;
import com.jchun.mylauncher.widget.LayoutCalculator;
import com.jchun.mylauncher.widget.ObjectPool;
import com.jchun.mylauncher.widget.PageScrollView;
import com.jchun.mylauncher.widget.SpringBoardPage;

public class MainActivity extends Activity {
    private final static String RECEIVER_ADD_APP = "RECEIVER_ADD_APP";
    private LinearLayout scrollView;
    private PageScrollView scrollContainer;
    private int screenWidth;
    private int screenHeight;
    private FrameLayout mFrame;
    private GestureDetector gd;
    private List<ApplicationInfo> list = new ArrayList<ApplicationInfo>();
    private LayoutCalculator lc;
    private Vector<ApplicationInfo[]> pages;
    private ObjectPool pp;
    private Handler handler;
    private float touchSlop;
    private int selectedPageIndex = 1;
    private HitTestResult3 hitTest2 = new HitTestResult3();
    private HitTestResult3 hitTest3 = new HitTestResult3();
    private IconMover mover;
    private DotView dotView;
    private RelativeLayout mContainer;
    private FolderView mFolderView;
    private int openFolderIndex = -1;
    private RelativeLayout mTouchController;
    private boolean needUpload = false;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(MainActivity.this, R.layout.main_activity, null);
        setContentView(view);
        mFrame = (FrameLayout) findViewById(R.id.frame);
        scrollView = (LinearLayout) findViewById(R.id.container);
        scrollContainer = (PageScrollView) findViewById(R.id.pageView);
        dotView = (DotView) findViewById(R.id.dotView);
        mContainer = (RelativeLayout) findViewById(R.id.springboard_container);
        mTouchController = (RelativeLayout) findViewById(R.id.touchController);
        gd = new GestureDetector(MainActivity.this, this.gestureListener);
        scrollContainer.setOnScrollChangedListener(scrollContainer_OnScrollChanged);
        mTouchController.setOnTouchListener(scrollContainer_OnTouch);
        lc = new LayoutCalculator(MainActivity.this);
        pp = new ObjectPool(MainActivity.this, lc);
        handler = new Handler();
        touchSlop = ViewConfiguration.get(MainActivity.this).getScaledTouchSlop();
        mover = new IconMover(view, lc, pp, handler);
        dotView.init(lc, pp);
        OnLayoutReady onLayoutReady = new OnLayoutReady();
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(onLayoutReady);
        IntentFilter filter = new IntentFilter(RECEIVER_ADD_APP);
        filter.addAction(ApplicationInfo.LOAD_ICON);
        registerReceiver(receiver, filter);
    };
    
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (RECEIVER_ADD_APP.equals(intent.getAction())) {
                SpringBoardPage page = (SpringBoardPage) getPage(getPageCount(), false);
                if (page.getIconsCount() >= LayoutCalculator.iconsPerPage) {
                    page = addNewPage();
                }
                ApplicationInfo info = new ApplicationInfo();
                info.setTitle("应用" + 88);
                info.setOrder(88);
                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
                page.clearUp(info);
                page.invalidate();
            } else if (ApplicationInfo.LOAD_ICON.equals(intent.getAction())) {
                if (mFolderView == null) {
                    for (int i = 1; i < getPageCount() + 1; i++) {
                        SpringBoardPage page = (SpringBoardPage) getPage(i, false);
                        page.invalidate();
                    }
                } else {
                    FolderContentView page = (FolderContentView) getPage(0, true);
                    page.invalidate();
                }

            }
        }
    };

    private OnGestureListener gestureListener = new OnGestureListener() {
        public boolean onDown(MotionEvent ev) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) <= 100.0F)
                return false;
            if (velocityX > 0) {
                scrollToLeft();
            } else {
                scrollToRight();
            }
            return true;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
    };

    private class OnLayoutReady implements OnGlobalLayoutListener {

        private OnLayoutReady() {
        }

        public void onGlobalLayout() {
            scrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            layoutReady();
            setupScrollView();
        }
    }

    private void layoutReady() {
        pages = new Vector<ApplicationInfo[]>();
        screenWidth = mFrame.getWidth();
        screenHeight = mFrame.getHeight();
        lc.layoutReady(mFrame);
    }

    private void setupScrollView() {
        AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                initDataFromDB(list);
                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                loaded();
                if (!needUpload) {
                    list.clear();
                    controller.initData(list);
                } else {
                    controller.onSynchronize();
                }
            }
        };
        task.execute("");
    }

    private void ensurePages(int count) {
        if (pages.size() < count)
            addPage();
    }

    private ApplicationInfo[] addPage() {
        ApplicationInfo[] infos = new ApplicationInfo[LayoutCalculator.rows * LayoutCalculator.columns];
        pages.add(infos);
        return infos;
    }

    public void loaded() {
        pages.clear();
        scrollView.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth, -1);
        LinearLayout emptyView = new LinearLayout(MainActivity.this);
        emptyView.setBackgroundDrawable(null);
        scrollView.addView(emptyView, params);
        LinearLayout emptyView2 = new LinearLayout(MainActivity.this);
        emptyView2.setBackgroundDrawable(null);
        scrollView.addView(emptyView2, params);
        ensurePages(1);
        for (int i = 0; i < list.size(); i++) {
            int page = i / LayoutCalculator.iconsPerPage + 1;
            int index = i % LayoutCalculator.iconsPerPage;
            ensurePages(page);
            ApplicationInfo[] infos = pages.get(page - 1);
            if ((index < infos.length) && (infos[index] == null))
                infos[index] = list.get(i);
        }
        for (int i = 0; i < pages.size(); i++) {
            SpringBoardPage page = new SpringBoardPage(MainActivity.this);
            page.init(lc, pp);
            page.setIcons(pages.get(i));
            scrollView.addView(page, scrollView.getChildCount() - 1, params);
        }
        scrollContainer.post(new Runnable() {

            @Override
            public void run() {
                scrollContainer.scrollTo(screenWidth, 0);
                scrollContainer.setVisibility(View.VISIBLE);
            }
        });
        dotView.setPages(getPageCount());
        dotView.setCurrentPage(0);
    }

    private void scrollToCurrent(int index) {
        scrollContainer.smoothScrollTo(index * screenWidth, 0);
        dotView.setCurrentPage(index - 1);
    }

    public void scrollToLeft() {
        IPageView page =getPage(selectedPageIndex, false);
        if (page != null) {
            page.deselect();
        }
        if (--selectedPageIndex <= 1) {
            selectedPageIndex = 1;
        } 
        scrollContainer.smoothScrollTo(selectedPageIndex * screenWidth, 0);
        dotView.setCurrentPage(selectedPageIndex - 1);
    }

    public void scrollToRight() {
        getPage(selectedPageIndex, false).deselect();
        if (++selectedPageIndex > scrollView.getChildCount() - 2) {
            selectedPageIndex = scrollView.getChildCount() - 2;
        }
        scrollContainer.smoothScrollTo(selectedPageIndex * screenWidth, 0);
        dotView.setCurrentPage(selectedPageIndex - 1);
    }

    private static double getDistance(float x1, float y1, float x2, float y2) {
        float x = x2 - x1;
        float y = y2 - y1;
        return Math.sqrt(x * x + y * y);
    }

    private OnTouchListener scrollContainer_OnTouch = new OnTouchListener() {
        private float x;
        private float y;
        boolean gdIntercept;
        private int scrollPointX;
        private int scrollPointY;
        IPageView currentPage;
        private CancellableQueueTimer jiggleModeWaiter;
        private CancellableQueueTimer moveToScrollWaiter;
        private CancellableQueueTimer moveIntoFolderWaiter;
        private CancellableQueueTimer startDragWaiter;
        private CancellableQueueTimer moveIconWaiter;
        private CancellableQueueTimer moveToDesktopWaiter;
        private int startIndex = -1;
        private boolean isDrag = false; //是否可以拖动
        private HitTestResult3 oldHitTest2 = new HitTestResult3();
        private FolderScrollView folderScrollView;
        private boolean isFolderActionDown = false;
        private boolean isDesktopActionDown = false;

        private Runnable jiggleDetacher = new Runnable() {

            @Override
            public void run() {
                if (mFolderView != null) {
                    mFolderView.jiggle();
                } else {
                    jma.jiggle();
                }
            }
        };
        private Runnable scrollToLeftDetacher = new Runnable() {

            @Override
            public void run() {
                if (mover.isAboveFolder()) {
                    mover.bisideFolder();
                    currentPage.removeFolderBound();
                    if (moveIntoFolderWaiter != null) {
                        moveIntoFolderWaiter.cancel();
                        moveIntoFolderWaiter = null;
                    }
                }
                scrollToLeft();
            }
        };
        private Runnable scrollToRightDetacher = new Runnable() {

            @Override
            public void run() {
                if (mover.isAboveFolder()) {
                    mover.bisideFolder();
                    currentPage.removeFolderBound();
                    if (moveIntoFolderWaiter != null) {
                        moveIntoFolderWaiter.cancel();
                        moveIntoFolderWaiter = null;
                    }
                }
                scrollToRight();
            }
        };
        private Runnable startDragDetacher = new Runnable() {

            @Override
            public void run() {
                isDrag = true;
                if (currentPage != null) {
                    detachIcon(currentPage, startIndex, currentPage.getSelectedIndex(), mFolderView != null);
                }
            }
        };
        private Runnable moveIconDetacher = new Runnable() {
            HitTestResult3 oldHitTest = new HitTestResult3();

            @Override
            public void run() {
                if (hitTest2.index >= 0) {
                    if (oldHitTest.index != hitTest2.index || oldHitTest.inIcon != hitTest2.inIcon) {
                        if (mover.isAboveFolder()) {
                            mover.bisideFolder();
                            currentPage.removeFolderBound();
                            if (moveIntoFolderWaiter != null) {
                                moveIntoFolderWaiter.cancel();
                                moveIntoFolderWaiter = null;
                            }
                        }
                        oldHitTest.index = hitTest2.index;
                        oldHitTest.inIcon = hitTest2.inIcon;
                    }
                    if (!hitTest2.inIcon) {
                        if (mover.isAboveFolder()) {
                            mover.bisideFolder();
                            currentPage.removeFolderBound();
                            if (moveIntoFolderWaiter != null) {
                                moveIntoFolderWaiter.cancel();
                                moveIntoFolderWaiter = null;
                            }
                        }
                        if (currentPage.setMoveTo(hitTest2.index)) {
                            if (mFolderView != null) {
                                mFolderView.initLayout();
                            }
                            mover.setIndex(hitTest2.index);
                            mover.setPageIndex(selectedPageIndex);
                            mover.setsIndex(hitTest2.index);
                            mover.setsPageIndex(selectedPageIndex);
                        } else {
                            mover.setIndex(mover.getsIndex());
                            mover.setPageIndex(mover.getsPageIndex());
                        }
                    } else {
                        if (!mover.isAboveFolder()) {
                            mover.aboveFolder();
                            mover.setIndex(hitTest2.index);
                            mover.setPageIndex(selectedPageIndex);
                            currentPage.createFolderBound(hitTest2.index);
                            if (moveIntoFolderWaiter == null) {
                                moveIntoFolderWaiter = new CancellableQueueTimer(handler,
                                        ViewConfiguration.getLongPressTimeout(), moveIntoFolderDetacher);
                            }
                        }
                    }
                } else {
                    mover.setIndex(mover.getsIndex());
                    mover.setPageIndex(mover.getsPageIndex());
                }
                moveIconWaiter = null;
            }
        };
        private Runnable moveIntoFolderDetacher = new Runnable() {

            @Override
            public void run() {
                if (hitTest2.index > 0) {
                    IPageView page = getPage(selectedPageIndex, false);
                    ApplicationInfo info = page.getIcon(hitTest2.index);
                    if (info != null && info.getType() == ItemInfo.TYPE_FOLDER) {
                        FolderInfo folder = (FolderInfo) info;
                        openFolderIndex = hitTest2.index;
                        openFolder(folder);
                    }
                }
                moveIntoFolderWaiter = null;
            }
        };

        private Runnable moveToDesktopDetacher = new Runnable() {

            @Override
            public void run() {
                currentPage.clearUp(null);
                closeFolder();
                getPage(selectedPageIndex, false).clearUp(null);
            }
        };

        public boolean onTouchFolder(View v, final MotionEvent ev) {
            switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mover.isMoving()) {
                    return false;
                }
                x = ev.getX();
                y = ev.getY();
                isDrag = false;
                oldHitTest2.index = -1;
                oldHitTest2.inIcon = false;
                folderScrollView = mFolderView.getScrollView();
                mover.setAboveFolder(false);
                scrollPointY = folderScrollView.getScrollY();
                currentPage = getPage(0, true);
                if (x < mFolderView.getTranslateLeft() || y < mFolderView.getTranslateTop()
                        || x > folderScrollView.getWidth() + mFolderView.getTranslateLeft()
                        || y > folderScrollView.getHeight() + mFolderView.getTranslateTop()) {
                    closeFolder();
                }
                isDesktopActionDown = false;
                isFolderActionDown = true;
                if (currentPage != null) {
                    currentPage.hitTest3((int) x - mFolderView.getTranslateLeft(),
                            (int) y - mFolderView.getTranslateTop() + scrollPointY, hitTest3);
                    if (hitTest3.index >= 0) {
                        currentPage.select(hitTest3.index);
                        ApplicationInfo info = currentPage.getIcon(hitTest3.index);
                        if (info != null) {
                            if (mFolderView.isJiggling()) {
                                if (!hitTest3.buttonRemove) {
                                    if (startDragWaiter == null) {
                                        startDragWaiter = new CancellableQueueTimer(handler, 200, startDragDetacher);
                                    }
                                }
                            } else {
                                hitTest3.buttonRemove = false;
                            }
                        }
                    }

                }
                if (!mFolderView.isJiggling() && jiggleModeWaiter == null) {
                    jiggleModeWaiter = new CancellableQueueTimer(handler, ViewConfiguration.getLongPressTimeout(),
                            jiggleDetacher);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!isFolderActionDown) {
                    folderScrollView = mFolderView.getScrollView();
                    x = ev.getX();
                    y = ev.getY();
                    mover.setAboveFolder(false);
                    scrollPointY = folderScrollView.getScrollY();
                    currentPage = getPage(0, true);
                    isFolderActionDown = true;
                    isDesktopActionDown = false;
                }
                if (mFolderView.isJiggling()) {
                    if (getDistance(ev.getX(), ev.getY(), x, y) <= touchSlop) {
                        if (currentPage != null) {
                            if (isDrag) {
                                detachIcon(currentPage, startIndex, currentPage.getSelectedIndex(), true);
                            }
                        }
                    } else {
                        if (currentPage != null) {
                            currentPage.deselect();
                        }
                        if (startDragWaiter != null) {
                            startDragWaiter.cancel();
                            startDragWaiter = null;
                        }
                        hitTest3.buttonRemove = false;
                    }
                    if (mover.isMoving()) {
                        Point point = new Point((int) ev.getX(), (int) ev.getY());
                        mover.moveTo(point.x, point.y);
                        mFolderView.invalidate(mover.getBounds());
                        if (currentPage != null) {
                            if (point.x < mFolderView.getTranslateLeft() || point.y < mFolderView.getTranslateTop()
                                    || point.x > folderScrollView.getWidth() + mFolderView.getTranslateLeft()
                                    || point.y > folderScrollView.getHeight() + mFolderView.getTranslateTop()) {
                                if (moveToDesktopWaiter == null) {
                                    moveToDesktopWaiter = new CancellableQueueTimer(handler,
                                            ViewConfiguration.getLongPressTimeout(), moveToDesktopDetacher);
                                }
                            } else {
                                if (moveToDesktopWaiter != null) {
                                    moveToDesktopWaiter.cancel();
                                    moveToDesktopWaiter = null;
                                }
                            }
                            if (point.y - mFolderView.getTranslateTop() < folderScrollView.getHeight() / 3) {
                                folderScrollView.scrollBy(0, -10);
                                scrollPointY = folderScrollView.getScrollY();
                            }
                            if (point.y - mFolderView.getTranslateTop() > folderScrollView.getHeight() * 2 / 3) {
                                mFolderView.getScrollView().scrollBy(0, 10);
                                scrollPointY = folderScrollView.getScrollY();
                            }
                            int position = currentPage.hitTest2(point.x - mFolderView.getTranslateLeft(), point.y
                                    - mFolderView.getTranslateTop() + scrollPointY, hitTest2, false);
                            if (position == 0) {
                                if (hitTest2.index >= 0) {
                                    if (oldHitTest2.index != hitTest2.index || oldHitTest2.inIcon != hitTest2.inIcon) {
                                        oldHitTest2.index = hitTest2.index;
                                        oldHitTest2.inIcon = hitTest2.inIcon;
                                        if (moveIconWaiter != null) {
                                            moveIconWaiter.cancel();
                                            moveIconWaiter = null;
                                        }
                                        moveIconWaiter = new CancellableQueueTimer(handler, 100, moveIconDetacher);
                                    }
                                }
                            } else {
                                if (moveIconWaiter != null) {
                                    moveIconWaiter.cancel();
                                    moveIconWaiter = null;
                                }
                                if (mover.isAboveFolder()) {
                                    mover.bisideFolder();
                                    mover.setIndex(mover.getsIndex());
                                    mover.setPageIndex(mover.getsPageIndex());
                                    currentPage.removeFolderBound();
                                    if (moveIntoFolderWaiter != null) {
                                        moveIntoFolderWaiter.cancel();
                                        moveIntoFolderWaiter = null;
                                    }
                                }
                            }
                            if (moveToScrollWaiter != null) {
                                moveToScrollWaiter.cancel();
                                moveToScrollWaiter = null;
                            }
                        }
                        return true;
                    } else {
                        folderScrollView.scrollTo(0, (int) (scrollPointY - (ev.getY() - y)));
                    }
                } else {
                    folderScrollView.scrollTo(0, (int) (scrollPointY - (ev.getY() - y)));
                    if (getDistance(ev.getX(), ev.getY(), x, y) > touchSlop) {
                        if (jiggleModeWaiter != null) {
                            jiggleModeWaiter.cancel();
                            jiggleModeWaiter = null;
                        }
                        if (currentPage != null) {
                            if (currentPage.getSelectedIndex() >= 0) {
                                currentPage.deselect();
                            }
                        }
                    }

                }
                break;
            case MotionEvent.ACTION_CANCEL:
                currentPage.deselect();
            case MotionEvent.ACTION_UP:
                isDesktopActionDown = false;
                isFolderActionDown = false;
                if (jiggleModeWaiter != null) {
                    jiggleModeWaiter.cancel();
                    jiggleModeWaiter = null;
                }
                if (moveToScrollWaiter != null) {
                    moveToScrollWaiter.cancel();
                    moveToScrollWaiter = null;
                }
                if (startDragWaiter != null) {
                    startDragWaiter.cancel();
                    startDragWaiter = null;
                }
                if (moveToDesktopWaiter != null) {
                    moveToDesktopWaiter.cancel();
                    moveToDesktopWaiter = null;
                }
                final IPageView currentPage = getPage(selectedPageIndex, true);
                if (currentPage != null) {
                    final int select = currentPage.getSelectedIndex();
                    if (select >= 0) {
                        ApplicationInfo info = currentPage.getSelectedApp();
                        if (info != null) {
                            if (!mFolderView.isJiggling()) {
                                controller.onAppClick(info);
                            } else {
                                if (hitTest3.buttonRemove) {
                                    currentPage.removeApp(hitTest3.index);
                                    controller.onAppRemove(info);
                                }
                            }
                        }
                    }
                    currentPage.deselect();
                }
                if (mover.isMoving()) {
                    final IPageView p = getPage(mover.getPageIndex(), true);
                    Point point = p.getIconLocation(mover.getIndex());
                    final ApplicationInfo app = mover.hook();
                    mover.stopMoving(point.x + mFolderView.getTranslateLeft(), point.y + mFolderView.getTranslateTop()
                            - scrollPointY, new OnMovingStopped() {
                        @Override
                        public void movingStopped(ApplicationInfo appInfo) {
                            p.clearUp(app);
                        }
                    });
                }
                startIndex = -1;
                return true;
            }
            return false;

        }

        @Override
        public boolean onTouch(View v, final MotionEvent ev) {
            hitTest2.index = -1;
            hitTest2.inIcon = false;
            hitTest2.buttonRemove = false;
            if (mFolderView != null) {
                return onTouchFolder(v, ev);
            }
            switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mover.isMoving()) {
                    return false;
                }
                isDrag = false;
                oldHitTest2.index = -1;
                oldHitTest2.inIcon = false;
                mover.setAboveFolder(false);
                x = ev.getX();
                y = ev.getY();
                scrollPointX = scrollContainer.getScrollX();
                int index = scrollPointX / screenWidth;
                startIndex = index;
                currentPage = getPage(index, false);
                isDesktopActionDown = true;
                isFolderActionDown = false;
                if (currentPage != null) {
                    currentPage.hitTest3((int) x, (int) y, hitTest3);
                    if (hitTest3.index >= 0) {
                        currentPage.select(hitTest3.index);
                        ApplicationInfo info = currentPage.getIcon(hitTest3.index);
                        if (info != null) {
                            if (jma.isJiggling()) {
                                if (!hitTest3.buttonRemove) {
                                    if (startDragWaiter == null) {
                                        startDragWaiter = new CancellableQueueTimer(handler, 200, startDragDetacher);
                                    }
                                }
                            } else {
                                hitTest3.buttonRemove = false;
                            }
                        }
                    }

                }
                gdIntercept = gd.onTouchEvent(ev);
                if (!jma.isJiggling() && jiggleModeWaiter == null) {
                    jiggleModeWaiter = new CancellableQueueTimer(handler, ViewConfiguration.getLongPressTimeout(),
                            jiggleDetacher);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                int currentPointX = scrollContainer.getScrollX();
                index = currentPointX / screenWidth;
                currentPage = getPage(index, false);
                if (!isDesktopActionDown) {
                    x = ev.getX();
                    y = ev.getY();
                    scrollPointX = scrollContainer.getScrollX();
                    startIndex = index;
                    isFolderActionDown = false;
                    isDesktopActionDown = true;
                }
                if (!gdIntercept) {
                    if (jma.isJiggling()) {
                        if (getDistance(ev.getX(), ev.getY(), x, y) <= touchSlop) {
                            if (currentPage != null) {
                                if (isDrag) {
                                    detachIcon(currentPage, startIndex, currentPage.getSelectedIndex(), false);
                                }
                            }
                        } else {
                            if (currentPage != null) {
                                currentPage.deselect();
                            }
                            if (startDragWaiter != null) {
                                startDragWaiter.cancel();
                                startDragWaiter = null;
                            }
                            hitTest3.buttonRemove = false;
                        }
                        if (mover.isMoving()) {
                            Point point = new Point((int) ev.getX(), (int) ev.getY());
                            mover.moveTo(point.x, point.y);
                            mFrame.invalidate(mover.getBounds());
                            if (currentPage != null) {
                                int position = currentPage.hitTest2(point.x, point.y, hitTest2,
                                        mover.hook().getType() == ItemInfo.TYPE_FOLDER);
                                if (position == -1) {
                                    if (moveToScrollWaiter == null) {
                                        moveToScrollWaiter = new CancellableQueueTimer(handler,
                                                ViewConfiguration.getLongPressTimeout(), scrollToLeftDetacher);
                                    }
                                    return true;
                                } else if (position == 1) {
                                    if (moveToScrollWaiter == null) {
                                        moveToScrollWaiter = new CancellableQueueTimer(handler,
                                                ViewConfiguration.getLongPressTimeout(), scrollToRightDetacher);
                                    }
                                    return true;
                                } else if (position == 0) {
                                    if (hitTest2.index >= 0) {
                                        if (oldHitTest2.index != hitTest2.index
                                                || oldHitTest2.inIcon != hitTest2.inIcon) {
                                            oldHitTest2.index = hitTest2.index;
                                            oldHitTest2.inIcon = hitTest2.inIcon;
                                            if (moveIconWaiter != null) {
                                                moveIconWaiter.cancel();
                                                moveIconWaiter = null;
                                            }
                                            moveIconWaiter = new CancellableQueueTimer(handler, 100, moveIconDetacher);
                                        }
                                    }
                                } else {
                                    if (moveIconWaiter != null) {
                                        moveIconWaiter.cancel();
                                        moveIconWaiter = null;
                                    }
                                    if (mover.isAboveFolder()) {
                                        mover.bisideFolder();
                                        mover.setIndex(mover.getsIndex());
                                        mover.setPageIndex(mover.getsPageIndex());
                                        currentPage.removeFolderBound();
                                        if (moveIntoFolderWaiter != null) {
                                            moveIntoFolderWaiter.cancel();
                                            moveIntoFolderWaiter = null;
                                        }
                                    }
                                }
                                if (moveToScrollWaiter != null) {
                                    moveToScrollWaiter.cancel();
                                    moveToScrollWaiter = null;
                                }
                            }
                            return true;
                        } else {
                            scrollContainer.scrollTo((int) (scrollPointX - (ev.getX() - x)), 0);
                        }
                    } else {
                        scrollContainer.scrollTo((int) (scrollPointX - (ev.getX() - x)), 0);
                        if (getDistance(ev.getX(), ev.getY(), x, y) > touchSlop) {
                            if (jiggleModeWaiter != null) {
                                jiggleModeWaiter.cancel();
                                jiggleModeWaiter = null;
                            }
                            if (currentPage != null) {
                                if (currentPage.getSelectedIndex() >= 0) {
                                    currentPage.deselect();
                                }
                            }
                        }

                    }
                }
                if (isOverScrolling()) {
                    float f = ev.getX() - x;
                    scrollContainer.scrollTo((int) (scrollPointX - f / 2.0F), 0);
                    return true;
                }
                gdIntercept = gd.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (currentPage != null) {
                    currentPage.deselect();
                }
            case MotionEvent.ACTION_UP:
                if (jiggleModeWaiter != null) {
                    jiggleModeWaiter.cancel();
                    jiggleModeWaiter = null;
                }
                if (moveToScrollWaiter != null) {
                    moveToScrollWaiter.cancel();
                    moveToScrollWaiter = null;
                }
                if (startDragWaiter != null) {
                    startDragWaiter.cancel();
                    startDragWaiter = null;
                }
                gdIntercept = gd.onTouchEvent(ev);
                if (isOverScrolling()) {
                    scrollToCurrent(selectedPageIndex);
                } else {
                    if (!gdIntercept && !mover.isMoving()) {
                        if (Math.abs((ev.getX() - x)) > screenWidth / 2) {
                            if (ev.getX() - x > 0) {
                                scrollToLeft();
                            } else {
                                scrollToRight();
                            }
                        } else {
                            scrollToCurrent(selectedPageIndex);
                        }
                    }
                }
                isDesktopActionDown = false;
                isFolderActionDown = false;
                final IPageView currentPage = getPage(selectedPageIndex, false);
                if (currentPage != null) {
                    final int select = currentPage.getSelectedIndex();
                    if (select >= 0) {
                        ApplicationInfo info = currentPage.getSelectedApp();
                        if (info != null) {
                            if (!jma.isJiggling()) {
                                if (info.getType() == ItemInfo.TYPE_FOLDER) {
                                    openFolderIndex = select;
                                    openFolder((FolderInfo) info);
                                } else {
                                    controller.onAppClick(info);
                                }
                            } else {
                                if (hitTest3.buttonRemove) {
                                    currentPage.removeApp(hitTest3.index);
                                    controller.onAppRemove(info);
                                }
                                if (info.getType() == ItemInfo.TYPE_FOLDER && !isDrag) {
                                    openFolderIndex = select;
                                    openFolder((FolderInfo) info);
                                }
                            }

                        }
                    }
                    currentPage.deselect();
                }
                if (mover.isMoving()) {
                    for (int i = 1; i < getPageCount() + 1; i++) {
                        if (i != mover.getPageIndex()) {
                            getPage(i, false).clearUp(null);
                        }
                    }
                    final IPageView p = getPage(mover.getPageIndex(), false);
                    if (p != null) {
                        Point point = p.getIconLocation(mover.getIndex());
                        final ApplicationInfo app = mover.hook();
                        final int i = mover.getIndex();
                        if (!mover.isAboveFolder()) {
                            mover.stopMoving((mover.getPageIndex() - selectedPageIndex) * screenWidth + point.x,
                                    point.y, new OnMovingStopped() {
                                        @Override
                                        public void movingStopped(ApplicationInfo appInfo) {
                                            p.clearUp(app);
                                        }
                                    });

                        } else {
                            mover.setAboveFolder(false);
                            p.removeFolderBound();
                            mover.moveIntoFolder((mover.getPageIndex() - selectedPageIndex) * screenWidth + point.x,
                                    point.y, new OnMovingStopped() {
                                        @Override
                                        public void movingStopped(ApplicationInfo appInfo) {
                                            p.addToFolder(i, app);
                                            p.clearUp(null);
                                        }
                                    });
                        }
                    }
                }
                startIndex = -1;
                return true;
            }
            return false;
        }

        private void detachIcon(IPageView page, int pageIndex, int index, boolean isFolder) {

            ApplicationInfo info = page.getIcon(index);
            if (info == null)
                return;
            page.deselect();
            Point point = page.getIconLocation(index);
            if (!mover.isMoving()) {
                if (isFolder) {
                    mover.startMoving(info, point.x + mFolderView.getTranslateLeft(), mFolderView.getTranslateTop()
                            + point.y - folderScrollView.getScrollY(), (int) x, (int) y);
                } else {
                    mover.startMoving(info, point.x, point.y, (int) x, (int) y);
                    mover.setPageIndex(pageIndex);
                    mover.setsPageIndex(pageIndex);
                }
                mover.setIndex(index);
                mover.setsIndex(index);
            }
            page.setIconIntoPage(index, null);
        }
    };

    @Override
    public void onBackPressed() {
        if (jma.isJiggling()) {
            jma.unjiggle();
            IPageView page = getPage(selectedPageIndex, false);
            if (page.isMessed()) {
                onSynchronize();
            }
        } else {
            if (mFolderView != null) {
                if (mFolderView.isJiggling()) {
                    mFolderView.unJiggle();
                    onSynchronize();
                } else {
                    closeFolder();
                }
            }
        }
    };

    private void onSynchronize() {
        boolean isMessed = false;
        if (mFolderView != null) {
            IPageView page = getPage(0, true);
            if (page.isMessed()) {
                isMessed = true;
                page.setMessed(false);
            }
        }
        for (int i = 1; i < getPageCount() + 1; i++) {
            IPageView page = getPage(i, false);
            if (page.isMessed()) {
                isMessed = true;
                page.setMessed(false);
            }
        }
        if (isMessed) {
            onSynchronizeDB();
            controller.onSynchronize();
        }
    }

    private void onSynchronizeDB() {
        
    }

    private boolean isOverScrolling() {
        int x = scrollContainer.getScrollX();
        if (x < screenWidth)
            return true;
        else {
            if (x > (scrollView.getChildCount() - 2) * this.screenWidth) {
                return true;
            }
        }
        return false;
    }

    public static abstract interface OnPageScrollListener {
        public abstract void onPageScroll(int paramInt);
    }

    public IPageView getPage(int index, boolean isFolder) {
        IPageView page = null;
        if (isFolder && mFolderView != null) {
            return mFolderView.getContentView();
        }
        if ((index <= 0) || (index > getPageCount()))
            return null;
        View view = scrollView.getChildAt(index);
        if (view instanceof SpringBoardPage)
            page = (SpringBoardPage) view;
        return page;
    }

    public int getPageCount() {
        return scrollView.getChildCount() - 2;
    }

    private OnScrollChangedListener scrollContainer_OnScrollChanged = new OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
        }
    };

    private JiggleModeActivator jma = new JiggleModeActivator() {
        public boolean isJigglable() {
            int i = scrollContainer.getScrollX();
            if ((i >= screenWidth) && (i % screenWidth == 0))
                return true;
            return false;
        }

        public void jiggle() {
            setState(JiggleModeActivator.STATE_JIGGLE);
            if (mFolderView != null) {
                mFolderView.jiggle();
            } else {
                addNewPage();
                for (int i = 1; i < scrollView.getChildCount() - 1; i++) {
                    getPage(i, false).jiggle();
                }
            }
        }

        public void unjiggle() {
            setState(JiggleModeActivator.STATE_UNJIGGLE);
            for (int i = scrollView.getChildCount() - 2; i >= 1; i--) {
                IPageView page = getPage(i, false);
                page.unJiggle();
                if (page.getIconsCount() == 0) {
                    scrollView.removeViewAt(i);
                    if (getCurrentPageIndex() == i && getCurrentPageIndex() == getPageCount() + 1) {
                        scrollToLeft();
                    }
                }
            }
            dotView.setPages(getPageCount());
        }
    };

    public int getCurrentPageIndex() {
        return scrollContainer.getScrollX() / this.screenWidth;
    }

    public SpringBoardPage addNewPage() {
        if (getPage(scrollView.getChildCount() - 2, false).getIconsCount() == 0) {
            return null;
        }
        SpringBoardPage page = new SpringBoardPage(MainActivity.this);
        page.init(this.lc, this.pp);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(this.screenWidth, -1);
        scrollView.addView(page, -1 + this.scrollView.getChildCount(), layoutParams);
        dotView.setPages(getPageCount());
        return page;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        closeFolder();
        if (jma.isJiggling()) {
            jma.unjiggle();
        }
        selectedPageIndex = 1;
        scrollToCurrent(selectedPageIndex);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        MainActivity.this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void closeFolder() {
        SpringBoardPage page = (SpringBoardPage) getPage(selectedPageIndex, false);
        Transformation transformation = new Transformation();
        if (mFolderView != null
                && !mFolderView.getAnimation().getTransformation(AnimationUtils.currentAnimationTimeMillis(),
                        transformation)) {
            Point point = page.getIconLocation(openFolderIndex);
            Animation animation = pp.createAnimationOpenFolder(point.x, point.y, screenWidth, screenHeight, false);
            animation.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    boolean isJiggling = mFolderView.isJiggling();
                    mContainer.removeView(mFolderView);
                    mFolderView = null;
                    if (isJiggling) {
                        jma.jiggle();
                    }
                    getPage(selectedPageIndex, false).clearUp(null);
                }
            });
            if (mFolderView != null) {
                mFolderView.startAnimation(animation);
            }
            Animation pageAnimation = pp.createAnimationPageShow(true);
            page.startAnimation(pageAnimation);
            dotView.setVisibility(View.VISIBLE);
        }
    }

    private void openFolder(FolderInfo folderInfo) {
        if (mFolderView == null) {
            SpringBoardPage page = (SpringBoardPage) getPage(selectedPageIndex, false);
            int index;
            if (hitTest2.index != -1) {
                index = hitTest2.index;
            } else {
                index = page.getSelectedIndex();
            }
            Point point = page.getIconLocation(index);
            dotView.setVisibility(View.INVISIBLE);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mFolderView = folderInfo.getFolderView(MainActivity.this);
            if (mFolderView.getParent() == null) {
                mContainer.addView(mFolderView, params);
            }
            mFolderView.onReday(lc, pp, screenHeight);
            Animation folderAnimation = pp.createAnimationOpenFolder(point.x, point.y, screenWidth, screenHeight, true);
            mFolderView.startAnimation(folderAnimation);
            Animation pageAnimation = pp.createAnimationPageShow(false);
            page.startAnimation(pageAnimation);
            page.removeFolderBound();
            page.clearUp(null);
            if (jma.isJiggling()) {
                mFolderView.jiggle();
            } else {
                mFolderView.unJiggle();
            }
            jma.unjiggle();
        }
    }


    private void initDataFromDB(List<ApplicationInfo> list) {
        //        List<ApplicationInfo> child = new ArrayList<ApplicationInfo>();
        //        for (int i = 0; i < 25; i++) {
        //            ApplicationInfo info = new ApplicationInfo();
        //            info.setTitle("应用" + i);
        //            info.setOrder(i);
        //            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
        //            list.add(info);
        //        }
        //        for (int i = 0; i < 13; i++) {
        //            ApplicationInfo info = new ApplicationInfo();
        //            info.setTitle("应用" + i);
        //            info.setId(i + "");
        //            info.setOrder(i);
        //            if (i == 0) {
        //                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a1));
        //            } else if (i == 1) {
        //                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a2));
        //            } else if (i == 2) {
        //                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a3));
        //            } else if (i == 3) {
        //                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a4));
        //            } else if (i == 4) {
        //                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a5));
        //            } else if (i == 5) {
        //                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a6));
        //            } else if (i == 6) {
        //                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a7));
        //            } else if (i == 7) {
        //                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a8));
        //            } else {
        //                info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
        //            }
        //            child.add(info);
        //        }
        //        FolderInfo folder = new FolderInfo();
        //        folder.setIcons(child);
        //        folder.setOrder(25);
        //        folder.setTitle("文件夹");
        //        list.add(folder);
        //        Collections.sort(list, new Comparator<ApplicationInfo>() {
        //            public int compare(ApplicationInfo arg0, ApplicationInfo arg1) {
        //                return arg0.getOrder() - arg1.getOrder();
        //            }
        //        });
//        List<Launcher> launchers = db.queryLaunchers();
//        for (int i = launchers.size() - 1; i >= 0; i--) {
//            Launcher launcher = launchers.get(i);
//            if (Launcher.RES_TYPE_APP.equals(launcher.getResType())) {
//                ApplicationInfo info = new ApplicationInfo();
//                info.setId(launcher.getId());
//                info.setTitle(launcher.getResName());
//                info.setImgUrl(launcher.getPicPath());
//                list.add(info);
//                launchers.remove(i);
//            }
//            if (Launcher.RES_TYPE_FOLDER.equals(launcher.getResType())) {
//                FolderInfo info = new FolderInfo();
//                info.setId(launcher.getId());
//                info.setTitle(launcher.getResName());
//                info.setImgUrl(launcher.getPicPath());
//                list.add(info);
//                launchers.remove(i);
//            }
//            if (launcher.getUpload().equals(Launcher.UPLOAD_FAIL)) {
//                needUpload = true;
//            }
//        }
//        for (int i = 0; i < launchers.size(); i++) {
//            Launcher launcher = launchers.get(i);
//            if (Launcher.RES_TYPE_FOLDER_APP.equals(launcher.getResType())) {
//                ApplicationInfo info = new ApplicationInfo();
//                info.setId(launcher.getId());
//                info.setTitle(launcher.getResName());
//                info.setImgUrl(launcher.getPicPath());
//                for (int j = 0; j < list.size(); j++) {
//                    ApplicationInfo app = list.get(j);
//                    if (ItemInfo.TYPE_FOLDER == app.getType() && app.getId().equals(launcher.getUpId())) {
//                        FolderInfo folder = (FolderInfo) app;
//                        folder.addIcon(info);
//                    }
//                }
//            }
//        }

    }

    private Controller controller = new Controller() {
        @Override
        public void initData(final List<ApplicationInfo> list) {
            AsyncTask<String, String, String> tast = new AsyncTask<String, String, String>() {

                @Override
                protected String doInBackground(String... params) {
                    List<ApplicationInfo> child = new ArrayList<ApplicationInfo>();
                    for (int i = 0; i < 8; i++) {
                        ApplicationInfo info = new ApplicationInfo();
                        info.setId(i + "");
                        info.setTitle("应用" + i);
                        info.setOrder(i);
                        if (i == 0) {
                            info.setImgUrl("http://img3.imgtn.bdimg.com/it/u=568867752,3099839373&fm=21&gp=0.jpg");
                        } else if (i == 1) {
                            info.setImgUrl("http://a2.att.hudong.com/04/58/300001054794129041580438110_950.jpg");
                        } else if (i == 2) {
                            info.setImgUrl("http://img.sc115.com/uploads/sc/jpgs/11/pic1916_sc115.com.jpg");
                        } else if (i == 3) {
                            info.setImgUrl("http://pic12.nipic.com/20110222/6660820_111945190101_2.jpg");
                        } else if (i == 4) {
                            info.setImgUrl("http://pica.nipic.com/2007-12-26/2007122602930235_2.jpg");
                        } else if (i == 5) {
                            info.setImgUrl("http://pic9.nipic.com/20100902/5615113_084913055054_2.jpg");
                        } else if (i == 6) {
                            info.setImgUrl("http://pica.nipic.com/2008-01-03/200813165855102_2.jpg");
                        } else if (i == 7) {
                            info.setImgUrl("http://pica.nipic.com/2008-03-20/2008320152335853_2.jpg");
                        } else {
                            //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
                            info.setImgUrl("http://pic2.ooopic.com/01/26/61/83bOOOPIC72.jpg");
                        }
                        //                        info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
                        list.add(info);
                    }
                    //                    for (int i = 0; i < 13; i++) {
                    //                        ApplicationInfo info = new ApplicationInfo();
                    //                        info.setId(i + "aa");
                    //                        info.setTitle("应用" + i);
                    //                        info.setId(i + "");
                    //                        info.setOrder(i);
                    //                        if (i == 0) {
                    //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a1));
                    //                        } else if (i == 1) {
                    //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a2));
                    //                        } else if (i == 2) {
                    //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a3));
                    //                        } else if (i == 3) {
                    //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a4));
                    //                        } else if (i == 4) {
                    //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a5));
                    //                        } else if (i == 5) {
                    //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a6));
                    //                        } else if (i == 6) {
                    //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a7));
                    //                        } else if (i == 7) {
                    //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.a8));
                    //                        } else {
                    //                            //                            info.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
                    //                            info.setImgUrl("http://img3.imgtn.bdimg.com/it/u=568867752,3099839373&fm=21&gp=0.jpg");
                    //                        }
                    //                        child.add(info);
                    //                    }
                    //                    FolderInfo folder = new FolderInfo();
                    //                    folder.setId(25 + "");
                    //                    folder.setIcons(child);
                    //                    folder.setOrder(25);
                    //                    folder.setTitle("文件夹");
                    //                    list.add(folder);
                    Collections.sort(list, new Comparator<ApplicationInfo>() {
                        public int compare(ApplicationInfo arg0, ApplicationInfo arg1) {
                            return arg0.getOrder() - arg1.getOrder();
                        }
                    });
                    return null;
                }

                protected void onPostExecute(String result) {
                    loaded();
                };
            };
            tast.execute("");
        }

        @Override
        public void onSynchronize() {
            Toast.makeText(MainActivity.this, "正在同步数据", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAppClick(ApplicationInfo app) {
            Toast.makeText(MainActivity.this, "点击了" + app.getTitle(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onAppRemove(ApplicationInfo app) {
            Toast.makeText(MainActivity.this, "删除" + app.getTitle(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RECEIVER_ADD_APP);
            sendBroadcast(intent);
        }

    };
}
