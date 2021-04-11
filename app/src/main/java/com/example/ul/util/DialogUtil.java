package com.example.ul.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ul.R;
import com.example.ul.activity.LoginActivity;
import com.example.ul.adapter.PictureListAdapter;


public class DialogUtil {

    /**定义一个显示消息的对话框*/
    public static void showDialog(Context context, String msg, boolean goLoginActivity) {
        //创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setMessage(msg).setCancelable(false);
        if (goLoginActivity) {
            builder.setPositiveButton("确定", (dialog, which) -> {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ActivityManager.getInstance().exit();
                context.startActivity(intent);
            });
        } else {
            builder.setPositiveButton("确定", null);
        }
        builder.create().show();
    }

    /**定义一个显示指定组件的对话框*/
    public static void showDialog(Context context, View view ,boolean goLoginActivity){
        //创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view).setCancelable(false);
        if (goLoginActivity) {
            builder.setPositiveButton("确定", (dialog, which) -> {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ActivityManager.getInstance().exit();
                context.startActivity(intent);
            });
        } else {
            builder.setPositiveButton("确定", null);
        }
        builder.create().show();
    }

    /**定义一个显示指定内容的对话框*/
    public static void showDialog(Context context, String tAG, Bundle data , boolean goLoginActivity){
        String code = data.getString("code");
        String message = data.getString("message");
        String tip = data.getString("tip");
        View view = View.inflate(context, R.layout.dialog_view,null);
        TextView tvFrom = view.findViewById(R.id.dialog_from);
        tvFrom.setText(tAG);
        TextView tvCode = view.findViewById(R.id.dialog_code);
        tvCode.setText(code);
        TextView tvMessage = view.findViewById(R.id.dialog_message);
        tvMessage.setText(message);
        TextView tvTip = view.findViewById(R.id.dialog_tip);
        tvTip.setText(tip);
        //创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view).setCancelable(false);
        if (goLoginActivity) {
            builder.setPositiveButton("确定", (dialog, which) -> {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ActivityManager.getInstance().exit();
                context.startActivity(intent);
            });
        } else {
            builder.setPositiveButton("确定", null);
        }
        builder.create().show();
    }

    /**定义一个图片删除的对话框*/
    public static void showDialog(Context context , PictureListAdapter pictureListAdapter , int position) {
        String title = "您确定要删除此图吗？";
        StringBuilder buffer = new StringBuilder("图片：");
        String name = pictureListAdapter.getData().get(position);
        String msg = buffer.append(name).toString();
        //创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(title).setMessage(msg).setCancelable(false);
        builder.setPositiveButton("确定", (dialog, which) -> {
            pictureListAdapter.removeItem(position);
        });
        builder.setNegativeButton("取消",null);
        builder.create().show();
    }
}