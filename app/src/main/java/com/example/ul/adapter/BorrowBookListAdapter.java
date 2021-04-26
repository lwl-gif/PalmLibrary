package com.example.ul.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.example.ul.R;
import com.example.ul.callback.CallbackToBorrowBookActivity;
import com.example.ul.model.Book;
import com.example.ul.view.ItemSlideHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @Author: Wallace
 * @Description: 借书活动中更多适配器
 * @Date: 2021/4/26 22:28
 * @Modified: By yyyy-MM-dd
 */
public class BorrowBookListAdapter extends RecyclerView.Adapter<BookViewHolder> implements ItemSlideHelper.Callback{

    private final String TAG = "BorrowBookListAdapter";
    /**适配器所在的列表*/
    private RecyclerView mRecyclerView;
    private Context context;
    /**
     * 访问服务器需携带的token
     */
    private String token;
    private String baseUrl;
    /**数据集*/
    ArrayList<Book> bookArrayList;
    /**回调接口*/
    private CallbackToBorrowBookActivity callbackToBorrowBookActivity;
    protected final RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder1).centerCrop().error(R.drawable.error1);
    
    public BorrowBookListAdapter(Context context,String token, String baseUrl, ArrayList<Book> bookArrayList){
        this.context = context;
        this.token = token;
        this.baseUrl = baseUrl;
        this.bookArrayList = bookArrayList;
        init(context);
    }

    private void init(Context context){
        //如果Context没有实现callback,ListClickedCallback接口，则抛出异常
        if (!(context instanceof CallbackToBorrowBookActivity)) {
            throw new IllegalStateException("BorrowBookListAdapter所在的Context必须实现CallbackToBorrowBookActivity接口");
        }
        //把该Context当初CallbackToBorrowBookActivity对象
        this.callbackToBorrowBookActivity = (CallbackToBorrowBookActivity)context;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book,parent,false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        // 从数据源中取值
        String itemId = String.valueOf(bookArrayList.get(holder.getLayoutPosition()).getId());
        String itemName = bookArrayList.get(holder.getLayoutPosition()).getName();
        String itemAuthor = bookArrayList.get(holder.getLayoutPosition()).getAuthor();
        String itemDescription = bookArrayList.get(holder.getLayoutPosition()).getDescription();
        String itemHot = String.valueOf(bookArrayList.get(holder.getLayoutPosition()).getHot());
        String itemState = bookArrayList.get(holder.getLayoutPosition()).getState();
        String itemTheme = bookArrayList.get(holder.getLayoutPosition()).getTheme();
        String itemIsbn = bookArrayList.get(holder.getLayoutPosition()).getIsbn();
        String itemLibrary = bookArrayList.get(holder.getLayoutPosition()).getLibrary();
        String itemImages = bookArrayList.get(holder.getLayoutPosition()).getImages();
        // 给列表项组件赋值
        holder.id.setText(itemId);
        holder.name.setText(itemName);
        holder.author.setText(itemAuthor);
        holder.description.setText(itemDescription);
        holder.hot.setText(itemHot);
        holder.state.setText(itemState);
        // 不同状态配不同的颜色
        holder.state.setTextColor(Color.GREEN);
        holder.theme.setText(itemTheme);
        holder.isbn.setText(itemIsbn);
        holder.library.setText(itemLibrary);
        // 获取图片
        ArrayList<String> pictures = bookArrayList.get(holder.getLayoutPosition()).getPictures();
        String url = baseUrl + itemImages;
        if (pictures.size() > 0) {
            String pictureName = pictures.get(0);
            url = url + "/" + pictureName;
            Log.e(TAG, "onBindViewHolder: holder.getLayoutPosition() = " + holder.getLayoutPosition());
            Log.e(TAG, "onBindViewHolder: url = " + url);
        }
        GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", this.token)
                .build());
        Glide.with(context)
                .applyDefaultRequestOptions(requestOptions)
                .load(glideUrl)
                .into(holder.bookImage);
        // 布局左半部分绑定单击方法
        holder.bookRootLeft.setOnClickListener(v -> {
            callbackToBorrowBookActivity.onClickToShowDetail(holder.getLayoutPosition());
        });
        // 右半部分布局绑定删除方法
        holder.bookRootRight.setOnClickListener(v -> {
            callbackToBorrowBookActivity.onClickToDeleteBook(holder.getLayoutPosition());
        });
    }

    @Override
    public int getItemCount() {
        return this.bookArrayList.size();
    }

    @Override
    public int getHorizontalRange(RecyclerView.ViewHolder holder) {
        if(holder.itemView instanceof LinearLayout){
            ViewGroup viewGroup = (ViewGroup) holder.itemView;
            if(viewGroup.getChildCount() == 2){
                return viewGroup.getChildAt(1).getLayoutParams().width;
            }
        }
        return 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        mRecyclerView.addOnItemTouchListener(new ItemSlideHelper(mRecyclerView.getContext(), this));
    }

    @Override
    public RecyclerView.ViewHolder getChildViewHolder(View childView) {
        return mRecyclerView.getChildViewHolder(childView);
    }

    @Override
    public View findTargetView(float x, float y) {
        return mRecyclerView.findChildViewUnder(x, y);
    }

    @Override
    public void updateItem() {
        Log.e(TAG,"update!");
        this.notifyDataSetChanged();
    }
}
