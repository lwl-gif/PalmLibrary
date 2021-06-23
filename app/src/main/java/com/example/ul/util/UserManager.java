package com.example.ul.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.ul.model.UserInfo;

/**
 * 保存用户信息的管理类
 */

public class UserManager {

    private static UserManager instance;

    private UserManager() {
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }


    /**
     * 保存自动登录的用户信息
     */
    public void saveUserInfo(Context context, String username, String password,String role ,String token) {
        // Context.MODE_PRIVATE表示SharedPreferences的数据只有自己应用程序能访问。
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("role",role);
        editor.putString( "token",token);
        // IO操作，异步执行
        editor.apply();
    }


    /**
     * 获取用户信息model
     *
     * @param context
     * @param
     * @param
     */
    public UserInfo getUserInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(sp.getString("username", ""));
        userInfo.setPassword(sp.getString("password", ""));
        userInfo.setToken(sp.getString("token", ""));
        userInfo.setRole(sp.getString("role", ""));
        return userInfo;
    }


    /**
     * userInfo中是否有数据
     */
    public boolean hasUserInfo(Context context) {
        UserInfo userInfo = getUserInfo(context);
        if (userInfo != null) {
            // 有数据
            if ((!TextUtils.isEmpty(userInfo.getUserName()))
                    && (!TextUtils.isEmpty(userInfo.getPassword()))
                    && (!TextUtils.isEmpty(userInfo.getToken()))
                    && (!TextUtils.isEmpty(userInfo.getRole()))) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}