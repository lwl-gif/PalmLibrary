package com.example.ul.adapter;

import android.annotation.SuppressLint;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.ul.R;
import com.example.ul.callback.ImageAdapterItemListener;
import com.example.ul.util.HttpUtil;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author: Wallace
 * @Description: 展示图片的列表适配器，只读（只看不可修改）
 * @Date: 2021/4/17 11:21
 * @Modified: By yyyy-MM-dd
 */
public class ImagesOnlyReadAdapter extends RecyclerView.Adapter<ImageViewHolder> implements Parcelable{

    private static final String TAG = "ImagesOnlyReadAdapter";
    protected Context context;
    /**
     * 访问服务器需携带的token
     */
    protected String token;
    /**
     * 基本url+从服务器端传来的图片名构成的完整的url的List
     */
    protected ArrayList<String> imageNameUrlList;
    /**
     * 本适配器拥有的所有图片的本地全路径（网络图片缓存全路径+本地图片全路径）
     */
    protected ArrayList<String> imagesPath;
    protected ImageAdapterItemListener imageAdapterItemListener;
    protected final RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder0).centerCrop().error(R.mipmap.error0);

    public ImagesOnlyReadAdapter(Context context, String token) {
        init(context);
        this.context = context;
        this.token = token;
        this.imageNameUrlList = new ArrayList<>();
        this.imagesPath  = new ArrayList<>();
    }

    protected void init(Context context) {
        if (!(context instanceof ImageAdapterItemListener)) {
            throw new IllegalStateException(TAG+"所在的Context必须实现ImageAdapterItemListener接口");
        }
        this.imageAdapterItemListener = (ImageAdapterItemListener)context;
    }

    protected ImagesOnlyReadAdapter(Parcel in) {
        token = in.readString();
        imageNameUrlList = in.createStringArrayList();
        imagesPath = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(token);
        dest.writeStringList(imageNameUrlList);
        dest.writeStringList(imagesPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImagesOnlyReadAdapter> CREATOR = new Creator<ImagesOnlyReadAdapter>() {
        @Override
        public ImagesOnlyReadAdapter createFromParcel(Parcel in) {
            return new ImagesOnlyReadAdapter(in);
        }

        @Override
        public ImagesOnlyReadAdapter[] newArray(int size) {
            return new ImagesOnlyReadAdapter[size];
        }
    };

    public ArrayList<String> getImagesPath() {
        ArrayList<String> arrayList = new ArrayList<>();
        for(int i = 0; i < imagesPath.size() ; i++){
            arrayList.add(imagesPath.get(i));
        }
        return arrayList;
    }

    public void setImageNameUrlList(ArrayList<String> imageNameUrlList) {
        this.imageNameUrlList.clear();
        this.imagesPath.clear();
        for(int i = 0; i < imageNameUrlList.size(); i ++){
            this.imageNameUrlList.add(imageNameUrlList.get(i));
            this.imagesPath.add(null);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.image_item, null);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
        String url = this.imageNameUrlList.get(position);
        final GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", this.token)
                .build());
        Glide.with(context)
                .applyDefaultRequestOptions(requestOptions)
                .load(glideUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (ImagesOnlyReadAdapter.this.imagesPath.get(holder.getLayoutPosition()) == null) {
                            FutureTask<String> task = new FutureTask<>(() ->
                                    HttpUtil.getImgCachePath(context, glideUrl));
                            //提交任务
                            HttpUtil.threadPool.submit(task);
                            try {
                                String imagePath = task.get(10, TimeUnit.SECONDS);
                                ImagesOnlyReadAdapter.this.imagesPath.set(holder.getLayoutPosition(), imagePath);
                            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                                ImagesOnlyReadAdapter.this.imagesPath.set(holder.getLayoutPosition(), null);
                            }
                        }
                        return false;
                    }
                })
                .into(holder.imageBtn);
        //因为是只读，因此只显示图片（imageButton）,只绑定单击事件
        holder.imageDel.setVisibility(View.GONE);
        holder.imageBtn.setOnClickListener(view -> {
            imageAdapterItemListener.onClickToShow(holder.getLayoutPosition());
        });

    }

    @Override
    public int getItemCount() {
        return this.imageNameUrlList.size();
    }
}