package com.example.ul.callback;

import java.util.HashMap;

/**
 * @Author: Wallace
 * @Description: 选择对话框的回调
 * @Date: 2021/5/13 23:10
 * @Modified: By yyyy-MM-dd
 */
public interface DialogActionCallback {

    /**
     * @Author: Wallace
     * @Description: 选择确定按钮后的回调
     * @Date: Created 23:17 2021/5/13
     * @Modified: by who yyyy-MM-dd
     * @param requestParam 请求参数
     */
    void positiveAction(HashMap<String, Object> requestParam);
    /**
     * @Author: Wallace
     * @Description: 选择取消按钮后的回调
     * @Date: Created 23:17 2021/5/13
     * @Modified: by who yyyy-MM-dd
     * @param requestParam 请求参数
     */
    void negativeAction(HashMap<String, Object> requestParam);
}
