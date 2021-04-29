package com.example.ul.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.CountDownTimerUtil;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.Response;

/**
 * @author luoweili
 */
public class RegisterActivity extends AppCompatActivity implements HttpUtil.MyCallback{
    private final static String TAG = "RegisterActivity";
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 200;
    /**请求失败*/
    private static final int REQUEST_FAIL = 2000;
    /**获取邮箱验证码*/ 
    private static final int GET_CODE_CODE = 201;
    /**提交注册申请*/ 
    private static final int REGISTER_CODE = 202;
    /**在校学生*/
    private final String roleStudent = "student";
    /**校外人员*/
    private final String roleOther = "other";
    /**身份(默认读者)*/
    private String role = roleStudent;
    private View studentView;
    private View otherView;
    /**滚动表单*/
    private ScrollView scrollView;
    /**获取验证码按钮*/
    private Button buttonCode;
    private String myId;
    private String myUsername;
    private String myPassword;
    private String myConfirmPassword;
    private String myEmail;
    private String myVerificationCode;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_register);
        Button buttonBack = findViewById(R.id.registerBack);
        buttonBack.setOnClickListener(view -> {
            RegisterActivity.this.finish();
        });
        Button buttonClear = findViewById(R.id.registerClear);
        buttonClear.setOnClickListener(view -> {
            clear();
        });
        Button buttonSubmit = findViewById(R.id.registerSubmit);
        buttonSubmit.setOnClickListener(view -> {
            submit();
        });
        scrollView =  findViewById(R.id.registerScrollView);
        // 默认加载学生注册时的ScrollView
        LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        studentView = inflater.inflate(R.layout.register_student, null);
        otherView = inflater.inflate(R.layout.register_other, null);
        scrollView.addView(studentView);
        buttonCode = findViewById(R.id.registerGetCode);
        buttonCode.setOnClickListener(view1 -> {
            getCode();
        });
        // 身份单选按钮
        RadioGroup rg = findViewById(R.id.radioGroup2);
        // 给rg绑定监听事件
        rg.setOnCheckedChangeListener((group, checkedId)->{
            role = checkedId == R.id.radioButtonStudent ? roleStudent:roleOther;
            if(role.equals(roleStudent)){
                scrollView.removeAllViews();
                // 加载学生注册时的ScrollView
                scrollView.addView(studentView);
            }else {
                scrollView.removeAllViews();
                // 加载校外人员注册时的ScrollView
                scrollView.addView(otherView);
            }
        });
    }
    /**获取验证码*/
    void getCode(){
        // 检查邮箱是否填写
        View tempView;
        if(role.equals(roleStudent)){
            tempView = studentView;
        }else {
            tempView = otherView;
        }
        EditText editTextEmail = (EditText) tempView.findViewById(R.id.registerEmail);
        myEmail = editTextEmail.getText().toString().trim();
        if("".equals(myEmail)){
            DialogUtil.showDialog(this,"请先填写邮箱",false);
        }else {
            // 禁用按钮一分钟
            CountDownTimerUtil countDownTimerUtil = new CountDownTimerUtil (buttonCode,60000,1000);
            countDownTimerUtil.start();
            // 使用HashMap封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("email", myEmail);
            // 定义发送的请求url
            String url = HttpUtil.BASE_URL + "register/emailcode";
            HttpUtil.postRequest(null,url,hashMap,RegisterActivity.this,GET_CODE_CODE);
        }
    }
    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }
    /**清空界面信息*/
    void clear(){
        EditText editTextId = (EditText) findViewById(R.id.registerId);
        editTextId.setText("");
        EditText editTextUsername = (EditText) findViewById(R.id.registerUsername);
        editTextUsername.setText("");
        EditText editTextPassword = (EditText) findViewById(R.id.registerPassword);
        editTextPassword.setText("");
        EditText editTextConfirmPassword = (EditText) findViewById(R.id.registerConfirmPassword);
        editTextConfirmPassword.setText("");
        EditText editTextEmail = (EditText) findViewById(R.id.registerEmail);
        editTextEmail.setText("");
        EditText editTextCode = (EditText) findViewById(R.id.registerVerificationCode);
        editTextCode.setText("");
    }
    /**提交表单*/
    void submit(){
        View tempView;
        if(role.equals(roleStudent)){
            tempView = studentView;
        }else {
            tempView = otherView;
        }
        EditText editTextId = (EditText) tempView.findViewById(R.id.registerId);
        myId = editTextId.getText().toString().trim();
        EditText editTextUsername = (EditText) tempView.findViewById(R.id.registerUsername);
        myUsername = editTextUsername.getText().toString().trim();
        EditText editTextPassword = (EditText) tempView.findViewById(R.id.registerPassword);
        myPassword = editTextPassword.getText().toString().trim();
        EditText editTextConfirmPassword = (EditText) tempView.findViewById(R.id.registerConfirmPassword);
        myConfirmPassword = editTextConfirmPassword.getText().toString().trim();
        EditText editTextEmail = (EditText) tempView.findViewById(R.id.registerEmail);
        myEmail = editTextEmail.getText().toString().trim();
        EditText editTextCode = (EditText)findViewById(R.id.registerVerificationCode);
        myVerificationCode = editTextCode.getText().toString().trim();
        if(validate()){
            //使用Map封装请求参数
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("id", myId);
            hashMap.put("username", myUsername);
            hashMap.put("password", myPassword);
            hashMap.put("email", myEmail);
            hashMap.put("code", myVerificationCode);
            //定义发送的请求url
            String url = HttpUtil.BASE_URL + "register";
            HttpUtil.postRequest(null,url,hashMap,RegisterActivity.this, REGISTER_CODE);
        }
    }
    /**校验*/
    boolean validate(){
        if("".equals(myId)){
            String msg;
            if(role.equals(roleStudent)){
                msg = "学号不许为空";
            }else {
                msg = "身份证号不许为空";
            }
            DialogUtil.showDialog(this,msg,false);
            return false;
        }
        if("".equals(myUsername)){
            DialogUtil.showDialog(this,"账号不许为空",false);
            return false;
        }
        if("".equals(myPassword)){
            DialogUtil.showDialog(this,"密码不许为空",false);
            return false;
        }
        if("".equals(myConfirmPassword)){
            DialogUtil.showDialog(this,"确认密码行不许为空",false);
            return false;
        }
        if(!myPassword.equals(myConfirmPassword)){
            DialogUtil.showDialog(this,"两次输入密码不一致",false);
            return false;
        }
        if("".equals(myEmail)){
            DialogUtil.showDialog(this,"邮箱不许为空",false);
            return false;
        }
        if("".equals(myVerificationCode)){
            DialogUtil.showDialog(this,"验证码不许为空",false);
            return false;
        }
        return true;
    }

    static class MyHandler extends Handler {
        private final WeakReference<RegisterActivity> registerActivity;

        public MyHandler(WeakReference<RegisterActivity> registerActivity) {
            this.registerActivity = registerActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            RegisterActivity myActivity = registerActivity.get();
            Bundle bundle = msg.getData();
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            } else if(what == GET_CODE_CODE){
                String m = bundle.getString("message");
                DialogUtil.showDialog(myActivity,m,false);
            } else if(what == REGISTER_CODE){
                String message = bundle.getString("message");
                String code = bundle.getString("code");
                String tip = bundle.getString("tip");
                View view = View.inflate(myActivity,R.layout.dialog_view,null);
                TextView tvFrom = view.findViewById(R.id.dialog_from);
                tvFrom.setText(TAG);
                TextView tvCode = view.findViewById(R.id.dialog_code);
                tvCode.setText(code);
                TextView tvMessage = view.findViewById(R.id.dialog_message);
                tvMessage.setText(message);
                TextView tvTip = view.findViewById(R.id.dialog_tip);
                tvTip.setText(tip);
                DialogUtil.showDialog(myActivity,view,false);
            }
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));
    
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
        data.putString("message",m);
        data.putString("code",c);
        data.putString("tip",t);
        msg.setData(data);
        switch (code) {
            // 获取验证码请求
            case GET_CODE_CODE:
                msg.what = GET_CODE_CODE;
                myHandler.sendMessage(msg);
                break;
            // 注册申请
            case REGISTER_CODE:
                msg.what = REGISTER_CODE;
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