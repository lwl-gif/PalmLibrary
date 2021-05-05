package com.example.ul.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ul.R;
import com.example.ul.librarian.LMainActivity;

import com.example.ul.reader.main.RMainActivity;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;

import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Response;

/**
 * @author luoweili
 */
public class AutoLoginActivity extends Activity implements HttpUtil.MyCallback {

    private ImageView imageView;
    private Button button;
    private final String librarian = "librarian";

    private static final String TAG = "AutoLoginActivity";
    /**请求失败*/
    private static final int REQUEST_FAIL = 000;
    /**发送自动登录请求*/
    private static final int LOGIN_CODE = 01;
    /**登录成功*/
    private static final int LOGIN_SUCCEED = 011;
    /**登录失败*/
    private static final int LOGIN_FAIL = 010;


    /**去管理员主页*/
    private static final int GO_LIBRARIAN = 1;
    /**去读者主页*/
    private static final int GO_READER = 2;
    /**去登录页*/
    private static final int GO_LOGIN = 0;

    @Override
    public void success(Response response, int code) throws IOException {

    }

    @Override
    public void failed(IOException e, int code) {

    }

    /**
     * 跳转判断
     */
    static class MyHandler extends Handler {
        private final WeakReference<AutoLoginActivity> autoLoginActivity;

        public MyHandler(WeakReference<AutoLoginActivity> autoLoginActivity) {
            this.autoLoginActivity = autoLoginActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            AutoLoginActivity myActivity = autoLoginActivity.get();
            switch (msg.what) {
                case GO_LIBRARIAN:
                    Intent intent1 = new Intent(myActivity, LMainActivity.class);
                    myActivity.startActivity(intent1);
                    myActivity.finish();
                    break;
                case GO_READER:
                    Intent intent2 = new Intent(myActivity, RMainActivity.class);
                    myActivity.startActivity(intent2);
                    myActivity.finish();
                    break;
                case GO_LOGIN://去登录页
                    Intent intent0 = new Intent(myActivity, LoginActivity.class);
                    myActivity.startActivity(intent0);
                    myActivity.finish();
                    break;
                default:
            }
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_auto_login);
        // 设一个定时器，三秒内无其他操作即自动跳转到登录界面
        final AutoLoginActivity.CountDownTimerUtil countDownTimerUtil = this.new CountDownTimerUtil(4000,1000);
        countDownTimerUtil.start();
        imageView = findViewById(R.id.image_starting);
        button = findViewById(R.id.btn_clock);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(AutoLoginActivity.this, LoginActivity.class);
            AutoLoginActivity.this.startActivity(intent);
            AutoLoginActivity.this.finish();
            countDownTimerUtil.cancel();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder0).centerCrop().error(R.mipmap.error0);
        Resources resources = this.getResources();
        // gif图片文件路径
        String path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(R.drawable.starting) + "/"
                + resources.getResourceTypeName(R.drawable.starting) + "/"
                + resources.getResourceEntryName(R.drawable.starting);
        Glide.with(this)
                .applyDefaultRequestOptions(requestOptions)
                .asGif()
                .load(path)
                .into(imageView);
        // 自动登录，SharedPreferences中有账号密码，则发送登录请求，没数据则跳转到登录页
//        if (UserManager.getInstance().hasUserInfo(this)) {
//            UserInfo userInfo = UserManager.getInstance().getUserInfo(this);
//            // 根据不同身份跳转到不同的主页面
//            String role = userInfo.getRole();
//            if(librarian.equals(role)){
//                myHandler.sendEmptyMessageDelayed(GO_LIBRARIAN, 2000);
//            }else {
//                myHandler.sendEmptyMessageDelayed(GO_READER, 2000);
//            }
//        } else {
//            myHandler.sendEmptyMessageAtTime(GO_LOGIN, 2000);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivity(this);
    }

    class CountDownTimerUtil extends CountDownTimer {

        public CountDownTimerUtil(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        /**计时过程*/
        @Override
        public void onTick(long l) {
            String content = l/1000 + "秒跳转";
            AutoLoginActivity.this.button.setText(content);
        }

        /**计时完毕的方法*/
        @Override
        public void onFinish() {
            Intent intent = new Intent(AutoLoginActivity.this, LoginActivity.class);
            AutoLoginActivity.this.startActivity(intent);
            AutoLoginActivity.this.finish();
        }
    }
}

