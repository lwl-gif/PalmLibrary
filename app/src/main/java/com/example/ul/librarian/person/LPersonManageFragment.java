package com.example.ul.librarian.person;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import okhttp3.Response;

/**
 * @author luoweili
 */
public class LPersonManageFragment extends Fragment implements HttpUtil.MyCallback{

    private static final String TAG = "LPersonManageFragment";
    /**未知请求*/
    private static final int UNKNOWN_REQUEST_ERROR = 700;
    /**请求失败*/
    private static final int REQUEST_FAIL = 7000;
    /**获取个人详情*/
    private static final int GET_PERSON_DETAIL = 701;
    /**获取个人详情成功，有数据需要渲染*/
    private static final int GET_PERSON_DETAIL_FILL = 7011;
    /**获取个人详情失败或无数据需要渲染*/
    private static final int GET_PERSON_DETAIL_NOT_FILL = 7010;
    /**退出登录请求*/
    private static final int ACCOUNT_OFFLINE = 702;
    /**退出成功*/
    private static final int ACCOUNT_OFFLINE_SUCCEED = 7021;
    /**退出失败*/
    private static final int ACCOUNT_OFFLINE_FAIL = 7020;

    private String token;
    /**服务器返回的个人信息详情*/
    private JSONObject jsonObject;
    private TextView tId,tName,tSex,tAge,tWorkplace,tUsername,tPermission;

    /**刷新按钮*/
    private Button bReload;
    /**清空应用中的个人信息并退出的按钮*/
    private Button bOut;

    static class MyHandler extends Handler {
        private WeakReference<LPersonManageFragment> lPersonManageFragment;
        public MyHandler(WeakReference<LPersonManageFragment> lPersonManageFragment){
            this.lPersonManageFragment = lPersonManageFragment;
        }
        @Override
        public void handleMessage(Message msg){
            Activity myActivity = lPersonManageFragment.get().getActivity();
            int what = msg.what;
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            }else if (what == GET_PERSON_DETAIL_FILL) {
                lPersonManageFragment.get().fill();
            } else {
                UserInfo userInfo = UserManager.getInstance().getUserInfo(myActivity);
                if (what == ACCOUNT_OFFLINE_SUCCEED){
                    // 退出，且清除数据
                    userInfo.setRole(null);
                    userInfo.setPassword(null);
                    userInfo.setUserName(null);
                    userInfo.setToken(null);
                    // Context.MODE_PRIVATE表示SharedPreferences的数据只有自己应用程序能访问。
                    SharedPreferences sp = myActivity.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("username", null);
                    editor.putString("password", null);
                    editor.putString("role",null);
                    editor.putString("token",null);
                    editor.apply();
                }
                if(what == ACCOUNT_OFFLINE_FAIL){
                    // 能退出，但不清除数据
                    Toast.makeText(myActivity,"网络异常！保存数据并退出！",Toast.LENGTH_LONG).show();
                }
                // 销毁所有活动
                ActivityManager.getInstance().exit();
            }
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 从SharedPreferences中获取用户信息
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getActivity());
        token = userInfo.getToken();
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_l_person_detail, container, false);
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
        // 刷新页面
        bReload.setOnClickListener(view -> {
            query();
        });
        // 清空数据，退出登录
        bOut.setOnClickListener(view -> {
            // 使用Map封装请求参数
            HashMap<String,String> map = new HashMap<>();
            map.put("skip","null");
            // 定义发送的请求url
            String url = HttpUtil.BASE_URL + "quit";
            HttpUtil.postRequest(token,url,map,this,ACCOUNT_OFFLINE);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        query();
    }

    /**查询个人信息*/
    private void query(){
        // 定义发送的请求url
        String url = HttpUtil.BASE_URL + "librarian/info";
        HttpUtil.getRequest(token,url,this,GET_PERSON_DETAIL);
    }

    private void fill() {
        if (jsonObject != null) {
            // 组件赋值
            tId.setText(jsonObject.getString("id"));
            tName.setText(jsonObject.getString("name"));
            // 获取性别代号
            String sex = jsonObject.getString("sex");
            if ("1".equals(sex)) {
                tSex.setText("男");
            } else {
                tSex.setText("女");
            }
            tAge.setText(jsonObject.getString("age"));
            tWorkplace.setText(jsonObject.getString("workplace"));
            tUsername.setText(jsonObject.getString("username"));
            tPermission.setText(jsonObject.getString("permission"));
        } else {
            DialogUtil.showDialog(getActivity(), "无数据！", false);
        }
    }

    @Override
    public void success(Response response, int code) throws IOException {
        // 获取服务器响应字符串
        String result = response.body().string().trim();
        JSONObject resultObject = JSON.parseObject(result);
        Message msg = new Message();
        Bundle data = new Bundle();
        if (code == GET_PERSON_DETAIL) {
            String message = resultObject.getString("message");
            if ("查询成功！".equals(message)) {
                jsonObject = resultObject.getJSONObject("object");
                myHandler.sendEmptyMessage(GET_PERSON_DETAIL_FILL);
            } else {
                String c = resultObject.getString("code");
                String tip = resultObject.getString("tip");
                data.putString("code", c);
                data.putString("tip", tip);
                data.putString("message", message);
                msg.setData(data);
                msg.what = GET_PERSON_DETAIL_NOT_FILL;
                myHandler.sendMessage(msg);
            }
        } else if (code == ACCOUNT_OFFLINE) {
            myHandler.sendEmptyMessage(ACCOUNT_OFFLINE_SUCCEED);
        } else {
            data.putString("reason", "未知错误");
            msg.setData(data);
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