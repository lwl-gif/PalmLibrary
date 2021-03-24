package com.example.ul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.ul.R;
import java.util.List;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/21 18:09
 * @Modified By:
 */
public class MySpinnerBelongAdapter extends BaseAdapter {

    private final String TAG = "SpinnerAdapter";
    private Context context;
    //定义需要包装的List<String>对象
    private List<String> list;

    public MySpinnerBelongAdapter(Context context, List<String> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if(view == null)
        {
            view = layoutInflater.inflate(R.layout.spinner_item, null);
        }
        TextView textView = (TextView)view.findViewById(R.id.s_item);
        textView.setText(list.get(i));
        return view;
    }
}
