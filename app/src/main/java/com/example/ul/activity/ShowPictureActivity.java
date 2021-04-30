package com.example.ul.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ul.R;
import com.example.ul.adapter.ImagesAdapter;
import com.example.ul.adapter.ImagesOnlyReadAdapter;
import com.example.ul.util.ActivityManager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;

/**
 * @Author: Wallace
 * @Description: 展示图片大图的活动
 * @Date: Created in 16:25 2021/4/7
 * @Modified By:
 * @return:
 */
@SuppressLint("NonConstantResourceId")
public class ShowPictureActivity extends Activity{

    private static final String TAG = "ShowPictureActivity";
    @BindView(R.id.imageButton_back)
    public ImageButton imageButton;
    @BindView(R.id.tv_now)
    public TextView tvNow;
    @BindView(R.id.tv_all)
    public TextView tvAll;
    @BindView(R.id.imageView)
    public ImageView imageView;
    public ArrayList<String> imagesPath;

    /**当前是第几张图片*/
    private int position;
    /**图片总数*/
    private int total = -1;
    private RequestOptions requestOptions = new RequestOptions().error(R.mipmap.error0);
    /**定义手势监听对象*/
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_picture);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Log.e(TAG, "onCreate: intent" + intent);
        // 获取来源
        String tag = intent.getStringExtra("TAG");
        Log.e(TAG, "onCreate:tag = " + tag);
        // 获取数据
        imagesPath = intent.getStringArrayListExtra("imagesPath");
        if(imagesPath != null && tag != null){
            total = imagesPath.size();
        }else {
            finish();
        }
        Log.e(TAG, "onCreate: total = " + total);
        // 获取点击位置
        position = intent.getIntExtra("position",0) + 1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        imageButton.setOnClickListener(view -> ShowPictureActivity.this.finish());
        tvNow.setText(String.valueOf(position));
        tvAll.setText(String.valueOf(total));
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(imagesPath.get(position - 1)).into(imageView);
        //设置手势监听由SimpleOnGestureListener处理
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            //当识别的手势是滑动手势时回调onFinger方法
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //得到手触碰位置的起始点和结束点坐标 x , y ，并进行计算
                float x = e2.getX() - e1.getX();
                float y = e2.getY() - e1.getY();
                //通过计算判断是向左还是向右滑动
                if (x < 0) {
                    //向右
                    if (position < total) {
                        position++;
                        tvNow.setText(String.valueOf(position));
                        Glide.with(ShowPictureActivity.this).applyDefaultRequestOptions(requestOptions).load(imagesPath.get(position - 1)).into(imageView);
                    } else {
                        Toast.makeText(ShowPictureActivity.this, "已经是最后一张了！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //向左
                    if (position > 1) {
                        position--;
                        tvNow.setText(String.valueOf(position));
                        Glide.with(ShowPictureActivity.this).applyDefaultRequestOptions(requestOptions).load(imagesPath.get(position - 1)).into(imageView);
                    } else {
                        Toast.makeText(ShowPictureActivity.this, "已经是第一张了！", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }

    /**当Activity被触摸时回调*/
    @Override
    public boolean onTouchEvent(MotionEvent event){
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageButton = null;
        tvNow = null;
        tvAll = null;
        imageView = null;
        imagesPath = null;
        position = 0;
        requestOptions = null;
        ActivityManager.getInstance().removeActivity(this);
    }
}