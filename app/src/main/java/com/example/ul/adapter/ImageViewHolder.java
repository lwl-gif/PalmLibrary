package com.example.ul.adapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;

/**
 * @Author: Wallace
 * @Description: 展示图片的列表项
 * @Date: 2021/4/17 11:19
 * @Modified: By yyyy-MM-dd
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {

    public ImageButton imageBtn;
    public ImageButton imageDel;

    public ImageViewHolder(View itemView) {
        super(itemView);
        imageBtn = itemView.findViewById(R.id.imageBtn);
        imageDel = itemView.findViewById(R.id.imageDel);
    }
}
