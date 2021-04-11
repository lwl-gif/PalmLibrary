package com.example.ul.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.ul.R;
import com.example.ul.activity.librarian.LMainActivity;
import com.example.ul.activity.reader.main.RMainActivity;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import okhttp3.Response;
/*
 * 登录界面
 * */
public class LoginActivity extends AppCompatActivity implements HttpUtil.MyCallback{

    private static final String TAG = "LoginActivity";

    //自定义代码
    //请求失败
    private static final int REQUEST_FAIL = 100;
    //登录请求的消息代码
    private static final int LOGIN_CODE = 11;
    //登录成功
    private static final int LOGIN_SUCCEED = 111;
    //登录失败
    private static final int LOGIN_FAIL = 110;
    //服务器返回的数据
    private JSONObject jsonObject;
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
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            switch (what){
                //登录成功
                case LOGIN_SUCCEED:
                    //根据身份启动主界面
                    loginActivity.get().password.setText(null);
                    if("librarian".equals(loginActivity.get().role)){
                        Intent intent = new Intent(loginActivity.get(), LMainActivity.class);
                        loginActivity.get().startActivity(intent);
                    }else {
                        Intent intent = new Intent(loginActivity.get(), RMainActivity.class);
                        loginActivity.get().startActivity(intent);
                    }
                    break;
                //登录失败
                case LOGIN_FAIL:
                    try {
                        String message = loginActivity.get().jsonObject.getString("message");
                        String code = loginActivity.get().jsonObject.getString("code");
                        String tip = loginActivity.get().jsonObject.getString("tip");
                        View view = View.inflate(loginActivity.get(),R.layout.dialog_view,null);
                        TextView tvFrom = view.findViewById(R.id.dialog_from);
                        tvFrom.setText(TAG);
                        TextView tvCode = view.findViewById(R.id.dialog_code);
                        tvCode.setText(code);
                        TextView tvMessage = view.findViewById(R.id.dialog_message);
                        tvMessage.setText(message);
                        TextView tvTip = view.findViewById(R.id.dialog_tip);
                        tvTip.setText(tip);
                        DialogUtil.showDialog(loginActivity.get(),view,false);
                    } catch (JSONException e) {
                        Toast.makeText(loginActivity.get(),"主线程解析数据时异常！",Toast.LENGTH_LONG);
                    }
                    break;
                case REQUEST_FAIL:
                    Toast.makeText(loginActivity.get(),"网络异常！",Toast.LENGTH_SHORT).show();
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

    void reset(){
        username.setText(null);
        password.setText(null);
        rg.check(R.id.radioButtonR);
    }

    void register(){
        //启动注册活动
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    void forget(){
        //启动忘记密码活动
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
    }

    void backHome(){
        finish();
    }

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
                        myHandler.sendEmptyMessage(LOGIN_SUCCEED);
                    }else {         //登录失败
                        //用handle发送消息，通知主线程登录失败，进行后续操作
                        myHandler.sendEmptyMessage(LOGIN_FAIL);
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
        myHandler.sendEmptyMessage(REQUEST_FAIL);
    }
}