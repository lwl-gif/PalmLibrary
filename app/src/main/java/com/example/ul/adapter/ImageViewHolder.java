package com.example.ul.adapter;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;

/**
 * @Author: Wallace
 * @Description: 展示图片的列表项
 * @Date: 2021/4/17 11:19
 * @Modified: By yyyy-MM-dd
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ImageViewHolder";
    public ConstraintLayout rootView;
    public ImageButton imageBtn;
    public ImageButton imageDel;

    public ImageViewHolder(View itemView) {
        super(itemView);
        rootView = itemView.findViewById(R.id.root_image);
        imageBtn = rootView.findViewById(R.id.imageBtn);
        imageDel = rootView.findViewById(R.id.imageDel);
        Log.e(TAG, "ImageViewHolder: imageBtn = " + imageBtn);
        Log.e(TAG, "ImageViewHolder: imageDel = " + imageDel);
    }
}
