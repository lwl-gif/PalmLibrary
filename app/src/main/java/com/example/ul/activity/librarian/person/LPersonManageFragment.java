package com.example.ul.activity.librarian.person;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ul.R;
import com.example.ul.activity.AutoLoginActivity;
import com.example.ul.activity.reader.main.activity.RBookDetailActivity;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class LPersonManageFragment extends Fragment implements HttpUtil.MyCallback{

    private static final String TAG = "LPersonManageFragment";
    //自定义消息代码
    //未知请求
    private static final int UNKNOWN_REQUEST = 700;
    //请求失败
    private static final int REQUEST_FAIL = 7000;
    //请求成功，但子线程解析数据失败
    private static final int REQUEST_BUT_FAIL_READ_DATA = 7001;
    //获取个人详情
    private static final int GET_PERSON_DETAIL = 701;
    //获取个人详情成功，有数据需要渲染
    private static final int GET_PERSON_DETAIL_FILL = 7011;
    //获取个人详情失败或无数据需要渲染
    private static final int GET_PERSON_DETAIL_NOT_FILL = 7010;
    //退出登录请求
    private static final int ACCOUNT_OFFLINE = 702;
    //退出成功
    private static final int ACCOUNT_OFFLINE_SUCCEED = 7021;
    //退出失败
    private static final int ACCOUNT_OFFLINE_FAIL = 7020;
    //视图
    private View rootView;
    //服务器返回的个人信息
    private JSONObject jsonObject;
    private TextView tId,tName,tSex,tAge,tWorkplace,tUsername,tPermission;

    //刷新按钮
    private Button bReload;
    //清空应用中的个人信息并退出
    private Button bOut;
    static class MyHandler extends Handler {
        private WeakReference<LPersonManageFragment> lPersonManageFragment;
        public MyHandler(WeakReference<LPersonManageFragment> lPersonManageFragment){
            this.lPersonManageFragment = lPersonManageFragment;
        }
        public void handleMessage(Message msg){
            Activity myActivity = lPersonManageFragment.get().getActivity();
            int what = msg.what;
            if(what == UNKNOWN_REQUEST) {
                Toast.makeText(myActivity,"未知请求，无法处理！",Toast.LENGTH_SHORT).show();
            }
            else if(what == REQUEST_FAIL){
                Toast.makeText(myActivity,"网络异常！",Toast.LENGTH_SHORT).show();
            }else if(what == REQUEST_BUT_FAIL_READ_DATA){
                Toast.makeText(myActivity,"子线程解析数据异常！",Toast.LENGTH_SHORT).show();
            } else if (what == GET_PERSON_DETAIL_FILL) {
                lPersonManageFragment.get().fill();
            } else {
                UserInfo userInfo = UserManager.getInstance().getUserInfo(myActivity);
                if (what == ACCOUNT_OFFLINE_SUCCEED){
                    //退出，且清除数据
                    userInfo.setRole(null);
                    userInfo.setPassword(null);
                    userInfo.setUserName(null);
                    userInfo.setToken(null);
                    SharedPreferences sp = myActivity.getSharedPreferences("userInfo", Context.MODE_PRIVATE);//Context.MODE_PRIVATE表示SharedPreferences的数据只有自己应用程序能访问。
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("username", null);
                    editor.putString("password", null);
                    editor.putString("role",null);
                    editor.putString("token",null);
                    editor.commit();
                }
                if(what == ACCOUNT_OFFLINE_FAIL){
                    //能退出，但不清除数据
                    Toast.makeText(myActivity,"网络异常！保存数据并退出！",Toast.LENGTH_LONG);
                }
                //销毁所有活动
                ActivityManager.getInstance().exit();
            }
        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //获取当前界面视图
        rootView = inflater.inflate(R.layout.activity_l_person_detail,container,false);
        //获取视图中的组件
        tId = rootView.findViewById(R.id.l_id);
        tName = rootView.findViewById(R.id.l_name);
        tSex = rootView.findViewById(R.id.l_sex);
        tAge = rootView.findViewById(R.id.l_age);
        tWorkplace = rootView.findViewById(R.id.l_workplace);
        tUsername = rootView.findViewById(R.id.l_username);
        tPermission = rootView.findViewById(R.id.l_permission);
        bReload = rootView.findViewById(R.id.btn_reload);
        bOut = rootView.findViewById(R.id.btn_out);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //刷新页面
        bReload.setOnClickListener(view -> {
            query();
        });
        //清空数据，退出登录
        bOut.setOnClickListener(view -> {
            UserInfo userInfo = UserManager.getInstance().getUserInfo(getActivity());
            //发出请求，告知服务器清除token
            String token = userInfo.getToken();
            //使用Map封装请求参数
            HashMap<String,String> map = new HashMap<>();
            map.put("skip","null");
            //定义发送的请求url
            String url = HttpUtil.BASE_URL + "quit";
            HttpUtil.postRequest(token,url,map,this,ACCOUNT_OFFLINE);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        query();
    }

    //查询个人信息
    private void query(){
        //从SharedPreferences中获取用户信息
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getActivity());
        String role = userInfo.getRole();
        String token = userInfo.getToken();
        //定义发送的请求url
        String url = HttpUtil.BASE_URL + role + "/" + "info";
        HttpUtil.getRequest(token,url,this,GET_PERSON_DETAIL);
    }

    //填充数据
    private void fill(){
        if(jsonObject!=null){
            //组件赋值
            try {
                tId.setText(jsonObject.getString("id"));
                tName.setText(jsonObject.getString("name"));
                //获取性别代号
                String sex = jsonObject.getString("sex");
                if(sex.equals("1")){
                    tSex.setText("男");
                }else {
                    tSex.setText("女");
                }
                tAge.setText(jsonObject.getString("age"));
                tWorkplace.setText(jsonObject.getString("workplace"));
                tUsername.setText(jsonObject.getString("username"));
                tPermission.setText(jsonObject.getString("permission"));
            } catch (JSONException e) {
                Toast.makeText(this.getActivity(), "主线程解析数据时异常！", Toast.LENGTH_SHORT).show();
            }
        }else {
            DialogUtil.showDialog(getActivity(),"无数据！",false);
        }
    }

    @Override
    public void success(Response response, int code) throws IOException {
        //服务器返回的数据
        String result = null;
        //获取服务器响应字符串
        result = response.body().string().trim();
        switch (code) {
            case GET_PERSON_DETAIL:
                try {
                    jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String tip = null;
                    if(message.equals("查询成功！")){
                        tip = jsonObject.getString("tip");
                        if(tip == null || tip.equals("null")){
                            //查询成功，获取书籍数据，通知主线程渲染前端
                            jsonObject = jsonObject.getJSONObject("object");
                            myHandler.sendEmptyMessage(GET_PERSON_DETAIL_FILL);
                        }
                    } else {
                        String c = jsonObject.getString("code");
                        tip = jsonObject.getString("tip");
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("code",c);
                        data.putString("tip",tip);
                        data.putString("message",message);
                        msg.setData(data);
                        msg.what = GET_PERSON_DETAIL_NOT_FILL;
                        myHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                    e.printStackTrace();
                }
                break;
            case ACCOUNT_OFFLINE:
                myHandler.sendEmptyMessage(ACCOUNT_OFFLINE_SUCCEED);
                break;
            default:
                myHandler.sendEmptyMessage(UNKNOWN_REQUEST);
        }
    }

    @Override
    public void failed(IOException e, int code) {
        if(code == ACCOUNT_OFFLINE){
            myHandler.sendEmptyMessage(ACCOUNT_OFFLINE_FAIL);
        }else {
            myHandler.sendEmptyMessage(REQUEST_FAIL);
        }
    }
}