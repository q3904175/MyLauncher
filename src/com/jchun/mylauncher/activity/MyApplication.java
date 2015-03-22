package com.jchun.mylauncher.activity;

import net.tsz.afinal.FinalBitmap;
import android.app.Application;

public class MyApplication extends Application{
    private static MyApplication instacne = null;
    public FinalBitmap fb;
    @Override
    public void onCreate() {
        fb = FinalBitmap.create(this);
        instacne = this;
        super.onCreate();
    }

    public static MyApplication getInstance() {
        return instacne;
    }
}
