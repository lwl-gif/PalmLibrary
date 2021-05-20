package com.example.ul.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.example.ul.R;
import com.example.ul.callback.CallbackToApplicationFragment;
import com.example.ul.callback.CallbackToBookFragment;
import com.example.ul.model.Book;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @Author: Wallace
 * @Description:
 * @Date: 2021/3/10 11:21
 * @Modified By:
 */
public class BookListAdapter extends RecyclerView.Adapter<BookViewHolder> {
    private final String TAG = "BookListAdapter";
    private final Context context;
    /**访问服务器需携带的token*/
    private final String token;
    private final String baseUrl;
    /**数据源*/
    private ArrayList<Book> books;
    /**列表项单击事件回调接口*/
    private final CallbackToBookFragment callbackToBookFragment;
    protected final RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder1).centerCrop().error(R.drawable.error1);
    String state1 = "在馆";
    String state2 = "预约";
    String state3 = "挂失";

    public BookListAdapter(Context context, String baseUrl, String token,
                           ArrayList<Book> books, CallbackToBookFragment callbackToBookFragment){
        this.context = context;
        this.baseUrl = baseUrl;
        this.token = token;
        this.books = books;
        this.callbackToBookFragment = callbackToBookFragment;
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    public void setBooks(ArrayList<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }
    
    /**删除某一项*/
    public void deleteItem(int position){
        if(books.remove(position) != null){
            notifyItemRemoved(position);
        }
    }

    @NotNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book,viewGroup,false);
        return new BookViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        String itemId = books.get(position).getId()+"";
        String itemName = books.get(position).getName() == null ? "" : books.get(position).getName();
        String itemAuthor = books.get(position).getAuthor() == null ? "" : books.get(position).getAuthor();
        String itemDescription = books.get(position).getDescription()== null ? "" : books.get(position).getDescription();
        String itemHot = books.get(position).getHot()+"";
        String itemState = books.get(position).getState() == null ? "" : books.get(position).getState();
        String itemTheme = books.get(position).getTheme() == null ? "" : books.get(position).getTheme();
        String itemIsbn = books.get(position).getIsbn() == null ? "" : books.get(position).getIsbn();
        String itemImages = books.get(position).getImages() == null ? "" : books.get(position).getImages();
        String itemLibrary = books.get(position).getLibrary() == null ? "" : books.get(position).getLibrary();
        // 给列表项组件赋值
        holder.id.setText(itemId);
        holder.name.setText(itemName);
        holder.author.setText(itemAuthor);
        holder.description.setText(itemDescription);
        holder.hot.setText(itemHot);
        holder.state.setText(itemState);
        // 不同状态配不同的颜色
        if (state1.equals(itemState)) {
            holder.state.setTextColor(Color.GREEN);
        } else if (state2.equals(itemState)) {
            holder.state.setTextColor(Color.BLUE);
        } else if (state3.equals(itemState)) {
            holder.state.setTextColor(Color.RED);
        } else {
            holder.state.setTextColor(Color.BLACK);
        }
        holder.theme.setText(itemTheme);
        holder.library.setText(itemLibrary);
        // 判断书来源库
        String shareBook = "读者书库";
        if(itemLibrary.equals(shareBook)){
            holder.tvIsbn.setText(R.string.getIt);
            String getIt = "联系主人借阅该书";
            holder.isbn.setText(getIt);
        }else {
            holder.isbn.setText(itemIsbn);
        }
        String url = baseUrl + itemImages;
        // 获取图片
        ArrayList<String> pictures = books.get(position).getPictures();
        if (pictures != null && pictures.size() > 0) {
            String pictureName = pictures.get(0);
            url = url + "/" + pictureName;
        }
        GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization",token)
                .build());
        Glide.with(context)
                .applyDefaultRequestOptions(requestOptions)
                .load(glideUrl)
                .into(holder.bookImage);
        holder.bookRoot.setOnClickListener(view -> callbackToBookFragment.bookListClickPosition(holder.getLayoutPosition()));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}
