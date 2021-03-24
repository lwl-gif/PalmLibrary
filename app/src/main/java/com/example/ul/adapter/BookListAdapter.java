package com.example.ul.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.callback.CallbackToBookFragment;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/10 11:21
 * @Modified By:
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.ViewHolder> {
    private final String TAG = "ReaderListAdapter";
    private Context context;
    //定义需要包装的JSONArray对象
    private JSONArray jsonArray;
    //定义列表项显示JSONObject对象的哪些属性
    private String id;                  //书本号
    private String name;                //书名
    private String author;              //作者
    private String description;         //书本详情
    private String hot;                 //热度
    private String state;               //书本状态
    private String theme;               //书本主题
    private String isbn;                //Isbn
    private String library;             //所属馆
    //列表项单击事件回调接口
    private CallbackToBookFragment callbackToBookFragment;

    public BookListAdapter(Context context,JSONArray jsonArray,String id,String name,String author,String description,String hot,
                             String state, String theme, String isbn, String library,
                           CallbackToBookFragment callbackToBookFragment){
        this.context = context;
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
        this.callbackToBookFragment = callbackToBookFragment;
    }

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
            //从数据源中取值
            String itemId = jsonArray.optJSONObject(position).getString(id);
            String itemName = jsonArray.optJSONObject(position).getString(name);
            String itemAuthor = jsonArray.optJSONObject(position).getString(author);
            String itemDescription = jsonArray.optJSONObject(position).getString(description);
            String itemHot = jsonArray.optJSONObject(position).getString(hot);
            String itemState = jsonArray.optJSONObject(position).getString(state);
            String itemTheme = jsonArray.optJSONObject(position).getString(theme);
            String itemIsbn = jsonArray.optJSONObject(position).getString(isbn);
            String itemLibrary = jsonArray.optJSONObject(position).getString(library);
            //给列表项组件赋值
            holder.id.setText(itemId);
            holder.name.setText(itemName);
            holder.author.setText(itemAuthor);
            holder.description.setText(itemDescription);
            holder.hot.setText(itemHot);
            holder.state.setText(itemState);
            //不同状态配不同的颜色
            if(itemState.equals("在馆")){
                holder.state.setTextColor(Color.GREEN);
            }else if(itemState.equals("预约")){
                holder.state.setTextColor(Color.BLUE);
            }else if(itemState.equals("挂失")){
                holder.state.setTextColor(Color.RED);
            }else {
                holder.state.setTextColor(Color.BLACK);
            }
            holder.theme.setText(itemTheme);
            holder.isbn.setText(itemIsbn);
            holder.library.setText(itemLibrary);
        } catch (JSONException e) {
            e.printStackTrace();
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

            //readerListClickedCallback，则为列表项绑定单击事件监听器
            if(callbackToBookFragment!=null){
                itemView.findViewById(R.id.boot_root).setOnClickListener(view -> callbackToBookFragment.bookListClickPosition(id.getText().toString().trim()));
            }
        }
    }
}
