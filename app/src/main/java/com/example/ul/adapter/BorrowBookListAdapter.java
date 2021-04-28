package com.example.ul.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.example.ul.R;
import com.example.ul.callback.CallbackToBorrowBookActivity;
import com.example.ul.model.Book;
import com.example.ul.util.DialogUtil;
import com.example.ul.view.ItemSlideHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @Author: Wallace
 * @Description: 借书活动中的适配器
 * @Date: 2021/4/26 22:28
 * @Modified: By yyyy-MM-dd
 */
public class BorrowBookListAdapter extends RecyclerView.Adapter<BookViewHolder> implements ItemSlideHelper.Callback{

    private final String TAG = "BorrowBookListAdapter";
    /**适配器所在的列表*/
    private RecyclerView mRecyclerView;
    /**上下文*/
    private final Context context;
    /**
     * 访问服务器需携带的token
     */
    private final String token;
    /**访问图片的基本url*/
    private final String baseUrl;
    /**最大可借阅量*/
    private int maxAmount;
    /**数据集*/
    private final ArrayList<Book> bookArrayList = new ArrayList<>();
    /**回调接口*/
    private CallbackToBorrowBookActivity callbackToBorrowBookActivity;

    protected final RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder1).centerCrop().error(R.drawable.error1);

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
        callbackToBorrowBookActivity.changeAmount(maxAmount);
    }

    public ArrayList<Book> getBookArrayList() {
        return bookArrayList;
    }

    public Book getBook(int position) {
        return bookArrayList.get(position);
    }

    public BorrowBookListAdapter(Context context,String token, String baseUrl){
        this.context = context;
        this.token = token;
        this.baseUrl = baseUrl;
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
        if (pictures != null && pictures.size() > 0) {
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
    /**
     * @Author: Wallace
     * @Description: 判断book是否已经存在于bookArrayList中
     * @Date: Created 11:22 2021/4/27
     * @Modified: by who yyyy-MM-dd
     * @param newBook 书籍
     * @return: int 存在返回所在的位置,不存在返回-1
     */
    private int isExisted(Book newBook){
        int bookId = newBook.getId();
        return isExisted(bookId);
    }

    private int isExisted(int bookId){
        int position = -1;
        for(Book book : bookArrayList){
            if(bookId == book.getId()){
                position = bookArrayList.indexOf(book);
                break;
            }
        }
        return position;
    }

    public void addItem(Book book){
        if(bookArrayList.size() < maxAmount){
            if(isExisted(book) == -1){
                bookArrayList.add(book);
                callbackToBorrowBookActivity.changeAmount(--maxAmount);
                notifyItemInserted(bookArrayList.size()-1);
            }else {
                Toast.makeText(context,"您已经添加过此书了！",Toast.LENGTH_LONG).show();
            }
        }else {
            DialogUtil.showDialog(context,"剩余量为0，您不能借再多的书了。",false);
        }
    }

    public void deleteItem(int position){
        bookArrayList.remove(position);
        callbackToBorrowBookActivity.changeAmount(++maxAmount);
        notifyItemRemoved(position);
    }

    public void deleteItemByBookId(int bookId){
        int position = isExisted(bookId);
        if(position != -1){
            deleteItem(position);
        }
    }

    public void updateItem(Book newBook){
        int position = isExisted(newBook);
        if(position == -1){
            throw new NullPointerException("找不到需要更新的列表项：position = " + position);
        }
        else {
            bookArrayList.set(position,newBook);
            notifyItemChanged(position);
        }
    }
    /**
     * @Author: Wallace
     * @Description: 方法描述
     * @Date: Created 23:19 2021/4/27
     * @Modified: by who yyyy-MM-dd
     * @param restBooks 剩余的图书的id
     * @return: void
     */
    public void updateItem(ArrayList<Integer> restBooks){
        ArrayList<Book> books = new ArrayList<>();
        for(int i : restBooks) {
            for(Book book : bookArrayList){
                if(book.getId().equals(i)){
                    books.add(book);
                    break;
                }
            }
        }
        // 更新数据集
        bookArrayList.clear();
        bookArrayList.addAll(books);
        notifyDataSetChanged();
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
/*        Log.e(TAG,"update!");
        this.notifyDataSetChanged();*/
    }
}
