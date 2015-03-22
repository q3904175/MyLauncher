package com.jchun.mylauncher.widget;

import java.util.List;

public interface Controller {
    /**
     * 初始化桌面数据
     */
    public void initData(List<ApplicationInfo> list);

    /**
     * 点击应用
     */
    public void onAppClick(ApplicationInfo app);

    /**
     * 删除应用
     */
    public void onAppRemove(ApplicationInfo app);

    /**
     * 与服务器同步
     */
    public void onSynchronize();

}
