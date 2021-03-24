package com.example.ul.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import com.example.ul.R;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import okhttp3.Response;
/*
 * 登录界面
 * */
public class LoginActivity extends AppCompatActivity implements HttpUtil.MyCallback{
    //自定义登录请求的消息代码
    private static final int LOGIN_CODE = 0101;

    //账号
    private EditText username;
    //密码
    private EditText password;
    //登陆
    private Button btn_login;
    //取消
    private Button btn_cancel;
    //注册
    private Button btn_register;
    //清空
    private Button btn_clear;
    //忘记密码
    private Button btn_forget;
    //身份单选按钮
    private RadioGroup rg;
    //身份(默认读者)
    private String role = "reader";

    static class MyHandler extends Handler {
        private WeakReference<LoginActivity> loginActivity;
        public MyHandler(WeakReference<LoginActivity> loginActivity){
            this.loginActivity = loginActivity;
        }
        public void handleMessage(Message msg){
            int what = msg.what;
            switch (what){
                //登录成功
                case 01011:
                    //根据身份启动主界面
                    if(loginActivity.get().role.equals("librarian")){
                        Intent intent = new Intent(loginActivity.get(), LMainActivity.class);
                        loginActivity.get().startActivity(intent);
                    }else {
                        Intent intent = new Intent(loginActivity.get(), RMainActivity.class);
                        loginActivity.get().startActivity(intent);
                    }
                    //结束该Activity
                    loginActivity.get().finish();
                    break;
                //登录失败
                case 01010:
                    loginActivity.get().password.setText(null);
                    break;
                default:
            }
        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.editUsername);
        password = findViewById(R.id.editPassword);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(view -> {
//            //根据身份启动主界面
//            if(role.equals("librarian")){
//                Intent intent = new Intent(com.example.myapplication1.activity.LoginActivity.this, LMainActivityItem.class);
//                startActivity(intent);
//            }else {
//                Intent intent = new Intent(com.example.myapplication1.activity.LoginActivity.this, RMainActivityItem.class);
//                startActivity(intent);
//            }
            if(validate()){
                login();
            }
        });
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(view -> {
            backHome();
        });

        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(view -> {
            register();
        });
        btn_clear = findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(view -> {
            reset();
        });

        btn_forget = findViewById(R.id.btn_forget);
        btn_forget.setOnClickListener(view -> {
            forget();
        });
        rg = findViewById(R.id.radioGroup1);
        //给rg绑定监听事件
        rg.setOnCheckedChangeListener((group,checkedId)->{
            //根据用户选择的单选钮来动态改变role的值,同时改变username的hint属性值
            role = checkedId == R.id.radioButtonR ? "reader":"librarian";
            String str = checkedId == R.id.radioButtonR ? "学号/用户名":"工号/用户名";
            username.setHint(str);
        });
        ActivityManager.getInstance().addActivity(this);
    }

    //登陆
    void login(){
        String username1 = username.getText().toString().trim();
        String password1 = password.getText().toString().trim();
        //使用HashMap封装请求参数
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("username",username1);
        hashMap.put("password",password1);
        hashMap.put("role",role);
        //定义发送的请求url
        String url = HttpUtil.BASE_URL + "login/";
        HttpUtil.postRequest(null,url,hashMap,LoginActivity.this, LOGIN_CODE);
    }
    //重置
    void reset(){
        username.setText(null);
        password.setText(null);
        rg.check(R.id.radioButtonR);
    }
    //注册
    void register(){
        //启动注册活动
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    //忘记密码
    void forget(){
        //启动忘记密码活动
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
    }
    //退出应用
    void backHome(){
        finish();
    }
    //校验
    boolean validate(){
        String username1 = username.getText().toString().trim();
        if(username1.equals("")){
            DialogUtil.showDialog(this,"账号不许为空",false);
            return false;
        }
        String password1 = password.getText().toString().trim();
        if(password1.equals("")){
            DialogUtil.showDialog(this,"密码不许为空",false);
            return false;
        }
        return true;
    }

    @Override
    public void success(Response response, int code) throws IOException {
        //服务器返回的数据
        JSONObject jsonObject;
        String result = null;
        //获取服务器响应字符串
        result = response.body().string().trim();
        switch (code){
            //登录请求
            case LOGIN_CODE:
                try {
                    jsonObject = new JSONObject(result);
                    if(jsonObject.getString("message").equals("登录成功！")){
                        UserInfo userInfo = UserManager.getInstance().getUserInfo(LoginActivity.this);
                        //旧的token
                        String token = userInfo.getToken();
                        //查看是否有新的token传过来
                        String isNewToken = new JSONObject(jsonObject.getString("dataObject")).getString("isNewToken");
                        //新的token
                        if(isNewToken.equals("true")){
                            //获取新的token
                            token = new JSONObject(jsonObject.getString("dataObject")).getString("token");
                        }
                        UserManager.getInstance().saveUserInfo(LoginActivity.this,username.getText().toString().trim(),password.getText().toString().trim(),role,token);
                        //用handle发送消息，通知主线程可以登录
                        myHandler.sendEmptyMessage(01011);
                    }else {         //登录失败
                        String msg0 = jsonObject.getString("message");
                        String msg1 = jsonObject.getString("tip");
                        DialogUtil.showDialog(this,msg0+msg1,false);
                        //用handle发送消息，通知主线程登录失败，进行后续操作
                        myHandler.sendEmptyMessage(01010);
                    }
                } catch (Exception e) {
                    DialogUtil.showDialog(this,"数据解析异常！",false);
                    e.printStackTrace();
                }
                break;
            default:
                DialogUtil.showDialog(this,"未知请求！无法处理",false);
        }
    }

    @Override
    public void failed(IOException e, int code) {
        switch (code){
            case LOGIN_CODE:
                DialogUtil.showDialog(this,"服务器响应异常，请稍后重试！",false);
                e.printStackTrace();
                break;

            default:

        }
    }
}