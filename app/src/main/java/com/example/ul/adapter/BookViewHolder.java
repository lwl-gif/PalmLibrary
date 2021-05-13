package com.example.ul.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;

/**
 * @Author: Wallace
 * @Description: 文件描述
 * @Date: 2021/4/25 0:16
 * @Modified: By yyyy-MM-dd
 */
public class BookViewHolder extends RecyclerView.ViewHolder{
    /**总布局*/
    public LinearLayout bookRoot;
    public LinearLayout bookRootLeft;
    public LinearLayout bookRootRight;
    public TextView id;
    public TextView name;
    public TextView author;
    public TextView description;
    public TextView hot;
    public TextView state;
    public TextView theme;
    public TextView tvIsbn;
    public TextView isbn;
    public TextView library;
    public ImageView bookImage;
    public TextView delete;

    public BookViewHolder(View itemView) {
        super(itemView);
        bookRoot = itemView.findViewById(R.id.book_root);
        bookRootLeft = bookRoot.findViewById(R.id.linearLayout1_1);
        bookRootRight = bookRoot.findViewById(R.id.linearLayout1_2);
        id = itemView.findViewById(R.id.bookId);
        name = itemView.findViewById(R.id.bookName);
        author = itemView.findViewById(R.id.bookAuthor);
        description = itemView.findViewById(R.id.bookDescription);
        hot = itemView.findViewById(R.id.bookHot);
        state = itemView.findViewById(R.id.bookState);
        theme = itemView.findViewById(R.id.bookTheme);
        tvIsbn = itemView.findViewById(R.id.tv_bookIsbn);
        isbn = itemView.findViewById(R.id.bookIsbn);
        library = itemView.findViewById(R.id.bookLibrary);
        bookImage = itemView.findViewById(R.id.imageView);
        delete = itemView.findViewById(R.id.tv_delete);
    }
}
