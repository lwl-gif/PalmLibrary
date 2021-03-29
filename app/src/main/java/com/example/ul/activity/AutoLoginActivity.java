package com.example.ul.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ul.R;
import com.example.ul.activity.librarian.LMainActivity;
import com.example.ul.activity.reader.main.RMainActivity;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.UserManager;

public class AutoLoginActivity extends AppCompatActivity {

    private static final int GO_Librarian = 1;//去管理员主页
    private static final int GO_Reader = 2;//去读者主页
    private static final int GO_LOGIN = 0;//去登录页

    /**
     * 跳转判断
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_Librarian:
                    Intent intent1 = new Intent(AutoLoginActivity.this, LMainActivity.class);
                    startActivity(intent1);
                    finish();
                    break;
                case GO_Reader:
                    Intent intent2 = new Intent(AutoLoginActivity.this, RMainActivity.class);
                    startActivity(intent2);
                    finish();
                    break;
                case GO_LOGIN://去登录页
                    Intent intent0 = new Intent(AutoLoginActivity.this, LoginActivity.class);
                    startActivity(intent0);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_login);
        if (UserManager.getInstance().hasUserInfo(this))//自动登录判断，SharedPreferences中有账号密码，则跳转到主页，没数据则跳转到登录页
        {
            UserInfo userInfo = UserManager.getInstance().getUserInfo(this);
            //根据不同身份跳转到不同的主页面
            String role = userInfo.getRole();
            if(role.equals("librarian")){
                mHandler.sendEmptyMessageDelayed(GO_Librarian, 2000);
            }else {
                mHandler.sendEmptyMessageDelayed(GO_Reader, 2000);
            }
        } else {
            mHandler.sendEmptyMessageAtTime(GO_LOGIN, 2000);
        }
        ActivityManager.getInstance().addActivity(this);
    }
}

