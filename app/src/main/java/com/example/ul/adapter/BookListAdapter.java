package com.example.ul.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.example.ul.R;
import com.example.ul.callback.CallbackToBookFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * @Author: Wallace
 * @Description:
 * @Date: 2021/3/10 11:21
 * @Modified By:
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.ViewHolder> {
    private final String TAG = "BookListAdapter";
    private Context context;
    /**
     * 访问服务器需携带的token
     */
    private String token;
    private String baseUrl;
    /**定义需要包装的JSONArray对象*/
    private final JSONArray jsonArray;
    private final String id;
    private final String name;
    private final String author;
    private final String description;
    private final String hot;
    private final String state;
    private final String theme;
    private final String isbn;
    private final String library;
    private final String images;
    // 列表项单击事件回调接口
    private final CallbackToBookFragment callbackToBookFragment;
    protected final RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder1).centerCrop().error(R.drawable.error1);
    String state1 = "在馆";
    String state2 = "预约";
    String state3 = "挂失";

    public BookListAdapter(Context context, String baseUrl,  String token, JSONArray jsonArray,String id,String name,String author,String description,String hot,
                             String state, String theme, String isbn, String library, String images,
                           CallbackToBookFragment callbackToBookFragment){
        this.context = context;
        this.token = token;
        this.baseUrl = baseUrl;
        this.jsonArray = jsonArray;
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.hot = hot;
        this.state = state;
        this.theme = theme;
        this.isbn = isbn;
        this.library = library;
        this.images = images;
        this.callbackToBookFragment = callbackToBookFragment;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book,viewGroup,false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //获取JSONArray数组元素的id,name,age,department,classroom属性
        try {
            // 从数据源中取值
            String itemId = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(id);
            String itemName = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(name);
            String itemAuthor = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(author);
            String itemDescription = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(description);
            String itemHot = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(hot);
            String itemState = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(state);
            String itemTheme = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(theme);
            String itemIsbn = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(isbn);
            String itemLibrary = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(library);
            String itemImages = jsonArray.optJSONObject(holder.getLayoutPosition()).getString(images);
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
            holder.isbn.setText(itemIsbn);
            holder.library.setText(itemLibrary);
            // 获取图片
            JSONArray jsonArray1 = jsonArray.optJSONObject(holder.getLayoutPosition()).getJSONArray("pictures");
            String url = baseUrl + itemImages;
            if (jsonArray1.length() > 0) {
                String pictureName = jsonArray1.getString(0);
                url = url + "/" + pictureName;
                Log.e(TAG, "onBindViewHolder: holder.getLayoutPosition() = "+ holder.getLayoutPosition());
                Log.e(TAG, "onBindViewHolder: url = "+ url);
            }
            GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                    .addHeader("Authorization", this.token)
                    .build());
            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(glideUrl)
                    .into(holder.bookImage);
        } catch (JSONException e) {
            Log.e(TAG, "onBindViewHolder: 列表"+holder.getLayoutPosition()+"项绑定数据时异常");
        }
    }

    @Override
    public int getItemCount() {
        if(jsonArray==null){
            return 0;
        }
        return jsonArray.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView id;
        private TextView name;
        private TextView author;
        private TextView description;
        private TextView hot;
        private TextView state;
        private TextView theme;
        private TextView isbn;
        private TextView library;
        private ImageView bookImage;

        public ViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.bookId);
            name = itemView.findViewById(R.id.bookName);
            author = itemView.findViewById(R.id.bookAuthor);
            description = itemView.findViewById(R.id.bookDescription);
            hot = itemView.findViewById(R.id.bookHot);
            state = itemView.findViewById(R.id.bookState);
            theme = itemView.findViewById(R.id.bookTheme);
            isbn = itemView.findViewById(R.id.bookIsbn);
            library = itemView.findViewById(R.id.bookLibrary);
            bookImage = itemView.findViewById(R.id.imageView);
            //readerListClickedCallback，则为列表项绑定单击事件监听器
            if(callbackToBookFragment!=null){
                itemView.findViewById(R.id.boot_root).setOnClickListener(view -> callbackToBookFragment.bookListClickPosition(id.getText().toString().trim()));
            }
        }
    }
}
