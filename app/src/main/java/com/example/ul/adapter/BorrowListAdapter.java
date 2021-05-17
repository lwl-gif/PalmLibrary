package com.example.ul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.example.ul.R;
import com.example.ul.callback.CallbackToBorrowBookActivity;
import com.example.ul.callback.CallbackToRBorrowFragment;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Author:Wallace
 * @Description:
 * @Date: 2021/3/10 17:55
 * @Modified By:
 */
public class BorrowListAdapter extends RecyclerView.Adapter<BorrowListAdapter.ViewHolder>{

    private final String TAG = "BorrowListAdapter";

    private Context context;
    /**定义需要包装的JSONArray对象*/
    private JSONArray jsonArray;
    /**
     * 定义列表项显示JSONObject对象的哪些属性*
     * 书本号
     * 名称
     * 读者Id
     * 读者名字
     * 借阅状态
     * 借阅时间
     * 预约
     * 有效时间至
     * 还书时间
     * 当前适配的数据类型
     */
    private String id;
    private String name;
    private String readerId;
    private String readerName;
    private String state;
    private String start;
    private String end;
    private String box;
    /**列表项单击事件回调接口*/
    private CallbackToRBorrowFragment callbackToRBorrowFragment;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public BorrowListAdapter(Context context, JSONArray jsonArray, String id, String name, String readerId,
                             String readerName, String state, String start, String end, String box,
                             CallbackToRBorrowFragment callbackToRBorrowFragment){
        this.context = context;
        this.jsonArray = jsonArray;
        this.id = id;
        this.name = name;
        this.readerId = readerId;
        this.readerName = readerName;
        this.state = state;
        this.start = start;
        this.end = end;
        this.box = box;
        this.callbackToRBorrowFragment = callbackToRBorrowFragment;
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        //适配当前借阅
        if("box1".equals(box)){
            view = LayoutInflater.from(context).inflate(R.layout.borrow,viewGroup,false);

        }
        //适配当前预约
        else if("box2".equals(box)){
            view = LayoutInflater.from(context).inflate(R.layout.reservation,viewGroup,false);
        }
        //适配过期记录
        else {
            view = null;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //从数据源中取值
        String itemId = jsonArray.getJSONObject(position).getString(id);
        String itemName = jsonArray.getJSONObject(position).getString(name);
        String itemReaderId = jsonArray.getJSONObject(position).getString(readerId);
        String itemReaderName = jsonArray.getJSONObject(position).getString(readerName);
        String itemState = jsonArray.getJSONObject(position).getString(state);
        Long Start = jsonArray.getJSONObject(position).getLong(start);
        Long End = jsonArray.getJSONObject(position).getLong(end);
        Date date = new Date(Start);
        String itemStart = format.format(date);
        Date date0 = new Date(End);
        String itemEnd = format.format(date0);
        // 给列表项组件赋值
        holder.id.setText(itemId);
        holder.name.setText(itemName);
        holder.readerId.setText(itemReaderId);
        holder.readerName.setText(itemReaderName);
        holder.state.setText(itemState);
        holder.start.setText(itemStart);
        holder.end.setText(itemEnd);
        // 绑定单击事件监听器
        if(callbackToRBorrowFragment != null){
            holder.tv_desc.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToWantMore(holder.getLayoutPosition()));
            if("box1".equals(box)){
                TextView tvLent = (TextView) holder.rootView.findViewById(R.id.tv_lent);
                tvLent.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToLent(holder.getLayoutPosition()));
                TextView tvRenew = (TextView) holder.rootView.findViewById(R.id.tv_renew);
                tvRenew.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToRenew(holder.getLayoutPosition()));
                TextView tvLoss = (TextView) holder.rootView.findViewById(R.id.tv_loss);
                tvLoss.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToLoss(holder.getLayoutPosition()));
            }else if("box2".equals(box)){
                TextView tvAbandon = (TextView) holder.rootView.findViewById(R.id.tv_abandon);
                tvAbandon.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToAbandon(holder.getLayoutPosition()));
            }else {

            }
        }
    }

    @Override
    public int getItemCount() {
        return jsonArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        //三个视图中共同的属性
        private View rootView;
        private TextView id;
        private TextView name;
        private TextView readerId;
        private TextView readerName;
        private TextView state;
        private TextView start;
        private TextView end;
        private TextView tv_desc;
        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            id = itemView.findViewById(R.id.bookId);
            name = itemView.findViewById(R.id.bookName);
            readerId = itemView.findViewById(R.id.readerId);
            readerName = itemView.findViewById(R.id.readerName);
            state = itemView.findViewById(R.id.State);
            start = itemView.findViewById(R.id.bookStart);
            end = itemView.findViewById(R.id.bookEnd);
            // “更多详情”按钮
            tv_desc = itemView.findViewById(R.id.tv_desc);
        }
    }
}