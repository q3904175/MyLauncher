package com.jchun.mylauncher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
/**
 * 文件夹标题输入框
 * @author JChun
 *
 */
public class FolderEditText extends EditText {

    private FolderView mFolder;

    public FolderEditText(Context context) {
        super(context);
    }
 
    public FolderEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FolderEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setFolder(FolderView folder) {
        mFolder = folder;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK) {
            mFolder.doneEditingFolderName(true);
        }
        return super.onKeyPreIme(keyCode, event);
    }
}