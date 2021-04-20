package com.example.ul.callback;

/**
 * @Author: Wallace
 * @Description: 列表项的监听器
 * @Date: 2021/4/17 11:24
 * @Modified: By yyyy-MM-dd
 */
public interface ImageAdapterItemListener {
    /**
     * 单击展示图片时
     */
    void onClickToShow(int position);

    /**
     * 单击删除图片时
     */
    void onClickToDelete(int position);
}
