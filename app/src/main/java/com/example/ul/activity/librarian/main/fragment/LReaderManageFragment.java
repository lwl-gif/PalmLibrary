package com.example.ul.activity.librarian.main.fragment;
/*
 * 管理员管理读者信息界面碎片
 */
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

import org.json.JSONArray;
import org.json.JSONException;

public class LReaderManageFragment extends Fragment implements CallbackToLReaderManageFragment {

    private static final String TAG = "LReadManageFragment";

    //服务器返回的所有读者的部分信息
    private JSONArray jsonArray;
    //适配器
    private ReaderListAdapter adapter;
    //碎片的视图
    private View rootView;
    //单选按钮框
    private RadioGroup rg;
    //单选按钮
    private RadioButton btn_all,btn_checked,btn_unchecked,btn_checking;
    //当前读者类别
    private String readerType = "all";
    //视图中的线性布局
    private LinearLayout linearLayout;
    //线性布局中的排序方式下拉列表
    private Spinner spinnerOrderBy;
    //当前排序方式
    private String orderBy = "null";
    //线性布局中的查询方式下拉列表
    private Spinner spinnerSelectBy;
    //当前查询方式
    private String selectBy = "null";
    //视图中的读者列表
    private RecyclerView recyclerViewReaderList;

    private CallbackTOMainActivity listClickedCallbackMain;

    private Callback callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle bundle) {
        //获取当前界面视图
        rootView = inflater.inflate(R.layout.reader_manage,container,false);
        //获取视图中的线性布局
        linearLayout = rootView.findViewById(R.id.linearLayout);
        //获取视图中的RadioGroup
        rg = rootView.findViewById(R.id.reader_manage_RadioGroup);
        //RadioGroup中的几个按钮
        btn_all = rg.findViewById(R.id.reader_manage_RadioGroup_all);
        btn_checked = rg.findViewById(R.id.reader_manage_RadioGroup_checked);
        btn_unchecked = rg.findViewById(R.id.reader_manage_RadioGroup_unchecked);
        btn_checking = rg.findViewById(R.id.reader_manage_RadioGroup_checking);
        rg.setOnCheckedChangeListener((group,checkedId)->{
            btn_all.setBackgroundColor(Color.WHITE);
            btn_checked.setBackgroundColor(Color.WHITE);
            btn_unchecked.setBackgroundColor(Color.WHITE);
            btn_checking.setBackgroundColor(Color.WHITE);
            switch (checkedId){
                case R.id.reader_manage_RadioGroup_all:
                    //切换到管理员个人信息详情碎片
                    btn_all.setBackgroundColor(Color.BLUE);
                    readerType = "all";
                    break;
                case R.id.reader_manage_RadioGroup_checked:
                    btn_checked.setBackgroundColor(Color.BLUE);
                    readerType = "checked";
                    break;
                case R.id.reader_manage_RadioGroup_unchecked:
                    btn_unchecked.setBackgroundColor(Color.BLUE);
                    readerType = "unchecked";
                    break;
                case R.id.reader_manage_RadioGroup_checking:
                    btn_checking.setBackgroundColor(Color.BLUE);
                    readerType = "checking";
                    break;
            }
            //根据选择的单选钮来查询
            query();
        });
        //获取线性布局中的组件(排序方式、检索方式)
        spinnerOrderBy = linearLayout.findViewById(R.id.spinnerOrderBy);
        spinnerOrderBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //i指的是点击的位置,通过i可以取到相应的数据源
                String info = adapterView.getItemAtPosition(i).toString();//获取i所在的文本
                Toast.makeText(getActivity(),"你选择了："+info,Toast.LENGTH_SHORT).show();
                if(info.equals("学院")){
                    orderBy = "department";
                }else if(info.equals("ID")){
                    orderBy = "id";
                }else if(info.equals("姓名")){
                    orderBy = "name";
                }else if(info.equals("班级")){
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
        spinnerSelectBy = linearLayout.findViewById(R.id.spinnerSelectBy);
        spinnerSelectBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //i指的是点击的位置,通过i可以取到相应的数据源
                String info = adapterView.getItemAtPosition(i).toString();//获取i所在的文本
                Toast.makeText(getActivity(),"你选择了："+info,Toast.LENGTH_SHORT).show();
                if(info.equals("学院")){
                    selectBy = "department";
                }else if(info.equals("ID")){
                    selectBy = "id";
                }else if(info.equals("姓名")){
                    selectBy = "name";
                }else if(info.equals("班级")){
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
        btn_all.setChecked(true);
        btn_all.setBackgroundColor(Color.BLUE);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    //查询所有读者id,name,age,department和classroom
    private void query() {
        /**
         * @Author:Wallace
         * @Description:将参数传到父碎片，调用父碎片的查询接口去查询
         * @Date:Created in 13:22 2021/3/8
         * @Modified By:
          * @param
         * @return: void
         */
//        //获取当前的查询方式，排序方式以及读者类别（全部，已审核，未审核，待审核）
//        JSONArray jsonArray0 = callback.readerManageQuery(selectBy,orderBy,readerType);
//        //将查询结果展示
//        if(jsonArray0!=null){
//            jsonArray = jsonArray0;
//            fill();
//        }
    }

    private void fill() {
        recyclerViewReaderList.setHasFixedSize(true);
        //为RecyclerView设置布局管理器
        recyclerViewReaderList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        //将服务器响应包装成Adapter
        adapter = new ReaderListAdapter(getActivity(),jsonArray,"id","name","age","department","classroom",this);
        recyclerViewReaderList.setAdapter(adapter);
    }

    @Override
    public void readerListClickPosition(int i) {
        /**
         * @Author:Wallace
         * @Description:获取列表中第i个读者的id，返回给所在的activity
         * @Date:Created in 22:28 2021/3/10
         * @Modified By:
          * @param i
         * @return: void
         */
        //获取第i个读者的id
        String id = "-100001";
        try {
            id = jsonArray.optJSONObject(i).getString("id");
        } catch (JSONException e) {
            DialogUtil.showDialog(getActivity(),"LReaderManageFragment:读者id获取失败！",false);
        }
        if(id.equals("-100001")){
            DialogUtil.showDialog(getActivity(),"LReaderManageFragment:读者id获取失败！",false);
        }else {
            //返回id给activity
            listClickedCallbackMain.clickToGetReaderDetail(id);
        }
    }

    //当该Fragment被添加、显示到Context时，回调该方法
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        Log.i(TAG, "读者管理界面加载了 ");
//        //父碎片
//        LMainFragment parentFragment = (LMainFragment)this.getParentFragment();
//        Log.i(TAG,"context:"+context);
//        Log.i(TAG,"parentFragment:"+parentFragment);
//        //如果Context没有实现callback,ListClickedCallback接口，则抛出异常
//        if (!(context instanceof CallbackTOMainActivity)) {
//            throw new IllegalStateException("LReaderManageFragment所在的Context必须实现ListClickedCallback接口");
//        }
//        if(!(parentFragment instanceof Callback)){
//            throw new IllegalStateException("LReaderManageFragment所在的parentFragment必须实现callback接口");
//        }
//        //把该Context当初listClickedCallback对象
//        listClickedCallbackMain = (CallbackTOMainActivity) context;
//        //把该parentFragment当初callback对象
//        callback = (Callback) parentFragment;
//    }

    //当该Fragment从它所属的Activity中被删除时回调该方法
    public void onDetach() {
        super.onDetach();
        //将接口赋值为null
        listClickedCallbackMain = null;
        callback = null;
    }

}
