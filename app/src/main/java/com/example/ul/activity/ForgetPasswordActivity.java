package com.example.ul.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;

import com.example.ul.util.CountDownTimerUtil;
import com.example.ul.util.UserManager;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.Response;

public class ForgetPasswordActivity extends AppCompatActivity implements HttpUtil.MyCallback{
    private final static String TAG = "ForgetPasswordActivity";
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 300;
    /**请求失败*/
    private static final int REQUEST_FAIL = 3000;
    /**获取邮箱验证码*/
    private static final int GET_CODE_CODE = 301;
    /**验证邮箱验证码*/
    private static final int VERIFY_CODE_CODE = 302;
    /**修改密码*/
    private static final int UPDATE_PASSWORD_CODE = 303;

    private EditText forgetEmailAddress;
    private Button forgetGetCode;
    private EditText forgetVerificationCode;

    private EditText forgetNextPassword;
    private EditText forgetNextConfirmPassword;
    /**界面保存的邮箱*/
    private String email;
    /**界面保存的密码*/
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        changeTo("forget");
    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    void clear(){
        forgetEmailAddress.setText(null);
        forgetVerificationCode.setText(null);
    }

    /**获取验证码*/
    void getCode(){
        // 检查邮箱是否填写
        email = forgetEmailAddress.getText().toString().trim();
        if(email ==null|| "".equals(email)){
            DialogUtil.showDialog(this,"请先填写邮箱",false);
        }else {
            //禁用按钮一分钟
            CountDownTimerUtil countDownTimerUtil = new CountDownTimerUtil (forgetGetCode,60000,1000);
            countDownTimerUtil.start();
            // 使用Map封装请求参数
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("email", email);
            // 定义发送的请求url
            String url = HttpUtil.BASE_URL + "forget/emailcode";
            HttpUtil.postRequest(null,url,hashMap,ForgetPasswordActivity.this,GET_CODE_CODE);
        }
    }
    /**
     * @Author: Wallace
     * @Description: “下一步”按钮的事件
     * @Date: Created in 19:30 2021/3/3
     * @Modified By:
     * @return: void
     */
    void submit(){
        // 检查邮箱是否填写
        if(email ==null|| "".equals(email)){
            DialogUtil.showDialog(this,"请先填写邮箱",false);
        }else {
            // 检查验证码是否填写
            String code = forgetVerificationCode.getText().toString().trim();
            if(code!=null&&!"".equals(code)){
                //发送邮箱和验证码，待服务器验证
                //使用Map封装请求参数
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("email", email);
                hashMap.put("code",code);
                // 定义发送的请求url
                String url = HttpUtil.BASE_URL + "forget/verify";
                HttpUtil.postRequest(null,url,hashMap,this,VERIFY_CODE_CODE);
            }else {
                DialogUtil.showDialog(this,"验证码不能为空",false);
            }
        }
    }

    void changeTo(String s){
        if("forgetNext".equals(s)){
            setContentView(R.layout.activity_forget_password_next);
            forgetNextPassword = findViewById(R.id.forgetNextPassword);
            forgetNextConfirmPassword = findViewById(R.id.forgetNextConfirmPassword);
            Button forgetNextBack = findViewById(R.id.forgetNextBack);
            forgetNextBack.setOnClickListener(view -> {
                changeTo("forget");
            });
            Button forgetNextClear = findViewById(R.id.forgetNextClear);
            forgetNextClear.setOnClickListener(view -> {
                forgetNextPassword.setText("");
                forgetNextConfirmPassword.setText("");
            });
            Button forgetNextSubmit = findViewById(R.id.forgetNextSubmit);
            forgetNextSubmit.setOnClickListener(view -> {
                //检验两次输入的密码是否为空且相同
                String password = forgetNextPassword.getText().toString().trim();
                if(!"".equals(password)){
                    String confirmPassword = forgetNextConfirmPassword.getText().toString().trim();
                    if(!"".equals(confirmPassword)){
                        if(password.equals(confirmPassword)){
                            // 发送修改密码请求
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("newPassword",password);
                            this.password = password;
                            // 定义发送的请求url
                            String url = HttpUtil.BASE_URL + "forget";
                            HttpUtil.postRequest(null,url,hashMap,this,UPDATE_PASSWORD_CODE);
                        } else {
                            DialogUtil.showDialog(this,"两次输入的密码不一致",false);
                        }
                    }else {
                        DialogUtil.showDialog(this,"确认密码不能为空",false);
                    }
                }else {
                    DialogUtil.showDialog(this,"新密码不能为空",false);
                }
            });
        }else {
            setContentView(R.layout.activity_forget_password);
            forgetEmailAddress = findViewById(R.id.forgetEmailAddress);
            forgetGetCode = findViewById(R.id.forgetGetCode);
            forgetGetCode.setOnClickListener(view -> {
                getCode();
            });
            forgetVerificationCode = findViewById(R.id.forgetVerificationCode);
            Button forgetBack = findViewById(R.id.forgetBack);
            forgetBack.setOnClickListener(view -> {
                ForgetPasswordActivity.this.finish();
            });
            Button forgetClear = findViewById(R.id.forgetClear);
            forgetClear.setOnClickListener(view -> {
                clear();
            });
            Button forgetSubmit = findViewById(R.id.forgetSubmit);
            forgetSubmit.setOnClickListener(view -> {
                submit();
            });
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    static class MyHandler extends Handler {
        private final WeakReference<ForgetPasswordActivity> forgetPasswordActivity;

        public MyHandler(WeakReference<ForgetPasswordActivity> forgetPasswordActivity) {
            this.forgetPasswordActivity = forgetPasswordActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            ForgetPasswordActivity myActivity = forgetPasswordActivity.get();
            Bundle bundle = msg.getData();
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            } else if(what == GET_CODE_CODE){
                String m = bundle.getString("message");
                DialogUtil.showDialog(myActivity,m,false);
            } else {
                DialogUtil.showDialog(myActivity,TAG,bundle,false);
            }
        }
    }

    @Override
    public void success(Response response, int code) throws IOException {
        // 获取服务器响应字符串
        String result = Objects.requireNonNull(response.body()).string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        String m = jsonObject.getString("message");
        String t = jsonObject.getString("tip");
        String c = jsonObject.getString("code");
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("message", m);
        data.putString("code", c);
        data.putString("tip", t);
        msg.setData(data);
        switch (code) {
            case GET_CODE_CODE:
                msg.what = GET_CODE_CODE;
                myHandler.sendMessage(msg);
                break;
            // 验证验证码请求
            case VERIFY_CODE_CODE:
                // 如果服务器验证成功，开始下一步
                if ("验证成功!".equals(m)) {
                    // 加载填新密码的页面
                    this.changeTo("forgetNext");
                } else {
                    msg.what = VERIFY_CODE_CODE;
                }
                myHandler.sendMessage(msg);
                break;
            // 更新密码请求
            case UPDATE_PASSWORD_CODE:
                // 如果修改成功
                if ("密码修改成功!".equals(m)) {
                    // 更改本机缓存中的用户密码
                    UserInfo userInfo = UserManager.getInstance().getUserInfo(ForgetPasswordActivity.this);
                    String username = userInfo.getUserName();
                    String role = userInfo.getRole();
                    String token = userInfo.getToken();
                    UserManager.getInstance().saveUserInfo(ForgetPasswordActivity.this, username, password, role, token);
                }
                msg.what = UPDATE_PASSWORD_CODE;
                myHandler.sendMessage(msg);
                break;
            default:
                data.putString("reason", "未知错误");
                msg.what = UNKNOWN_REQUEST_ERROR;
                myHandler.sendMessage(msg);
        }
    }
    @Override
    public void failed(IOException e, int code) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        String reason;
        if (e instanceof SocketTimeoutException) {
            reason = "连接超时";
            message.what = REQUEST_FAIL;
        } else if (e instanceof ConnectException) {
            reason = "连接服务器失败";
            message.what = REQUEST_FAIL;
        } else if (e instanceof UnknownHostException) {
            reason = "网络异常";
            message.what = REQUEST_FAIL;
        } else {
            reason = "未知错误";
            message.what = UNKNOWN_REQUEST_ERROR;
        }
        bundle.putString("reason", reason);
        message.setData(bundle);
        myHandler.sendMessage(message);
    }
}