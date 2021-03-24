package com.example.ul.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.ul.activity.LoginActivity;


public class DialogUtil {

    //定义一个显示消息的对话框
    public static void showDialog(Context context, String msg, boolean goHome) {
        //创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setMessage(msg).setCancelable(false);
        if (goHome) {
            builder.setPositiveButton("确定", (dialog, which) -> {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            });
        } else {
            builder.setPositiveButton("确定", null);
        }
        builder.create().show();
    }

    //定义一个显示指定组件的对话框
    public static void showDialog(Context context, View view){
        new AlertDialog.Builder(context)
                .setView(view).setCancelable(false)
                .setPositiveButton("确定",null)
                .create()
                .show();
    }
}