package com.jchun.mylauncher.activity;

import net.tsz.afinal.FinalBitmap;
import android.app.Application;

public class MyApplication extends Application{
    private static MyApplication instacne = null;
    public FinalBitmap fb;
    @Override
    public void onCreate() {
        super.onCreate();
        fb = FinalBitmap.create(getApplicationContext());
        instacne = this;
    }

    public static MyApplication getInstance() {
        return instacne;
    }
}
