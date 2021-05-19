package com.example.ul.adapter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ul.R;
import com.example.ul.callback.CallbackToBorrowBookActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @Author: Wallace
 * @Description: 支付方式列表
 * @Date: 2021/5/19 15:01
 * @Modified: By yyyy-MM-dd
 */
public class PayWayAdapter extends RecyclerView.Adapter<PayWayAdapter.ViewHolder>{

    private final String TAG = "PayWayAdapter";
    private final Context context;
    /**logo图标文件路径*/
    private final ArrayList<String> logoPaths = new ArrayList<>();
    /**支付名称*/
    private final ArrayList<String> payNames = new ArrayList<>();
    /**广告*/
    private final ArrayList<String> payAds = new ArrayList<>();
    /**建议*/
    private final ArrayList<String> paySuggests = new ArrayList<>();
    /**按钮id*/
    private final ArrayList<Integer> btnIds = new ArrayList<>();
    AfterSelectedChange afterSelectedChange;

    public CompoundButton getLastCheckedRB() {
        return lastCheckedRB;
    }

    /**选择的按钮*/
    private CompoundButton lastCheckedRB = null;

    public PayWayAdapter(Context context){
        init(context);
        this.context = context;
        Resources resources = context.getResources();
        // 支付宝logo的文件路径
        String path1 = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(R.drawable.zhifubaologo) + "/"
                + resources.getResourceTypeName(R.drawable.zhifubaologo) + "/"
                + resources.getResourceEntryName(R.drawable.zhifubaologo);
        // 花呗logo的文件路径
        String path2 = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(R.drawable.huabeilogo) + "/"
                + resources.getResourceTypeName(R.drawable.huabeilogo) + "/"
                + resources.getResourceEntryName(R.drawable.huabeilogo);
        // 花呗分期logo的文件路径
        String path3 = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(R.drawable.huabeifenqilogo) + "/"
                + resources.getResourceTypeName(R.drawable.huabeifenqilogo) + "/"
                + resources.getResourceEntryName(R.drawable.huabeifenqilogo);
        logoPaths.add(path1);
        logoPaths.add(path2);
        logoPaths.add(path3);
        payNames.add(resources.getString(R.string.payWay_zhifubao));
        payNames.add(resources.getString(R.string.payWay_huabei));
        payNames.add(resources.getString(R.string.payWay_huabeifenqi));
        payAds.add(resources.getString(R.string.payWay_zhifubao_ad));
        payAds.add(resources.getString(R.string.payWay_huabei_ad));
        payAds.add(resources.getString(R.string.payWay_huabeifenqi_ad));
        paySuggests.add(resources.getString(R.string.payWay_zhifubao_suggest));
        paySuggests.add(null);
        paySuggests.add(null);
        btnIds.add(R.id.radioButton1);
        btnIds.add(R.id.radioButton2);
        btnIds.add(R.id.radioButton3);
    }

    private void init(Context context){
        if (!(context instanceof AfterSelectedChange)) {
            throw new IllegalStateException(TAG+"所在的Context必须实现AfterSelectedChange接口");
        }
        afterSelectedChange = (AfterSelectedChange)context;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pay_way,parent,false);
        return new PayWayAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Glide.with(context).load(logoPaths.get(position)).into(holder.payWayIvLogo);
        holder.payWayName.setText(payNames.get(position));
        holder.payWayAd.setText(payAds.get(position));
        holder.payWaySuggest.setText(paySuggests.get(position));
        holder.payWayButton.setId(btnIds.get(position));
        holder.payWayButton.setTag(position);
        holder.payWayButton.setOnCheckedChangeListener(ls);
        if(position == 0){
            holder.itemPayWay.setBackgroundResource(R.drawable.border_white_top_bottom_half);
            holder.payWayButton.setChecked(true);
        }else if(position == 1){
            holder.payWaySuggest.setVisibility(View.GONE);
        }else {
            holder.payWaySuggest.setVisibility(View.GONE);
        }
    }

    private final CompoundButton.OnCheckedChangeListener ls = ((buttonView, isChecked) -> {
        int tag = (int) buttonView.getTag();
        if (lastCheckedRB == null) {
            lastCheckedRB = buttonView;
        } else if (tag != (int) lastCheckedRB.getTag()) {
            lastCheckedRB.setChecked(false);
            lastCheckedRB = buttonView;
        }
        afterSelectedChange.changeButtonText(lastCheckedRB);
    });

    @Override
    public int getItemCount() {
        return logoPaths.size();
    }

    public interface AfterSelectedChange{
        void changeButtonText(CompoundButton lastCheckedRB);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        View itemPayWay;
        ImageView payWayIvLogo;
        TextView payWayName;
        TextView payWayAd;
        TextView payWaySuggest;
        RadioButton payWayButton;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            itemPayWay = itemView.findViewById(R.id.item_payWay);
            payWayIvLogo = itemPayWay.findViewById(R.id.payWay_iv_logo);
            payWayName = itemPayWay.findViewById(R.id.payWay_name);
            payWayAd = itemPayWay.findViewById(R.id.payWay_ad);
            payWaySuggest = itemPayWay.findViewById(R.id.payWay_suggest);
            payWayButton = itemPayWay.findViewById(R.id.payWay_button);
        }
    }
}
