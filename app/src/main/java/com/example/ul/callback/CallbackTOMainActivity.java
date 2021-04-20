package com.example.ul.callback;

/**
 * @Author: Wallace
 * @Description: 点击列表项时，主活动执行相应功能
 * @Date: 2021/3/7 10:51
 * @Modified By:
 */
public interface CallbackTOMainActivity {
    /**点击读者列表项时回调的接口*/
    void clickToGetReaderDetail(String id);

    /**点击书本列表项时回调的接口*/
    void clickToGetBookDetail(String id);

    /**点击借阅列表项时回调的接口*/
    void clickToGetBorrowDetail(int i);

    /**点击缴费列表项时回调的接口*/
    void clickToGetApplicationDetail(int i);

}
