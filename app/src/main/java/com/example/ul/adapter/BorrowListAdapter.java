package com.example.ul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ul.R;
import com.example.ul.callback.CallbackToRBorrowFragment;
import org.json.JSONArray;
import org.json.JSONException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/10 17:55
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
    //列表项单击事件回调接口
    private CallbackToRBorrowFragment callbackToRBorrowFragment;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public BorrowListAdapter(Context context,JSONArray jsonArray,String id , String name,String readerId,String readerName,String state,
                           String start, String end, String box, CallbackToRBorrowFragment callbackToRBorrowFragment){
        /**
         * @Author:Wallace
         * @Description:
         * @Date:Created in 21:55 2021/3/10
         * @Modified By:
          * @param context
         * @param jsonArray
         * @param id
         * @param name
         * @param readerId
         * @param readerName
         * @param state
         * @param start
         * @param end
         * @param box 通过box来确定适配哪类数据（当前借阅、当前预约和过期记录）
         * @param callbackToRBorrowFragment
         * @return:
         */
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
        //获取JSONArray数组元素的id,name,age,department,classroom属性
        try {
            //从数据源中取值
            String itemId = jsonArray.optJSONObject(position).getString(id);
            String itemName = jsonArray.optJSONObject(position).getString(name);
            String itemReaderId = jsonArray.optJSONObject(position).getString(readerId);
            String itemReaderName = jsonArray.optJSONObject(position).getString(readerName);
            String itemState = jsonArray.optJSONObject(position).getString(state);
            Long Start = jsonArray.optJSONObject(position).getLong(start);
            Long End = jsonArray.optJSONObject(position).getLong(end);
            Date date = new Date(Start);

            String itemStart = format.format(date);
            Date date0 = new Date(End);
            String itemEnd = format.format(date0);
            //给列表项组件赋值
            holder.id.setText(itemId);
            holder.name.setText(itemName);
            holder.readerId.setText(itemReaderId);
            holder.readerName.setText(itemReaderName);
            holder.state.setText(itemState);
            holder.start.setText(itemStart);
            holder.end.setText(itemEnd);
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
        //三个视图中共同的属性
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
            id = itemView.findViewById(R.id.bookId);
            name = itemView.findViewById(R.id.bookName);
            readerId = itemView.findViewById(R.id.readerId);
            readerName = itemView.findViewById(R.id.readerName);
            state = itemView.findViewById(R.id.State);
            start = itemView.findViewById(R.id.bookStart);
            end = itemView.findViewById(R.id.bookEnd);
            //“更多详情”按钮
            tv_desc = itemView.findViewById(R.id.tv_desc);
            //绑定单击事件监听器
            if(callbackToRBorrowFragment!=null){
                tv_desc.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToWantMore(this.getLayoutPosition()));
                if(box.equals("box1")){
                    TextView tv_lent = (TextView) itemView.findViewById(R.id.tv_lent);
                    tv_lent.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToLent(this.getLayoutPosition()));
                    TextView tv_renew = (TextView) itemView.findViewById(R.id.tv_renew);
                    tv_renew.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToRenew(this.getLayoutPosition()));
                    TextView tv_loss = (TextView) itemView.findViewById(R.id.tv_loss);
                    tv_loss.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToLoss(this.getLayoutPosition()));
                }else if(box.equals("box2")){
                    TextView tv_abandon = (TextView) itemView.findViewById(R.id.tv_abandon);
                    tv_abandon.setOnClickListener(view -> callbackToRBorrowFragment.borrowListToAbandon(this.getLayoutPosition()));
                }else {

                }
            }
        }
    }
}