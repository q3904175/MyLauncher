package com.jchun.mylauncher.widget;

public class HitTestResult3 {
    public boolean buttonRemove; //是否点击在删除按钮上
    public int index = -1; //应用位置
    public boolean inIcon; //是否点击在图标上

    public HitTestResult3() {
    }

    public HitTestResult3(int index, boolean buttonRemove) {
        this.index = index;
        this.buttonRemove = buttonRemove;
    }
}