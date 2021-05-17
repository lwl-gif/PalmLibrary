package com.example.ul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.callback.CallbackToLReaderManageFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author luoweili
 */
public class ReaderListAdapter extends RecyclerView.Adapter<ReaderListAdapter.ViewHolder> {
    private final String TAG = "ReaderListAdapter";
    private Context context;
    /**定义需要包装的JSONArray对象*/
    private JSONArray jsonArray;
    /**定义列表项显示JSONObject对象的哪些属性*/
    private final String id;
    private final String name;
    private final String age;
    private final String department;
    private final String classroom;
    /**列表项单击事件回调接口*/
    private final CallbackToLReaderManageFragment callbackToLReaderManageFragment;

    public ReaderListAdapter(Context context,JSONArray jsonArray,
                             String id,String name,String age,String department,String classroom,
                             CallbackToLReaderManageFragment callbackToLReaderManageFragment){
        this.context = context;
        this.jsonArray = jsonArray;
        this.id = id;
        this.name = name;
        this.age = age;
        this.department = department;
        this.classroom = classroom;
        this.callbackToLReaderManageFragment = callbackToLReaderManageFragment;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reader,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 获取JSONArray数组元素的id,name,age,department,classroom属性
        try {
            // 从数据源中取值
            String itemId = jsonArray.optJSONObject(position).getString(id);
            String itemName = jsonArray.optJSONObject(position).getString(name);
            String itemAge = jsonArray.optJSONObject(position).getString(age);
            String itemDepartment = jsonArray.optJSONObject(position).getString(department);
            String itemClassroom = jsonArray.optJSONObject(position).getString(classroom);
            // 给列表项组件赋值
            holder.id.setText(itemId);
            holder.name.setText(itemName);
            holder.age.setText(itemAge);
            holder.department.setText(itemDepartment);
            holder.classroom.setText(itemClassroom);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView id;
        private TextView name;
        private TextView age;
        private TextView department;
        private TextView classroom;

        public ViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.readerId);
            name = itemView.findViewById(R.id.readerName);
            age = itemView.findViewById(R.id.readerAge);
            department = itemView.findViewById(R.id.readerDepartment);
            classroom = itemView.findViewById(R.id.readerClassroom);
            if(callbackToLReaderManageFragment !=null){
                itemView.findViewById(R.id.book_root).setOnClickListener(view -> callbackToLReaderManageFragment.readerListClickPosition(this.getLayoutPosition()));
            }
        }
    }
}
