package com.example.ul.callback;

/**
 * @Author: Wallace
 * @Description: 文件描述
 * @Date: 2021/4/26 23:02
 * @Modified: By yyyy-MM-dd
 */
public interface CallbackToBorrowBookActivity {
    /**
     * 单击展示书本详情时
     */
    void onClickToShowDetail(int position);
    /**
     * 单击删除书本时
     */
    void onClickToDeleteBook(int position);
}
