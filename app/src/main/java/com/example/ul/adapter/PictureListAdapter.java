package com.example.ul.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.Request;
import com.example.ul.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ul.util.HttpUtil;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/4/6 14:25
 * @Modified By:
 */
public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.ViewHolder> {

    private final String TAG = "PictureListAdapter";
    private Context context;
    /**item是否实现点击（大图查看）事件*/
    private boolean canClick;
    /**item是否实现长按（删除）事件*/
    private boolean canLongClick;
    private LayoutInflater inflater;
    private String baseUrl;
    private String token;
    /**图片名*/
    private ArrayList<String> data;
    RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder1).error(R.drawable.error1);
    /**对外提供一个接口，让Activity实现对item的单击和长按事件*/
    public interface  ItemClickListener{
        /**
         * @Author:Wallace
         * @Description:单击事件
         * @Date:Created in 16:06 2021/4/7
         * @Modified By:
         * @param position 点击的位置
         * @return:
         */
        void onItemClick(int position);
        /**
         * @Author:Wallace
         * @Description:长按事件
         * @Date:Created in 16:06 2021/4/7
         * @Modified By:
         * @param position 长按的位置
         * @return:
         */
        void  onItemLongClick(PictureListAdapter pictureListAdapter, int position);
    }
    public interface ItemShow{
        /**
         * @Author:Wallace
         * @Description:展示此适配器中所有的图片
         * @Date:Created in 16:06 2021/4/7
         * @Modified By:
         * @param pictureListAdapter
         * @return:
         */
        void showByBigPicture(PictureListAdapter pictureListAdapter,int position);
    }

    private ItemClickListener itemClickListener;
    private ItemShow itemShow;

    public ItemShow getItemShow() {
        return itemShow;
    }

    public void setItemShow(ItemShow itemShow) {
        this.itemShow = itemShow;
    }
    public PictureListAdapter(Context context, ItemClickListener itemClickListener, String baseUrl, String token,
                              ArrayList<String> data, boolean canClick, boolean canLongClick) {
        this.context = context;
        this.itemClickListener = itemClickListener;
        inflater = LayoutInflater.from(context);
        this.baseUrl = baseUrl;
        this.token = token;
        this.data = data;
        this.canClick = canClick;
        this.canLongClick = canLongClick;
    }
    public ArrayList<String> getData() {
        return data;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public String getToken() {
        return token;
    }
    @NonNull
    @Override
    public PictureListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureListAdapter.ViewHolder holder, int position) {
        String url = baseUrl + this.data.get(position);
        GlideUrl glideUrl = new GlideUrl(url,new LazyHeaders.Builder().addHeader("Authorization", token).build());
        Glide.with(context)
                .applyDefaultRequestOptions(requestOptions)
                .load(glideUrl)
                .into(holder.imageButton);
        String s = data.get(position);
        holder.name.setText(s);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addItem(int position, String drawable){
        data.add(position,drawable);
        notifyItemInserted(position);
    }

    public void removeItem(int position){
        data.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageButton imageButton;
        private TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.imageButton);
            name = itemView.findViewById(R.id.textView);
            if(canClick){
                imageButton.setOnLongClickListener(view -> {
                    itemClickListener.onItemLongClick(PictureListAdapter.this,getLayoutPosition());
                    return false;
                });
            }
            if(canLongClick){
                imageButton.setOnClickListener(view -> {
                    itemClickListener.onItemClick(getLayoutPosition());
                });
            }
        }
    }
}
