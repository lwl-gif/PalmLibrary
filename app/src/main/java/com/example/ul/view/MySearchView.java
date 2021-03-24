package com.example.ul.view;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import com.example.ul.R;
import com.example.ul.callback.SearchCallback;
import com.example.ul.util.RecordSQLiteOpenHelper;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/5 16:54
 * @Modified By:
 */
public class MySearchView extends LinearLayout{
    /**
     * 初始化成员变量
     */
    private LinearLayout search_block; // 搜索框布局
    // 搜索框组件
    private SearchView et_search;   //搜索框
    private Button btn_clear;       //清空搜索记录
    // 回调接口
    private SearchCallback searchCallback;   // 搜索按键回调接口
    // 用于存放历史搜索记录
    private RecordSQLiteOpenHelper helper ;
    private SQLiteDatabase db;
    // ListView列表 & 适配器
    private Search_ListView listView;
    private BaseAdapter adapter;
    //上下文
    private Context context;

    public MySearchView(Context context){
        super(context);
        this.context = context;
//        this.viewGroup = viewGroup;
        // 实例化数据库SQLiteOpenHelper子类对象
        helper = new RecordSQLiteOpenHelper(context);
        initView();
    }
    public MySearchView(Context context, AttributeSet attrs){
        super(context,attrs);
        this.context = context;
//        this.viewGroup = viewGroup;
        // 实例化数据库SQLiteOpenHelper子类对象
        helper = new RecordSQLiteOpenHelper(context);
        initView();
    }
    public MySearchView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        this.context = context;
//        this.viewGroup = viewGroup;
        // 实例化数据库SQLiteOpenHelper子类对象
        helper = new RecordSQLiteOpenHelper(context);
        initView();
    }

    public void setSearchCallback(SearchCallback searchCallback){
        this.searchCallback = searchCallback;
    }

    /**
     * 绑定 搜索框 组件
     */
    private void initView() {
        // 1. 绑定R.layout.search_layout作为搜索框的xml文件
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.search_layout,this,true);
        // 2. 绑定搜索框EditText
        et_search = (SearchView)rootView.findViewById(R.id.et_search);
        et_search.setQueryHint("输入关键字搜索");
        //历史搜索记录
        listView = (Search_ListView) rootView.findViewById(R.id.listView);
        /**
         * 搜索记录列表（ListView）监听
         * 即当用户点击搜索历史里的字段后,会直接将结果当作搜索字段进行搜索
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取用户点击列表里的文字,并自动填充到搜索框内
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                String name = textView.getText().toString();
                et_search.setQuery(name,true);
                Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
            }
        });
        // 5. 删除历史搜索记录 按钮
        btn_clear = (Button) rootView.findViewById(R.id.tv_clear);
        btn_clear.setVisibility(GONE); // 初始状态 = 隐藏
        btn_clear.setOnClickListener(view -> {
                // 清空数据库->>关注2
                deleteData();
                // 模糊搜索空字符 = 显示所有的搜索历史（此时是没有搜索记录的） & 显示该按钮的条件->>关注3
                queryData("");
        });
        /**
         * 搜索框的文本变化实时监听
         */
        et_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 每次输入后，模糊查询数据库 & 实时显示历史搜索记录
                // 注：若搜索框为空,则模糊搜索空字符 = 显示所有的搜索历史
                String tempName = et_search.getQuery().toString();
                queryData(tempName); // ->>关注1
                return true;
            }
        });
        /**
         * 监听输入键盘更换后的搜索按键
         * 调用时刻：点击键盘上的搜索键时
         */
        et_search.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

                    // 1. 点击搜索按键后，根据输入的搜索字段进行查询
                    // 注：由于此处需求会根据自身情况不同而不同，所以具体逻辑由开发者自己实现，此处仅留出接口
                    if (!(searchCallback == null)){
                        searchCallback.searchAction(et_search.getQuery().toString().trim());
                    }
                    Toast.makeText(context, "需要搜索的是" + et_search.getQuery(), Toast.LENGTH_SHORT).show();
                    // 2. 点击搜索键后，对该搜索字段在数据库是否存在进行检查（查询）->> 关注3
                    boolean hasData = hasData(et_search.getQuery().toString().trim());
                    // 3. 若存在，则不保存；若不存在，则将该搜索字段保存（插入）到数据库，并作为历史搜索记录
                    if (!hasData) {
                        insertData(et_search.getQuery().toString().trim()); // ->>关注4
                        queryData("");
                    }
                }
                return false;
            }
        });
    }
    /**
     * 关注1
     * 模糊查询数据 & 显示到ListView列表上
     */
    private void queryData(String tempName) {
        // 1. 模糊搜索
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "select id as _id,name from records where name like '%" + tempName + "%' order by id desc ", null);
        // 2. 创建adapter适配器对象 & 装入模糊搜索的结果
        adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursor, new String[] { "name" },
                new int[] { android.R.id.text1 }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // 3. 设置适配器
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        System.out.println(cursor.getCount());
        // 当输入框为空 & 数据库中有搜索记录时，显示 "删除搜索记录"按钮
        if (tempName.equals("") && cursor.getCount() != 0){
            btn_clear.setVisibility(VISIBLE);
        }
        else {
            btn_clear.setVisibility(GONE);
        };

    }
    /**
     * 关注2：清空数据库
     */
    void deleteData(){
        db = helper.getWritableDatabase();
        db.execSQL("delete from records");
        db.close();
        btn_clear.setVisibility(GONE);
    }
    /**
     * 关注3
     * 检查数据库中是否已经有该搜索记录
     */
    private boolean hasData(String tempName) {
        // 从数据库中Record表里找到name=tempName的id
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "select id as _id,name from records where name =?", new String[]{tempName});
        //  判断是否有下一个
        return cursor.moveToNext();
    }
    /**
     * 关注4
     * 插入数据到数据库，即写入搜索字段到历史搜索记录
     */
    private void insertData(String tempName) {
        db = helper.getWritableDatabase();
        db.execSQL("insert into records(name) values('" + tempName + "')");
        db.close();
    }

}
