package com.example.ul.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.alibaba.fastjson.JSONArray;
import com.example.ul.R;


/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/21 17:57
 * @Modified By:
 */
public class MySpinnerAdapter extends BaseAdapter {

    private final String TAG = "MySpinnerAdapter";
    private Context context;
    /**定义需要包装的JSONArray对象*/
    private JSONArray jsonArray;

    public MySpinnerAdapter(Context context, JSONArray jsonArray){
        this.context = context;
        this.jsonArray = jsonArray;
    }

    @Override
    public int getCount() {
        return jsonArray.size();
    }

    @Override
    public Object getItem(int i) {
        return jsonArray.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if(view == null) {
            view = layoutInflater.inflate(R.layout.spinner_item, null);
        }
        TextView textView = (TextView)view.findViewById(R.id.s_item);
        textView.setText(jsonArray.getString(i));
        return view;
    }
}