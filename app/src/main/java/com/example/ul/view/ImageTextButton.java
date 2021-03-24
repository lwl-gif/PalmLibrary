package com.example.ul.view;
/**
 * @Author:Wallace
 * @Description:自定义一个有图片+文字的单选按钮
 * @Date:2021/3/5 9:14
 * @Modified By:
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.ul.R;

@SuppressLint("AppCompatCustomView")
public class ImageTextButton extends RadioButton {
    private ImageView iv;
    private TextView tv;

    public ImageTextButton(Context context) {
        super(context);
        View rootView = LayoutInflater.from(context).inflate(R.layout.image_text_button, null, false);
        iv = (ImageView) rootView.findViewById(R.id.iv);
        tv = (TextView) rootView.findViewById(R.id.tv);
//        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }
    public ImageTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        View rootView = LayoutInflater.from(context).inflate(R.layout.image_text_button, null, false);
        iv = (ImageView) rootView.findViewById(R.id.iv);
        tv = (TextView) rootView.findViewById(R.id.tv);
//        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }
    public ImageTextButton(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        View rootView = LayoutInflater.from(context).inflate(R.layout.image_text_button, null, false);
        iv = (ImageView) rootView.findViewById(R.id.iv);
        tv = (TextView) rootView.findViewById(R.id.tv);
    }
    public void setDefaultImageResource(int resId) {
        iv.setImageResource(resId);
    }

    public void setDefaultTextViewText(String text) {
        tv.setText(text);
    }

    /**
     * @param resId
     */
    public void setImageResource(int resId) {
        iv.setImageResource(resId);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    /**
     * @param text
     */
    public void setTextViewText(String text) {
        tv.setText(text);
    }

    /**
     * @param color
     */
    public void setTextColor(int color) {
        tv.setTextColor(color);
    }
}