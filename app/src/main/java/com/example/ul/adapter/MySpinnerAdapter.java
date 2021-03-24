package com.example.ul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ul.R;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/21 17:57
 * @Modified By:
 */
public class MySpinnerAdapter extends BaseAdapter {

    private final String TAG = "MySpinnerAdapter";
    private Context context;
    //定义需要包装的JSONArray对象
    private JSONArray jsonArray;

    public MySpinnerAdapter(Context context, JSONArray jsonArray){
        this.context = context;
        this.jsonArray = jsonArray;
    }

    @Override
    public int getCount() {
        return jsonArray.length();
    }

    @Override
    public Object getItem(int i) {
        Object object = null;
        try {
            object = jsonArray.get(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
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
        try {
            textView.setText(jsonArray.getString(i));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context,"数据解析异常.",Toast.LENGTH_SHORT);
        }
        return view;
    }
}