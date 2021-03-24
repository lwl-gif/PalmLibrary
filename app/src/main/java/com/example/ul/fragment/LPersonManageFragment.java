package com.example.ul.fragment;

/*
 * 管理员个人信息界面碎片
 * */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ul.R;
import com.example.ul.activity.AutoLoginActivity;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LPersonManageFragment extends Fragment {
    private static final String TAG = "LPersonDetailFragment";
    //视图
    private View rootView;
    //服务器返回的个人信息
    private JSONObject jsonObject;
    //id
    private TextView tId;
    //姓名
    private TextView tName;
    //性别
    private TextView tSex;
    //年龄
    private TextView tAge;
    //工作地点
    private TextView tWorkplace;
    //用户名
    private EditText tUsername;
    //权限
    private TextView tPermission;
    //刷新按钮
    private Button bReload;
    //清空应用中的个人信息并退出
    private Button bOut;

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
            fill();
        });
        //清空数据，退出登录
        bOut.setOnClickListener(view -> {
            UserInfo userInfo = UserManager.getInstance().getUserInfo(getActivity());
            //发出请求，告知服务器清除token
            String token = userInfo.getToken();
            //使用Map封装请求参数
            Map<String,String> map = new HashMap<>();
            map.put("token",token);
            //定义发送的请求url
            String url = HttpUtil.BASE_URL + "quit";
            try{
//                JSONObject jsonObject0 = new JSONObject(HttpUtil.postRequest(token,url,map));
                //退出，且清除数据
                userInfo.setRole(null);
                userInfo.setPassword(null);
                userInfo.setUserName(null);
                userInfo.setToken(null);
                SharedPreferences sp = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);//Context.MODE_PRIVATE表示SharedPreferences的数据只有自己应用程序能访问。
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("username", null);
                editor.putString("password", null);
                editor.putString("role",null);
                editor.putString("token",null);
                editor.commit();
                //销毁所有活动，返回登录界面
                Intent intent = new Intent(getActivity(), AutoLoginActivity.class);
                startActivity(intent);
                ActivityManager.getInstance().exit();
            } catch (Exception e) {
                DialogUtil.showDialog(getActivity(),TAG+"服务器响应异常！",false);
                //能退出，但不清除token
                userInfo.setRole(null);
                userInfo.setPassword(null);
                userInfo.setUserName(null);
                SharedPreferences sp = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);//Context.MODE_PRIVATE表示SharedPreferences的数据只有自己应用程序能访问。
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("username", null);
                editor.putString("password", null);
                editor.putString("role",null);
                editor.commit();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
//        query();
//        fill();
    }

    //查询个人信息
    private void query(){
        JSONObject jsonObject0 = new JSONObject();
        //从SharedPreferences中获取用户信息
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getActivity());
        String role = userInfo.getRole();
        String token = userInfo.getToken();
        //定义发送的请求url
        String url = HttpUtil.BASE_URL + role + "/" + "info";
        try{
//            jsonObject0 = new JSONObject(HttpUtil.getRequest(token,url));
            this.jsonObject = jsonObject0;
        } catch (Exception e) {
            DialogUtil.showDialog(getActivity(),TAG+":服务器响应异常！",false);
        }
    }
    //填充数据
    private void fill(){
//        query();
        if(jsonObject!=null){
            if(jsonObject.length()==0){
                DialogUtil.showDialog(getActivity(),"服务器无数据返回！",false);
            }else {
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
                    e.printStackTrace();
                }
            }
        }else {

        }
    }

}
