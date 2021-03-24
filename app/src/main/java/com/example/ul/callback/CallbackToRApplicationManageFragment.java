package com.example.ul.callback;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/22 8:58
 * @Modified By:
 */
public interface CallbackToRApplicationManageFragment {
    //点击缴费列表项“点击支付”按钮
    void applicationListToPay(int i);
    //点击缴费列表项“更多详情”按钮
    void applicationToWantMore(int i);
}
