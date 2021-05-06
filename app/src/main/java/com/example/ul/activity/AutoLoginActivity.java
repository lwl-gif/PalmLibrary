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
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.HashMap;

import okhttp3.Response;

/**
 * @author luoweili
 */
public class AutoLoginActivity extends Activity implements HttpUtil.MyCallback {
    private static final String TAG = "AutoLoginActivity";
    private ImageView imageView;
    private Button button;
    private final String librarian = "librarian";
    /**设一个定时器，四秒内无其他操作即自动跳转到登录界面*/
    private final AutoLoginActivity.CountDownTimerUtil countDownTimerUtil = this.new CountDownTimerUtil(5000,1000);
    /**未知请求*/
    private static final int UNKNOWN_REQUEST = 10;
    /**请求失败*/
    private static final int REQUEST_FAIL = 100;
    /**发送自动登录请求*/
    private static final int AUTO_LOGIN_CODE = 11;
    /**登录成功*/
    private static final int AUTO_LOGIN_SUCCEED = 111;
    /**登录失败*/
    private static final int AUTO_LOGIN_FAIL = 110;

    private String username;
    private String password;
    private String role;
    @Override
    public void success(Response response, int code) throws IOException {
        // 获取服务器响应字符串
        String result = response.body().string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        // 登录请求
        if (code == AUTO_LOGIN_CODE) {
            if ("登录成功！".equals(jsonObject.getString("message"))) {
                UserInfo userInfo = UserManager.getInstance().getUserInfo(this);
                // 旧的token
                String token = userInfo.getToken();
                // 查看是否有新的token传过来
                JSONObject dataJsonObject = JSON.parseObject(jsonObject.getString("dataObject"));
                String isNewToken = dataJsonObject.getString("isNewToken");
                // 新的token
                if ("true".equals(isNewToken)) {
                    // 获取新的token
                    token = dataJsonObject.getString("token");
                }
                UserManager.getInstance().saveUserInfo(this, username, password, role, token);
                // 用handle发送消息，通知主线程可以登录
                myHandler.sendEmptyMessage(AUTO_LOGIN_SUCCEED);
            } else {
                //登录失败
                myHandler.sendEmptyMessage(AUTO_LOGIN_FAIL);
            }
        } else {
            myHandler.sendEmptyMessage(UNKNOWN_REQUEST);
        }
    }

    @Override
    public void failed(IOException e, int code) {
        myHandler.sendEmptyMessage(REQUEST_FAIL);
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
            myActivity.countDownTimerUtil.cancel();
            Intent intent;
            switch (msg.what) {
                case AUTO_LOGIN_SUCCEED:
                    if(myActivity.role.equals(myActivity.librarian)){
                        intent  = new Intent(myActivity, LMainActivity.class);
                    }else {
                        intent  = new Intent(myActivity, RMainActivity.class);
                    }
                    myActivity.startActivity(intent);
                    myActivity.finish();
                    break;
                case AUTO_LOGIN_FAIL:
                    Toast.makeText(myActivity,"自动登录失败！",Toast.LENGTH_SHORT).show();
                    intent = new Intent(myActivity, LoginActivity.class);
                    myActivity.startActivity(intent);
                    myActivity.finish();
                    break;
                default:
                    intent = new Intent(myActivity, LoginActivity.class);
                    myActivity.startActivity(intent);
                    myActivity.finish();
            }
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_auto_login);
        // 定时器启动
        countDownTimerUtil.start();
        imageView = findViewById(R.id.image_starting);
        button = findViewById(R.id.btn_clock);
        button.setOnClickListener(v -> {
            AutoLoginActivity.this.myHandler.sendEmptyMessage(REQUEST_FAIL);
//            if (UserManager.getInstance().hasUserInfo(this)) {
//                UserInfo userInfo = UserManager.getInstance().getUserInfo(this);
//                username = userInfo.getUserName();
//                password = userInfo.getPassword();
//                role = userInfo.getRole();
//                // 使用HashMap封装请求参数
//                HashMap<String, String> hashMap = new HashMap<>();
//                hashMap.put("username",username);
//                hashMap.put("password",password);
//                hashMap.put("role",role);
//                // 定义发送的请求url
//                String url = HttpUtil.BASE_URL + "autoLogin";
//                HttpUtil.postRequest(null,url,hashMap,this, AUTO_LOGIN_CODE);
//            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RequestOptions requestOptions = new RequestOptions()
                //.placeholder(R.drawable.placeholder0)
                .centerCrop()
                .error(R.mipmap.error0);
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
        if (UserManager.getInstance().hasUserInfo(this)) {
            UserInfo userInfo = UserManager.getInstance().getUserInfo(this);
            username = userInfo.getUserName();
            password = userInfo.getPassword();
            role = userInfo.getRole();
            // 使用HashMap封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("username",username);
            hashMap.put("password",password);
            hashMap.put("role",role);
            // 定义发送的请求url
            String url = HttpUtil.BASE_URL + "autoLogin";
            HttpUtil.postRequest(null,url,hashMap,this, AUTO_LOGIN_CODE);
        }
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
            AutoLoginActivity.this.myHandler.sendEmptyMessage(REQUEST_FAIL);
        }
    }
}

