package com.jchun.mylauncher.widget;

import java.lang.ref.SoftReference;

import android.graphics.Bitmap;

public class ItemInfo {
    public static final int TYPE_APP = 1;
    public static final int TYPE_FOLDER = 2;
    private String id;
    protected Bitmap icon;
    protected String title;
    protected String text;
    protected int order;
    protected int type;
    protected SoftReference<Bitmap> iconRef;
    public Bitmap notification;
    public Bitmap buffer;
    private boolean jiggle = false;
    protected boolean titleMeasured = false;
    private String imgUrl;

    public void clearIcon() {
        this.icon = null;
        this.iconRef = null;
    }

    public void destroy() {
        this.icon = null;
        this.notification = null;
        this.buffer = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
        iconRef = new SoftReference<Bitmap>(icon);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title ;
    }

    public void setTitle(String title) {
        this.title = title;
        this.text = title;
        titleMeasured = false;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public SoftReference<Bitmap> getIconRef() {
        return iconRef;
    }

    public void setIconRef(SoftReference<Bitmap> iconRef) {
        this.iconRef = iconRef;
    }

    public void jiggle() {
        setJiggle(true);
    }

    public void unJiggle() {
        setJiggle(false);
    }

    public boolean isJiggle() {
        return jiggle;
    }

    public void setJiggle(boolean jiggle) {
        this.jiggle = jiggle;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
