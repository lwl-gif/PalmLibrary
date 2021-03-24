package com.example.ul.callback;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/22 8:52
 * @Modified By:
 */
public interface CallbackToRBorrowFragment {
    //点击借阅列表项“查看详情”按钮
    void borrowListToWantMore(int i);
    //点击借阅列表项“扫码转借”按钮
    void borrowListToLent(int i);
    //点击借阅列表项“一键续借”按钮
    void borrowListToRenew(int i);
    //点击借阅列表项“一键挂失”按钮
    void borrowListToLoss(int i);
    //点击借阅列表项“放弃预约”按钮
    void borrowListToAbandon(int i);
}
