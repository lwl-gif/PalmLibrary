package com.example.ul.activity.librarian.main.fragment;
/*
 * 管理员管理读者信息界面碎片
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.adapter.ReaderListAdapter;
import com.example.ul.callback.Callback;
import com.example.ul.callback.CallbackTOMainActivity;
import com.example.ul.callback.CallbackToLReaderManageFragment;
import com.example.ul.util.DialogUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

public class LReaderManageFragment extends Fragment implements CallbackToLReaderManageFragment {

    private static final String TAG = "LReadManageFragment";

    /**服务器返回的所有读者的部分信息*/
    private JSONArray jsonArray;
    /**适配器*/
    private ReaderListAdapter adapter;
    /**单选按钮*/
    private RadioButton btnAll, btnChecked, btnUnchecked, btnChecking;
    /**当前读者类别*/
    private String readerType = "all";
    /**当前排序方式*/
    private String orderBy = "null";
    /**当前查询方式*/
    private String selectBy = "null";
    /**视图中的读者列表*/
    private RecyclerView recyclerViewReaderList;

    private CallbackTOMainActivity listClickedCallbackMain;

    /**当该Fragment被添加、显示到Context时，回调该方法*/
    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        //如果Context没有实现callback,ListClickedCallback接口，则抛出异常
        if (!(context instanceof CallbackTOMainActivity)) {
            throw new IllegalStateException("LReaderManageFragment所在的Context必须实现listClickedCallbackMain接口");
        }
        //把该Context当初listClickedCallback对象
        listClickedCallbackMain = (CallbackTOMainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle bundle) {
        //获取当前界面视图
        //碎片的视图
        View rootView = inflater.inflate(R.layout.reader_manage, container, false);
        //获取视图中的线性布局
        //视图中的线性布局
        LinearLayout linearLayout = rootView.findViewById(R.id.linearLayout);
        //获取视图中的RadioGroup
        //单选按钮框
        RadioGroup rg = rootView.findViewById(R.id.reader_manage_RadioGroup);
        //RadioGroup中的几个按钮
        btnAll = rg.findViewById(R.id.reader_manage_RadioGroup_all);
        btnChecked = rg.findViewById(R.id.reader_manage_RadioGroup_checked);
        btnUnchecked = rg.findViewById(R.id.reader_manage_RadioGroup_unchecked);
        btnChecking = rg.findViewById(R.id.reader_manage_RadioGroup_checking);
        rg.setOnCheckedChangeListener((group, checkedId)->{
            btnAll.setBackgroundColor(Color.WHITE);
            btnChecked.setBackgroundColor(Color.WHITE);
            btnUnchecked.setBackgroundColor(Color.WHITE);
            btnChecking.setBackgroundColor(Color.WHITE);
            switch (checkedId){
                case R.id.reader_manage_RadioGroup_all:
                    //切换到管理员个人信息详情碎片
                    btnAll.setBackgroundColor(Color.BLUE);
                    readerType = "all";
                    break;
                case R.id.reader_manage_RadioGroup_checked:
                    btnChecked.setBackgroundColor(Color.BLUE);
                    readerType = "checked";
                    break;
                case R.id.reader_manage_RadioGroup_unchecked:
                    btnUnchecked.setBackgroundColor(Color.BLUE);
                    readerType = "unchecked";
                    break;
                case R.id.reader_manage_RadioGroup_checking:
                    btnChecking.setBackgroundColor(Color.BLUE);
                    readerType = "checking";
                    break;
                default:
            }
            //根据选择的单选钮来查询
            query();
        });
        //获取线性布局中的组件(排序方式、检索方式)
        //线性布局中的排序方式下拉列表
        Spinner spinnerOrderBy = linearLayout.findViewById(R.id.spinnerOrderBy);
        spinnerOrderBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // i指的是点击的位置,通过i可以取到相应的数据源
                String info = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getActivity(),"你选择了："+info,Toast.LENGTH_SHORT).show();
                if("学院".equals(info)){
                    orderBy = "department";
                }else if("ID".equals(info)){
                    orderBy = "id";
                }else if("姓名".equals(info)){
                    orderBy = "name";
                }else if("班级".equals(info)){
                    orderBy = "classroom";
                }else {
                    orderBy = "null";
                }
                query();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //线性布局中的查询方式下拉列表
        Spinner spinnerSelectBy = linearLayout.findViewById(R.id.spinnerSelectBy);
        spinnerSelectBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //i指的是点击的位置,通过i可以取到相应的数据源
                String info = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getActivity(),"你选择了："+info,Toast.LENGTH_SHORT).show();
                if("学院".equals(info)){
                    selectBy = "department";
                }else if("ID".equals(info)){
                    selectBy = "id";
                }else if("姓名".equals(info)){
                    selectBy = "name";
                }else if("班级".equals(info)){
                    selectBy = "classroom";
                }else {
                    selectBy = "null";
                }
                query();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //获取视图中的读者列表
        recyclerViewReaderList = rootView.findViewById(R.id.recyclerReserveList);
        recyclerViewReaderList.setHasFixedSize(true);
        //为RecyclerView设置布局管理器
        recyclerViewReaderList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        btnAll.setChecked(true);
        btnAll.setBackgroundColor(Color.BLUE);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 有数据，直接渲染
        if(jsonArray != null && jsonArray.length() > 0){
            fill();
        }else {
            query();
        }
    }

    /**
     * @Author: Wallace
     * @Description: 将参数传到父碎片，调用父碎片的查询接口去查询所有读者id,name,age,department和classroom
     * @Date: Created in 13:22 2021/3/8
     * @Modified By:
     * @return: void
     */
    private void query() {
        // 获取当前的查询方式，排序方式以及读者类别（全部，已审核，未审核，待审核）

    }

    private void fill() {
        //将服务器响应包装成Adapter
        adapter = new ReaderListAdapter(getActivity(),jsonArray,"id","name","age","department","classroom",this);
        recyclerViewReaderList.setAdapter(adapter);
    }

    /**
     * @Author: Wallace
     * @Description: 获取列表中第i个读者的id，返回给所在的activity
     * @Date: Created in 22:28 2021/3/10
     * @Modified By:
     * @param i item位置
     * @return: void
     */
    @Override
    public void readerListClickPosition(int i) {
        //获取第i个读者的id
        String id = "-100001";
        try {
            id = jsonArray.optJSONObject(i).getString("id");
        } catch (JSONException e) {
            DialogUtil.showDialog(getActivity(),"LReaderManageFragment:读者id获取失败！",false);
        }
        if("-100001".equals(id)){
            DialogUtil.showDialog(getActivity(),"LReaderManageFragment:读者id获取失败！",false);
        }else {
            //返回id给activity
            listClickedCallbackMain.clickToGetReaderDetail(id);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        //将接口赋值为null
        listClickedCallbackMain = null;
    }
}
