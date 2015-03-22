package com.jchun.mylauncher.widget;

public abstract class JiggleModeActivator {
    public static final int STATE_JIGGLE = 2;
    public static final int STATE_JIGGLE_FADE_IN = 1;
    public static final int STATE_JIGGLE_FADE_OUT = 3;
    public static final int STATE_UNJIGGLE = 0;
    public static final int TIME_ACTIVATE = 600;
    private int state;

    public int getState() {
        return this.state;
    }

    public abstract boolean isJigglable();

    public boolean isJiggling() {
        if (this.state == STATE_JIGGLE)
            return true;
        return false;
    }

    public boolean isUnjiggled() {
        if (this.state == STATE_UNJIGGLE)
            return true;
        return false;
    }

    public abstract void jiggle();

    public void setState(int state) {
        this.state = state;
    }

    public abstract void unjiggle();
}