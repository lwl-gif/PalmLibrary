package com.example.ul.adapter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
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
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @Author: Wallace
 * @Description: 展示图片的列表适配器，可读可写（可看可修改）
 * @Date: 2021/4/13 21:10
 * @Modified: By yyyy-MM-dd
 */

public class ImagesAdapter extends ImagesOnlyReadAdapter implements Parcelable {

    private static final String TAG = "ImagesAdapter";
    protected Context context;
    /**
     * 选择的本地图片
     */
    protected ArrayList<LocalMedia> selectList;
    /**
     * 本适配器展示的所有图片的本地存储路径
     */
    protected ArrayList<String> imagesPath;
    /**
     * 当前是否处于删除状态
     */
    protected boolean deleting = false;
    /**
     * 当前是否第一次删除图片
     */
    protected boolean firstDelete = true;
    /**
     * 本适配器拥有的所有图片的url+uri
     */
    protected ArrayList<String> glideLoad;

    public ImagesAdapter(Context context,String token, ImageAdapterItemListener imageAdapterItemListener) {
        super(context,token,imageAdapterItemListener);
        this.context = context;
        this.imageAdapterItemListener = imageAdapterItemListener;
        Resources resources = context.getResources();
        // "添加图片"按钮的文件路径
        String path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(R.drawable.add_image) + "/"
                + resources.getResourceTypeName(R.drawable.add_image) + "/"
                + resources.getResourceEntryName(R.drawable.add_image);
        this.imageNameUrlList = new ArrayList<>();
        this.selectList = new ArrayList<>();
        this.glideLoad  = new ArrayList<>();
        this.imagesPath  = new ArrayList<>();
        for (int i = 0; i < this.imageNameUrlList.size(); i++) {
            String url = this.imageNameUrlList.get(i);
            this.glideLoad.add(url);
            this.imagesPath.add(null);
        }
        for (int i = 0; i < this.selectList.size(); i++) {
            LocalMedia localMedia = this.selectList.get(i);
            this.glideLoad.add(localMedia.getPath());
            this.imagesPath.add(localMedia.getPath());
        }
        this.glideLoad.add(path);
        this.imagesPath.add(path);
    }

    protected ImagesAdapter(Parcel in) {
        super(in);
        selectList = in.createTypedArrayList(LocalMedia.CREATOR);
        imageNameUrlList = in.createStringArrayList();
        glideLoad = in.createStringArrayList();
        imagesPath = in.createStringArrayList();
        deleting = in.readByte() != 0;
        firstDelete = in.readByte() != 0;
    }

    public static final Creator<ImagesAdapter> CREATOR = new Creator<ImagesAdapter>() {
        @Override
        public ImagesAdapter createFromParcel(Parcel in) {
            return new ImagesAdapter(in);
        }

        @Override
        public ImagesAdapter[] newArray(int size) {
            return new ImagesAdapter[size];
        }
    };

    public void setSelectList(ArrayList<LocalMedia> selectList) {
        setDeleting(false);
        int index = this.imageNameUrlList.size() + this.selectList.size();
        for (int i = 0; i < selectList.size(); i++) {
            LocalMedia localMedia = selectList.get(i);
            this.selectList.add(localMedia);
            this.glideLoad.add(index, localMedia.getPath());
            this.imagesPath.add(index, localMedia.getPath());
            notifyItemInserted(index);
            index++;
        }
        notifyItemChanged(index);
    }

    @Override
    public void setImageNameUrlList(ArrayList<String> imageNameUrlList) {
        this.imageNameUrlList = imageNameUrlList;
        String path = this.glideLoad.get(this.glideLoad.size() - 1);
        this.glideLoad.clear();
        this.imagesPath.clear();
        for (int i = 0; i < this.imageNameUrlList.size(); i++) {
            String url = this.imageNameUrlList.get(i);
            this.glideLoad.add(url);
            this.imagesPath.add(null);
        }
        for (int i = 0; i < this.selectList.size(); i++) {
            LocalMedia localMedia = this.selectList.get(i);
            this.glideLoad.add(localMedia.getPath());
            this.imagesPath.add(localMedia.getPath());
        }
        this.glideLoad.add(path);
        this.imagesPath.add(path);
        notifyDataSetChanged();
    }

    public boolean getDeleting(){
        return  this.deleting;
    }

    public void setDeleting(boolean deleting) {
        this.deleting = deleting;
        if(!this.deleting){
            setFirstDelete(true);
        }
        notifyItemRangeChanged(0, ImagesAdapter.this.glideLoad.size());
    }

    public boolean isFirstDelete() {
        return firstDelete;
    }

    public void setFirstDelete(boolean firstDelete) {
        this.firstDelete = firstDelete;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.image_tiem, null);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
        Log.e(TAG, "onBindViewHolder: position = "+position);
        String url = glideLoad.get(position);
        final GlideUrl glideUrl;
        RequestManager requestManager = Glide.with(context).applyDefaultRequestOptions(requestOptions);
        RequestBuilder<Drawable> requestBuilder;
        // 加载网络图片
        if(position < this.imageNameUrlList.size()) {
            glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                    .addHeader("Authorization", this.token)
                    .build());
            requestBuilder = requestManager.load(glideUrl);
            requestBuilder.listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    Log.e(TAG, "onResourceReady: getLayoutPosition = " + holder.getLayoutPosition());
                    if (ImagesAdapter.this.imagesPath.get(holder.getLayoutPosition()) == null) {
                        FutureTask<String> task = new FutureTask<>(() ->
                                HttpUtil.getImgCachePath(context, glideUrl));
                        //提交任务
                        HttpUtil.threadPool.submit(task);
                        try {
                            String imagePath = task.get(10, TimeUnit.SECONDS);
                            ImagesAdapter.this.imagesPath.set(holder.getLayoutPosition(), imagePath);
                        } catch (ExecutionException | InterruptedException | TimeoutException e) {
                            ImagesAdapter.this.imagesPath.set(holder.getLayoutPosition(), null);
                        }
                    }
                    return false;
                }
            })
                    .into(holder.imageButton);
        }
        // 加载本地图片
        else {
            requestBuilder = requestManager.load(url);
            requestBuilder.into(holder.imageButton);
        }
        holder.imageDelete.setOnClickListener(view -> {
            imageAdapterItemListener.onClickToDelete(holder.getLayoutPosition());
        });
        holder.imageButton.setOnClickListener(view -> {
            imageAdapterItemListener.onClickToShow(holder.getLayoutPosition());
        });
        //如果不是最后一个item
        if (holder.getLayoutPosition() < ImagesAdapter.this.getItemCount() - 1) {
            holder.imageButton.setOnLongClickListener(view -> {
                if (ImagesAdapter.this.deleting) {
                    ImagesAdapter.this.setDeleting(false);
                } else {
                    ImagesAdapter.this.setDeleting(true);
                }
                return true;
            });
            //当前处于删除图片状态，显示删除的图标
            if (ImagesAdapter.this.deleting) {
                holder.imageDelete.setVisibility(View.VISIBLE);
            } else {
                holder.imageDelete.setVisibility(View.GONE);
            }
        }
        //如果是最后一个item，删除图标永不显示
        else {
            holder.imageDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.glideLoad.size();
    }

    public void removeItem(int position) {
        //删除的是网络图片
        Log.e(TAG, "removeItem: position = "+position);
        if (position < this.imageNameUrlList.size()) {
            this.imageNameUrlList.remove(position);
        }
        //删除的是本地图片
        else {
            this.selectList.remove(position - this.imageNameUrlList.size());
        }
        this.glideLoad.remove(position);
        this.imagesPath.remove(position);
        notifyDataSetChanged();
        if(glideLoad.size() == 1){
            setDeleting(false);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(selectList);
        parcel.writeStringList(imageNameUrlList);
        parcel.writeStringList(glideLoad);
        parcel.writeStringList(imagesPath);
        parcel.writeByte((byte) (deleting ? 1 : 0));
        parcel.writeByte((byte) (firstDelete ? 1 : 0));
    }
}