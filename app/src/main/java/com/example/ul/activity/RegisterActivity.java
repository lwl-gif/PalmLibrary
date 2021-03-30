package com.example.ul.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ul.R;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.CountDownTimerUtil;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity implements HttpUtil.MyCallback{
    //自定义消息代码
    private static final int GET_CODE_CODE = 201; //获取邮箱验证码
    private static final int REGISTER_CODE = 202; //提交注册申请

    //身份单选按钮
    private RadioGroup rg;
    //身份(默认读者)
    private String role = "student";
    private View studentView;
    private View otherView;
    //滚动表单
    private ScrollView scrollView;
    //获取验证码按钮
    private Button ButtonCode;
    //返回按钮
    private Button ButtonBack;
    //清空按钮
    private Button ButtonClear;
    //提交按钮
    private Button ButtonSubmit;
    private String Id;
    private String Username;
    private String Password;
    private String ConfirmPassword;
    private String Email;
    private String VerificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_register);
        ButtonBack = findViewById(R.id.registerBack);
        ButtonBack.setOnClickListener(view -> {
            back();
        });
        ButtonClear = findViewById(R.id.registerClear);
        ButtonClear.setOnClickListener(view -> {
            clear();
        });
        ButtonSubmit = findViewById(R.id.registerSubmit);
        ButtonSubmit.setOnClickListener(view -> {
            submit();
        });
        scrollView =  findViewById(R.id.registerScrollView);
        //默认加载学生注册时的ScrollView
        LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        studentView = inflater.inflate(R.layout.register_student, null);
        otherView = inflater.inflate(R.layout.register_other, null);
        scrollView.addView(studentView);
        ButtonCode = findViewById(R.id.registerGetCode);
        ButtonCode.setOnClickListener(view1 -> {
            getCode();
        });
        rg =  findViewById(R.id.radioGroup2);
        //给rg绑定监听事件
        rg.setOnCheckedChangeListener((group,checkedId)->{
            role = checkedId == R.id.radioButtonStudent ? "student":"other";
            if(role.equals("student")){
                scrollView.removeAllViews();
                //加载学生注册时的ScrollView
                scrollView.addView(studentView);
            }else {
                scrollView.removeAllViews();
                //加载校外人员注册时的ScrollView
                scrollView.addView(otherView);
            }
        });
    }
    //获取验证码
    void getCode(){
        //检查邮箱是否填写
        View tempView;
        if(role.equals("student")){
            tempView = studentView;
        }else {
            tempView = otherView;
        }
        EditText editTextEmail = (EditText) tempView.findViewById(R.id.registerEmail);
        Email = editTextEmail.getText().toString().trim();
        if(Email.equals("")){
            DialogUtil.showDialog(this,"请先填写邮箱",false);
        }else {
            //禁用按钮一分钟
            CountDownTimerUtil countDownTimerUtil = new CountDownTimerUtil (ButtonCode,60000,1000);
            countDownTimerUtil.start();
            //使用HashMap封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("email",Email);
            //定义发送的请求url
            String url = HttpUtil.BASE_URL + "register/emailcode";
            HttpUtil.postRequest(null,url,hashMap,RegisterActivity.this,GET_CODE_CODE);
        }
    }
    //返回
    void back(){
        finish();
    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    //清空界面信息
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
    //提交表单
    void submit(){
        View tempView;
        if(role.equals("student")){
            tempView = studentView;
        }else {
            tempView = otherView;
        }
        EditText editTextId = (EditText) tempView.findViewById(R.id.registerId);
        Id = editTextId.getText().toString().trim();
        EditText editTextUsername = (EditText) tempView.findViewById(R.id.registerUsername);
        Username = editTextUsername.getText().toString().trim();
        EditText editTextPassword = (EditText) tempView.findViewById(R.id.registerPassword);
        Password = editTextPassword.getText().toString().trim();
        EditText editTextConfirmPassword = (EditText) tempView.findViewById(R.id.registerConfirmPassword);
        ConfirmPassword = editTextConfirmPassword.getText().toString().trim();
        EditText editTextEmail = (EditText) tempView.findViewById(R.id.registerEmail);
        Email = editTextEmail.getText().toString().trim();
        EditText editTextCode = (EditText)findViewById(R.id.registerVerificationCode);
        VerificationCode = editTextCode.getText().toString().trim();
        if(validate()){
            //使用Map封装请求参数
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("id",Id);
            hashMap.put("username",Username);
            hashMap.put("password",Password);
            hashMap.put("email",Email);
            hashMap.put("code",VerificationCode);
            //定义发送的请求url
            String url = HttpUtil.BASE_URL + "register";
            HttpUtil.postRequest(null,url,hashMap,RegisterActivity.this, REGISTER_CODE);
        }
    }
    //校验
    boolean validate(){
        if(Id.equals("")){
            String msg;
            if(role.equals("student")){
                msg = "学号不许为空";
            }else {
                msg = "身份证号不许为空";
            }
            DialogUtil.showDialog(this,msg,false);
            return false;
        }
        if(Username.equals("")){
            DialogUtil.showDialog(this,"账号不许为空",false);
            return false;
        }
        if(Password.equals("")){
            DialogUtil.showDialog(this,"密码不许为空",false);
            return false;
        }
        if(ConfirmPassword.equals("")){
            DialogUtil.showDialog(this,"确认密码行不许为空",false);
            return false;
        }
        if(!Password.equals(ConfirmPassword)){
            DialogUtil.showDialog(this,"两次输入密码不一致",false);
            return false;
        }
        if(Email.equals("")){
            DialogUtil.showDialog(this,"邮箱不许为空",false);
            return false;
        }
        if(VerificationCode.equals("")){
            DialogUtil.showDialog(this,"验证码不许为空",false);
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
        switch (code) {
            //获取验证码请求
            case GET_CODE_CODE:
                try {
                    jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("message");
                    DialogUtil.showDialog(this, msg, false);
                } catch (Exception e) {
                    DialogUtil.showDialog(this, "数据解析异常！", false);
                    e.printStackTrace();
                }
                break;
            //注册申请成功
            case REGISTER_CODE:
                try{
                    jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("message");
                    String msg1 = jsonObject.getString("tip");
                    if(msg.equals("注册成功！")){
                        DialogUtil.showDialog(this,msg1,false);
                        DialogUtil.showDialog(this,msg,false);
                    }else {
                        DialogUtil.showDialog(this,msg+msg1,false);
                    }
                } catch (Exception e) {
                    DialogUtil.showDialog(this, "数据解析异常！", false);
                    e.printStackTrace();
                }
                break;
            default:
                DialogUtil.showDialog(this, "未知请求！无法处理", false);
        }
    }
    @Override
    public void failed(IOException e, int code) {
        e.printStackTrace();
        switch (code){
            case GET_CODE_CODE:
                DialogUtil.showDialog(this,"服务器响应异常，请稍后重试！",false);
                break;
            case REGISTER_CODE:
                DialogUtil.showDialog(this,"服务器响应异常，请稍后重试！",false);
                break;
            default:
        }
    }
}