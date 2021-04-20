package com.example.ul.adapter;

import android.view.View;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;

/**
 * @Author: Wallace
 * @Description: 展示图片的列表项
 * @Date: 2021/4/17 11:19
 * @Modified: By yyyy-MM-dd
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {

    protected ImageButton imageButton;
    protected ImageButton imageDelete;

    public ImageViewHolder(View itemView) {
        super(itemView);
        imageButton = itemView.findViewById(R.id.imageButton);
        imageDelete = itemView.findViewById(R.id.image_delete);
    }
}
