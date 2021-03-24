package com.example.ul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.callback.CallbackToRApplicationManageFragment;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/11 15:05
 * @Modified By:
 */
public class ApplicationListAdapter extends RecyclerView.Adapter<ApplicationListAdapter.ViewHolder>{
    private final String TAG = "BorrowListAdapter";
    private Context context;
    //定义需要包装的JSONArray对象
    private JSONArray jsonArray;
    //定义列表项显示JSONObject对象的哪些属性
    private String id;
    private String name;
    private String readerId;
    private String readerName;
    private String applicationDescription;
    private String applicationMoney;
    //列表项单击事件回调接口
    private CallbackToRApplicationManageFragment callbackToRApplicationManageFragment;

    public ApplicationListAdapter(Context context,JSONArray jsonArray,String id , String name,String readerId,String readerName,
                             String applicationDescription, String applicationMoney, CallbackToRApplicationManageFragment callbackToRApplicationManageFragment){
        /**
         * @Author:Wallace
         * @Description:
         * @Date:Created in 15:21 2021/3/11
         * @Modified By:
          * @param context
         * @param jsonArray
         * @param id
         * @param name
         * @param readerId
         * @param readerName
         * @param applicationDescription    详情描述
         * @param applicationMoney          需支付款（元）
         * @param listItemClickedCallbackParent
         * @return:
         */
        this.context = context;
        this.jsonArray = jsonArray;
        this.id = id;
        this.name = name;
        this.readerId = readerId;
        this.readerName = readerName;
        this.applicationDescription = applicationDescription;
        this.applicationMoney = applicationMoney;
        this.callbackToRApplicationManageFragment = callbackToRApplicationManageFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.application,viewGroup,false);
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
            String itemApplicationDescription = jsonArray.optJSONObject(position).getString(applicationDescription);
            String itemApplicationMoney = jsonArray.optJSONObject(position).getString(applicationMoney);
            //给列表项组件赋值
            holder.id.setText(itemId);
            holder.name.setText(itemName);
            holder.readerId.setText(itemReaderId);
            holder.readerName.setText(itemReaderName);
            holder.applicationDescription.setText(applicationDescription);
            holder.applicationMoney.setText(applicationMoney);
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
        private TextView applicationDescription;
        private TextView applicationMoney;
        //点击支付
        private TextView applicationPay;
        //更多详情
        private TextView applicationDesc;
        public ViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.bookId);
            name = itemView.findViewById(R.id.bookName);
            readerId = itemView.findViewById(R.id.readerId);
            readerName = itemView.findViewById(R.id.readerName);
            applicationDescription = itemView.findViewById(R.id.applicationDescription);
            applicationMoney = itemView.findViewById(R.id.applicationMoney);
            //“更多详情”按钮
            applicationDesc = itemView.findViewById(R.id.applicationDesc);
            //“点击支付”按钮
            applicationPay = itemView.findViewById(R.id.applicationPay);
            //绑定单击事件监听器
            if(callbackToRApplicationManageFragment!=null){
                applicationDescription.setOnClickListener(view -> callbackToRApplicationManageFragment.applicationToWantMore(this.getLayoutPosition()));
                applicationPay.setOnClickListener(view -> callbackToRApplicationManageFragment.applicationListToPay(this.getLayoutPosition()));
            }
        }
    }
}

