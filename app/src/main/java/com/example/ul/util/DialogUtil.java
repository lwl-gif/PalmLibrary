package com.example.ul.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ul.R;
import com.example.ul.activity.LoginActivity;
import com.example.ul.adapter.ImagesAdapter;

import java.util.HashMap;


/**
 * @author luoweili
 */
public class DialogUtil {

    /**定义一个显示消息的对话框*/
    public static void showDialog(Context context, String msg, boolean goLoginActivity) {
        // 创建一个AlertDialog.Builder对象
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
    /**
     * @Author: Wallace
     * @Description: 显示一个选择对话框
     * @Date: Created 17:36 2021/5/19
     * @Modified: by who yyyy-MM-dd
     * @param context 上下文
     * @param positive 是否有正选按钮
     * @param negative 是否有反选按钮
     * @param dialogActionCallback 回调接口
     * @param hashMap 参数
     * @return: void
     */
    public static void showDialog(Context context, boolean positive, boolean negative, String title, final DialogActionCallback dialogActionCallback, final HashMap<String, Object> hashMap){
        View view = View.inflate(context, R.layout.dialog_view,null);
        TextView tvFrom = view.findViewById(R.id.dialog_from);
        String tAG = (String) hashMap.get("tAG");
        tvFrom.setText(tAG);
        TextView tvCode = view.findViewById(R.id.dialog_code);
        String code = (String) hashMap.get("code");
        tvCode.setText(code);
        TextView tvMessage = view.findViewById(R.id.dialog_message);
        String message = (String) hashMap.get("message");
        tvMessage.setText(message);
        TextView tvTip = view.findViewById(R.id.dialog_tip);
        String tip = (String) hashMap.get("tip");
        tvTip.setText(tip);
        // 创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view).setCancelable(false);
        // 设置标题
        builder.setTitle(title);
        // 设置图标，图片id即可
        builder.setIcon(R.drawable.main_background);
        if(positive){
            // 设置确定按钮
            builder.setPositiveButton("确定", (dialog, which) -> {
                dialogActionCallback.positiveAction(hashMap);
                dialog.dismiss();
            });
        }
        if(negative){
            // 设置取消按钮
            builder.setNegativeButton("取消", (dialog, which) -> {
                dialogActionCallback.negativeAction(hashMap);
                dialog.dismiss();
            });
        }
        // 参数都设置完成了，创建并显示出来
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

    /**定义一个图片删除的对话框*/
    public static void showDialog(Context context , ImagesAdapter imagesAdapter, int position) {
        // 先得到构造器
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 设置标题
        builder.setTitle("删除提示");
        // 设置内容
        String msg = "您确定要删除它：" + imagesAdapter.getImagesPath().get(position);
        builder.setMessage(msg);
        // 设置图标，图片id即可
        builder.setIcon(R.mipmap.ic_launcher);
        // 设置确定按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            imagesAdapter.removeItem(position);
            imagesAdapter.setFirstDelete(false);
            dialog.dismiss(); // 关闭dialog
        });
        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            imagesAdapter.setFirstDelete(false);
            dialog.dismiss();
        });
        // 参数都设置完成了，创建并显示出来
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
    
    /**定义一个需要回调动作的选择对话框*/
    public static void showDialog(Context context, String title, String msg,
                                  final DialogActionCallback dialogActionCallback,final HashMap<String, Object> hashMap) {
        // 先得到构造器
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 设置标题
        builder.setTitle(title);
        // 设置内容
        builder.setMessage(msg);
        // 设置图标，图片id即可
        builder.setIcon(R.drawable.main_background);
        // 设置确定按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            dialogActionCallback.positiveAction(hashMap);
            dialog.dismiss();
        });
        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialogActionCallback.negativeAction(hashMap);
            dialog.dismiss();
        });
        // 参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    /**
     * @Author: Wallace
     * @Description: 选择对话框的回调
     * @Date: 2021/5/13 23:10
     * @Modified: By yyyy-MM-dd
     */
    public interface DialogActionCallback {

        /**
         * @Author: Wallace
         * @Description: 选择确定按钮后的回调
         * @Date: Created 23:17 2021/5/13
         * @Modified: by who yyyy-MM-dd
         * @param requestParam 请求参数
         */
        void positiveAction(HashMap<String, Object> requestParam);
        /**
         * @Author: Wallace
         * @Description: 选择取消按钮后的回调
         * @Date: Created 23:17 2021/5/13
         * @Modified: by who yyyy-MM-dd
         * @param requestParam 请求参数
         */
        void negativeAction(HashMap<String, Object> requestParam);
    }
}