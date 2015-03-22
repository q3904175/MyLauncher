package com.jchun.mylauncher.widget;

import java.util.List;

import android.graphics.Point;

public interface IPageView {
    public Point getIconLocation(int index);

    public int getSelectedIndex();

    public void select(int index);

    public void hitTest3(int x, int y, HitTestResult3 hitTest3);

    public int hitTest2(int x, int y, HitTestResult3 hitTest2, boolean isFolder);

    public ApplicationInfo getIcon(int index);

    public ApplicationInfo getSelectedApp();

    public void deselect();

    public void jiggle();

    public void unJiggle();

    public int getIconsCount();

    public boolean setMoveTo(int index);

    public void clearUp(ApplicationInfo info);

    public void removeFolderBound();

    public void setIconIntoPage(int index, ApplicationInfo info);

    public void createFolderBound(int index);

    public void addToFolder(int i, ApplicationInfo app);

    public boolean isMessed();

    public void removeApp(int index);

    public void setMessed(boolean isMessed);

}
