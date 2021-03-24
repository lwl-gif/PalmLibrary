package com.example.ul.util;

import android.os.CountDownTimer;
import android.widget.Button;

/**
 * @Author:Wallace
 * @Description:倒计时按钮工具类
 * @Date:2021/3/3 22:55
 * @Modified By:
 */
//倒计时函数
public class CountDownTimerUtil extends CountDownTimer {
    private Button timeButton;

    public CountDownTimerUtil(Button button,long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.timeButton = button;
    }

    //计时过程
    @Override
    public void onTick(long l) {
        //防止计时过程中重复点击
        timeButton.setClickable(false);
        timeButton.setText(l/1000+"秒后重新发送");
    }

    //计时完毕的方法
    @Override
    public void onFinish() {
        //重新给Button设置文字
        timeButton.setText("重新获取验证码");
        //设置可点击
        timeButton.setClickable(true);
    }
}

