package com.example.ul.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ul.R;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import org.json.JSONObject;
import com.example.ul.util.CountDownTimerUtil;
import com.example.ul.util.UserManager;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class ForgetPasswordActivity extends AppCompatActivity implements HttpUtil.MyCallback{

    //自定义消息代码
    private static final int GET_CODE_CODE = 301; //获取邮箱验证码
    private static final int VERIFY_CODE_CODE = 302; //验证邮箱验证码
    private static final int UPDATE_PASSWORD_CODE = 303; //修改密码

    private EditText forgetEmailAddress;
    private Button forgetGetCode;
    private EditText forgetVerificationCode;
    private Button forgetBack;
    private Button forgetClear;
    private Button forgetSubmit;

    private EditText forgetNextPassword;
    private EditText forgetNextConfirmPassword;
    private Button forgetNextBack;
    private Button forgetNextClear;
    private Button forgetNextSubmit;

    private String email;//界面保存的邮箱
    private String password;//界面保存的密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        changeTo("forget");
    }

    void back(){
        finish();
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

    //获取验证码
    void getCode(){
        //检查邮箱是否填写
        email = forgetEmailAddress.getText().toString().trim();
        if(email ==null|| email.equals("")){
            DialogUtil.showDialog(this,"请先填写邮箱",false);
        }else {
            //禁用按钮一分钟
            CountDownTimerUtil countDownTimerUtil = new CountDownTimerUtil (forgetGetCode,60000,1000);
            countDownTimerUtil.start();
            //使用Map封装请求参数
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("email", email);
            //定义发送的请求url
            String url = HttpUtil.BASE_URL + "forget/emailcode";
            HttpUtil.postRequest(null,url,hashMap,ForgetPasswordActivity.this,GET_CODE_CODE);
        }
    }

    void submit(){
        /**
         * @Author:Wallace
         * @Description:“下一步”按钮的事件
         * @Date:Created in 19:30 2021/3/3
         * @Modified By:
         * @param
         * @return: void
         */
        //检查邮箱是否填写
        if(email ==null|| email.equals("")){
            DialogUtil.showDialog(this,"请先填写邮箱",false);
        }else {
            //检查验证码是否填写
            String code = forgetVerificationCode.getText().toString().trim();
            if(code!=null&&!code.equals("")){
                //发送邮箱和验证码，待服务器验证
                //使用Map封装请求参数
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("email", email);
                hashMap.put("code",code);
                //定义发送的请求url
                String url = HttpUtil.BASE_URL + "forget/verify";
                HttpUtil.postRequest(null,url,hashMap,this,VERIFY_CODE_CODE);
            }else {
                DialogUtil.showDialog(this,"验证码不能为空",false);
            }
        }
    }

    void changeTo(String s){
        if(s.equals("forgetNext")){
            setContentView(R.layout.activity_forget_password_next);
            forgetNextPassword = findViewById(R.id.forgetNextPassword);
            forgetNextConfirmPassword = findViewById(R.id.forgetNextConfirmPassword);
            forgetNextBack = findViewById(R.id.forgetNextBack);
            forgetNextBack.setOnClickListener(view -> {
                changeTo("forget");
            });
            forgetNextClear = findViewById(R.id.forgetNextClear);
            forgetNextClear.setOnClickListener(view -> {
                forgetNextPassword.setText("");
                forgetNextConfirmPassword.setText("");
            });
            forgetNextSubmit = findViewById(R.id.forgetNextSubmit);
            forgetNextSubmit.setOnClickListener(view -> {
                //检验两次输入的密码是否为空且相同
                String password = forgetNextPassword.getText().toString().trim();
                if(!password.equals("")){
                    String confirmPassword = forgetNextConfirmPassword.getText().toString().trim();
                    if(!confirmPassword.equals("")){
                        if(password.equals(confirmPassword)){
                            //发送修改密码请求
                            //使用Map封装请求参数
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("newPassword",password);
                            this.password = password;
                            //定义发送的请求url
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
            forgetBack = findViewById(R.id.forgetBack);
            forgetBack.setOnClickListener(view -> {
                back();
            });
            forgetClear = findViewById(R.id.forgetClear);
            forgetClear.setOnClickListener(view -> {
                clear();
            });
            forgetSubmit = findViewById(R.id.forgetSubmit);
            forgetSubmit.setOnClickListener(view -> {
                submit();
            });
        }
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
                try{
                    jsonObject = new JSONObject();
                    String msg = jsonObject.getString("message");
                    DialogUtil.showDialog(this,msg,false);
                } catch (Exception e) {
                    DialogUtil.showDialog(this,"数据解析异常！",false);
                    e.printStackTrace();
                }
                break;
            //验证验证码请求
            case VERIFY_CODE_CODE:
                try{
                    jsonObject = new JSONObject();
                    //如果服务器验证成功，开始下一步
                    if(jsonObject.getString("message").equals("验证成功!")){
                        //加载填新密码的页面
                        changeTo("forgetNext");
                    }else {
                        String msg = jsonObject.getString("message");
                        String msg1 = jsonObject.getString("tip");
                        DialogUtil.showDialog(this,msg+msg1,false);
                    }
                } catch (Exception e) {
                    DialogUtil.showDialog(this,"数据解析异常！",false);
                    e.printStackTrace();
                }
                break;
            //更新密码请求
            case UPDATE_PASSWORD_CODE:
                try{
                    jsonObject = new JSONObject();
                    String msg="";
                    //如果修改成功
                    if(jsonObject.getString("message").equals("密码修改成功!")){
                        msg = jsonObject.getString("message");
                        //更改本机缓存中的用户密码
                        UserInfo userInfo = UserManager.getInstance().getUserInfo(ForgetPasswordActivity.this);
                        String username = userInfo.getUserName();
                        String role = userInfo.getRole();
                        String token = userInfo.getToken();
                        UserManager.getInstance().saveUserInfo(ForgetPasswordActivity.this,username,password,role,token);
                        DialogUtil.showDialog(this,msg,true);
                    }else {
                        msg = jsonObject.getString("message");
                        msg = msg + jsonObject.getString("tip");
                        DialogUtil.showDialog(this,msg,false);
                    }
                } catch (Exception e) {
                    DialogUtil.showDialog(this,"数据解析异常！",false);
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
            case VERIFY_CODE_CODE:
                DialogUtil.showDialog(this,"服务器响应异常，请稍后重试！",false);
                break;
            case UPDATE_PASSWORD_CODE:
                DialogUtil.showDialog(this,"服务器响应异常，请稍后重试！",false);
                break;
            default:
        }
    }
}