package com.example.ul.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.example.ul.R;

/**
 * @Author:Wallace
 * @Description:自定义的带图片的RadioButton类
 * @Date:2021/3/6 11:31
 * @Modified By:
 */

public class MyRadioButton extends androidx.appcompat.widget.AppCompatRadioButton {
    private Drawable drawable;
    private Drawable drawable0;
    public MyRadioButton(Context context) {
        super(context);
    }

    public MyRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyRadioButton);//获取我们定义的属性
//        drawable = typedArray.getDrawable(R.styleable.MyRadioButton_drawableTop);
//        drawable.setBounds(0, 0, 60, 60);
//        setCompoundDrawables(null, drawable, null, null);
        drawable0 = typedArray.getDrawable(R.styleable.MyRadioButton_drawableBottom);
        drawable0.setBounds(0, 0, 120, 120);
        setCompoundDrawables(null, null, null, drawable0);
    }
}
