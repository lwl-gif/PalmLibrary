package com.example.ul.adapter;


import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import android.view.View;

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
import com.bumptech.glide.request.target.Target;
import com.example.ul.R;
import com.example.ul.callback.CallbackToBorrowBookActivity;
import com.example.ul.callback.ImageAdapterItemListener;
import com.example.ul.util.HttpUtil;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @Author: Wallace
 * @Description: 展示图片的列表适配器，可读可写（可看可修改）
 * @Date: 2021/4/13 21:10
 * @Modified: By yyyy-MM-dd
 */
public class ImagesAdapter extends ImagesOnlyReadAdapter {

    private static final String TAG = "ImagesAdapter";

    private Context context;
    /**
     * 选择的本地图片
     */
    private ArrayList<LocalMedia> selectList;
    /**
     * 本适配器拥有的所有图片的url+uri
     */
    private ArrayList<String> glideLoad;
    /**
     * 当前是否处于删除状态
     */
    private boolean deleting = false;
    /**
     * 当前是否第一次删除图片
     */
    private boolean firstDelete = true;

    public ImagesAdapter(Context context,String token) {
        super(context, token);
        init(context);
        this.context = context;
        this.token = token;
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
        glideLoad = in.createStringArrayList();
        deleting = in.readByte() != 0;
        firstDelete = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(selectList);
        dest.writeStringList(glideLoad);
        dest.writeByte((byte) (deleting ? 1 : 0));
        dest.writeByte((byte) (firstDelete ? 1 : 0));
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
        String path = glideLoad.get(glideLoad.size() - 1);
        glideLoad.clear();
        imagesPath.clear();
        for (int i = 0; i < this.imageNameUrlList.size(); i++) {
            String url = this.imageNameUrlList.get(i);
            glideLoad.add(url);
            imagesPath.add(null);
        }
        for (int i = 0; i < this.selectList.size(); i++) {
            LocalMedia localMedia = this.selectList.get(i);
            glideLoad.add(localMedia.getPath());
            imagesPath.add(localMedia.getPath());
        }
        glideLoad.add(path);
        imagesPath.add(path);
        notifyDataSetChanged();
    }

    @Override
    public ArrayList<String> getImagesPath() {
        ArrayList<String> arrayList = new ArrayList<>();
        for(int i = 0; i < imagesPath.size() - 1; i++){
            arrayList.add(imagesPath.get(i));
        }
        return arrayList;
    }

    public boolean getDeleting(){
        return  this.deleting;
    }

    public void setDeleting(boolean deleting) {
        this.deleting = deleting;
        if(!this.deleting){
            setFirstDelete(true);
        }
        notifyItemRangeChanged(0, glideLoad.size());
    }

    public boolean isFirstDelete() {
        return firstDelete;
    }

    public void setFirstDelete(boolean firstDelete) {
        this.firstDelete = firstDelete;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
        String url = glideLoad.get(holder.getLayoutPosition());
        RequestManager requestManager = Glide.with(context).applyDefaultRequestOptions(requestOptions);
        RequestBuilder<Drawable> requestBuilder;
        // 加载网络图片
        if(position < this.imageNameUrlList.size()) {
            String p = this.imagesPath.get(position);
            if(p == null){
                final GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
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
                        if (ImagesAdapter.this.imagesPath.get(holder.getLayoutPosition()) == null) {
                            FutureTask<String> task = new FutureTask<>(() ->
                                    HttpUtil.getImgCachePath(context, glideUrl));
                            // 提交任务
                            HttpUtil.threadPool.submit(task);

                            try {
                                String imagePath = task.get(2, TimeUnit.SECONDS);
                                ImagesAdapter.this.imagesPath.set(holder.getLayoutPosition(), imagePath);
                            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                                e.printStackTrace();
                                ImagesAdapter.this.imagesPath.set(holder.getLayoutPosition(), null);
                            }
                        }
                        return false;
                    }
                })
                        .into(holder.imageBtn);
            }else {
                requestBuilder = requestManager.load(p);
                requestBuilder.into(holder.imageBtn);
            }
        }
        // 加载本地图片
        else {
            requestBuilder = requestManager.load(url);
            requestBuilder.into(holder.imageBtn);
        }
        holder.imageDel.setOnClickListener(view -> imageAdapterItemListener.onClickToDelete(holder.getLayoutPosition()));
        holder.imageBtn.setOnClickListener(view -> imageAdapterItemListener.onClickToShow(holder.getLayoutPosition()));
        // 如果不是最后一个item
        if (holder.getLayoutPosition() < getItemCount() - 1) {
            holder.imageBtn.setOnLongClickListener(view -> {
                ImagesAdapter.this.setDeleting(!ImagesAdapter.this.deleting);
                return true;
            });
            // 当前处于删除图片状态，显示删除的图标
            if (ImagesAdapter.this.deleting) {
                holder.imageDel.setVisibility(View.VISIBLE);
            } else {
                holder.imageDel.setVisibility(View.GONE);
            }
        }
        // 如果是最后一个item，删除图标永不显示
        else {
            holder.imageDel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.glideLoad.size();
    }

    public void removeItem(int position) {
        // 删除的是网络图片
        Log.e(TAG, "removeItem: position = "+position);
        if (position < this.imageNameUrlList.size()) {
            this.imageNameUrlList.remove(position);
        }
        // 删除的是本地图片
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
}