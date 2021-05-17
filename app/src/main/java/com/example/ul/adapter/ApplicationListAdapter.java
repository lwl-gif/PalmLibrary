package com.example.ul.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.callback.CallbackToApplicationFragment;
import com.example.ul.callback.CallbackToApplicationFragment;
import com.example.ul.callback.CallbackToBorrowBookActivity;
import com.example.ul.model.Application;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * @Author:Wallace
 * @Description:
 * @Date: 2021/3/11 15:05
 * @Modified By:
 */
public class ApplicationListAdapter extends RecyclerView.Adapter<ApplicationListAdapter.ViewHolder>{

    private final String TAG = "ApplicationListAdapter";

    private final Context context;

    /**数据源*/
    private ArrayList<Application> applications;
    /**列表项单击事件回调接口*/
    private final CallbackToApplicationFragment callbackToApplicationFragment;

    public ApplicationListAdapter(Context context, ArrayList<Application> applications,
                                  CallbackToApplicationFragment callbackToApplicationFragment){
        Log.d(TAG, "ApplicationListAdapter: TAG = " + TAG);
        this.context = context;
        this.applications = applications;
        this.callbackToApplicationFragment = callbackToApplicationFragment;
    }

    public ArrayList<Application> getApplications(){
        return this.applications;
    }
    
    public void setApplications(ArrayList<Application> applications) {
        this.applications = applications;
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.application,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int i = holder.getLayoutPosition();
        // 从数据源中取值
        String payId = applications.get(i).getPayId();
        int itemId = applications.get(i).getId();
        String itemName = applications.get(i).getName();
        String itemReaderId = applications.get(i).getReaderId();
        String itemReaderName = applications.get(i).getReaderName();
        String itemDescription = applications.get(i).getDescription();
        Date itemCreateTime = applications.get(i).getTime();
        BigDecimal itemMoney = applications.get(i).getMoney();
        Date itemPayTime = applications.get(i).getPayTime();
        // 给列表项组件赋值
        String idString = String.valueOf(itemId);
        holder.payId.setText(payId);
        holder.id.setText(idString);
        holder.name.setText(itemName);
        holder.readerId.setText(itemReaderId);
        holder.readerName.setText(itemReaderName);
        holder.description.setText(itemDescription);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String createTimeString = itemCreateTime == null ? null : format.format(itemCreateTime);
        holder.createTime.setText(createTimeString);
        String moneyString = itemMoney == null ? null : itemMoney.toString();
        holder.money.setText(moneyString);
        String itemPayTimeString = itemPayTime == null ? null : format.format(itemPayTime);
        holder.payTime.setText(itemPayTimeString);
        // 列表项绑定单击事件
        if(callbackToApplicationFragment!=null){
           holder.rootView.setOnClickListener(v -> {
               callbackToApplicationFragment.clickToGetApplicationDetail(i);
           });
        }
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private View rootView;
        private TextView payId;
        private TextView id;
        private TextView name;
        private TextView readerId;
        private TextView readerName;
        private TextView description;
        private TextView createTime;
        private TextView money;
        private TextView payTime;
        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            payId = itemView.findViewById(R.id.payId);
            id = itemView.findViewById(R.id.bookId);
            name = itemView.findViewById(R.id.bookName);
            readerId = itemView.findViewById(R.id.readerId);
            readerName = itemView.findViewById(R.id.readerName);
            description = itemView.findViewById(R.id.applicationDescription);
            createTime = itemView.findViewById(R.id.createTime);
            money = itemView.findViewById(R.id.applicationMoney);
            payTime = itemView.findViewById(R.id.payTime);
        }
    }
}