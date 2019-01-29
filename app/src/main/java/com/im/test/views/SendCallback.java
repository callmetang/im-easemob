package com.im.test.views;

import android.widget.EditText;

/**
 * @author tangdehao
 * @date 2019/1/28
 */

public interface SendCallback {
    /**
     * 点击发送监听
     *
     * @param editText
     */
    void send(EditText editText);
}
