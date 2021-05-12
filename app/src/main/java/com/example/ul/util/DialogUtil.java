package com.example.ul.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.ul.R;
import com.example.ul.activity.LoginActivity;
import com.example.ul.adapter.ImagesAdapter;
import com.example.ul.fragment.BookFragment;
import com.example.ul.librarian.LMainActivity;


/**
 * @author luoweili
 */
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
        // 创建一个AlertDialog.Builder对象
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
    public static void showDialog(Context context , ImagesAdapter imagesAdapter, int position) {
        //先得到构造器
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //设置标题
        builder.setTitle("删除提示");
        //设置内容
        String msg = "您确定要删除它：" + imagesAdapter.getImagesPath().get(position);
        builder.setMessage(msg);
        //设置图标，图片id即可
        builder.setIcon(R.mipmap.ic_launcher);
        //设置确定按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            imagesAdapter.removeItem(position);
            imagesAdapter.setFirstDelete(false);
            dialog.dismiss(); //关闭dialog
        });
        //设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            imagesAdapter.setFirstDelete(false);
            dialog.dismiss();
        });
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    /**定义一个显示消息的对话框*/
    public static void showDialog(Activity activity, String msg, boolean finishActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity).setMessage(msg).setCancelable(false);
        if (finishActivity) {
            builder.setPositiveButton("确定", (dialog, which) -> {
                activity.finish();
            });
        } else {
            builder.setPositiveButton("确定", null);
        }
        builder.create().show();
    }
}