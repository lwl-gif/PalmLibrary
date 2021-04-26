package com.example.ul.librarian.main.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.librarian.LMainActivity;
import com.example.ul.adapter.ReaderListAdapter;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.callback.CallbackToLReaderManageFragment;
import com.example.ul.callback.SearchCallback;
import com.example.ul.model.UserInfo;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.example.ul.view.MySearchView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import okhttp3.Response;

/**
 * @Author: Wallace
 * @Description: 管理员管理读者信息界面碎片
 * @Date: Created 13:04 2021/4/21
 * @Modified: by who yyyy-MM-dd
 */
public class LReaderManageFragment extends Fragment implements CallbackToLReaderManageFragment,HttpUtil.MyCallback, SearchCallback {

    private static final String TAG = "LReadManageFragment";
    //自定义消息代码
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 1000;
    /**请求失败*/
    private static final int REQUEST_FAIL = 10000;
    /**请求成功，但子线程解析数据失败*/
    private static final int REQUEST_BUT_FAIL_READ_DATA = 10001;
    /**获取读者部分信息*/
    private static final int GET_READER = 1001;
    /**请求成功，但无数据渲染*/
    private static final int GET_READER_NOT_FILL= 10010;
    /**请求成功，有数据渲染*/
    private static final int GET_READER_FILL= 10011;
    /**服务器返回的所有读者的部分信息*/
    private JSONArray jsonArray;
    /**单选按钮*/
    private RadioButton btnAll, btnChecked, btnUnchecked, btnChecking;
    /**检索内容*/
    private String queryString = "null";
    /**当前读者类别*/
    private String readerType = "all";
    /**当前排序方式*/
    private String orderBy = "null";
    /**当前查询方式*/
    private String selectBy = "null";
    /**视图中的读者列表*/
    private RecyclerView recyclerViewReaderList;

    private CallbackToMainActivity listClickedCallbackMain;

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    static class MyHandler extends Handler {
        private WeakReference<LReaderManageFragment> lReaderManageFragment;

        public MyHandler(WeakReference<LReaderManageFragment> lReaderManageFragment) {
            this.lReaderManageFragment = lReaderManageFragment;
        }
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
             LMainActivity myActivity = (LMainActivity) lReaderManageFragment.get().getParentFragment().getActivity();
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            }else if (what == REQUEST_BUT_FAIL_READ_DATA) {
                Toast.makeText(myActivity, "子线程解析数据异常！", Toast.LENGTH_SHORT).show();
            }else if(what == GET_READER){
                lReaderManageFragment.get().fill();
            }
        }
    }

    /**当该Fragment被添加、显示到Context时，回调该方法*/
    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        //如果Context没有实现callback,ListClickedCallback接口，则抛出异常
        if (!(context instanceof CallbackToMainActivity)) {
            throw new IllegalStateException("LReaderManageFragment所在的Context必须实现listClickedCallbackMain接口");
        }
        //把该Context当初listClickedCallback对象
        listClickedCallbackMain = (CallbackToMainActivity) context;
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
        View rootView = inflater.inflate(R.layout.reader_manage, container, false);
        LinearLayout linearLayout = rootView.findViewById(R.id.linearLayout);
        RadioGroup rg = rootView.findViewById(R.id.reader_manage_RadioGroup);
        // “点击检索”
        TextView textView = rootView.findViewById(R.id.textSelect);
        textView.setOnClickListener(view -> {
            query();
        });
        // 输入框
        MySearchView mySearchView = rootView.findViewById(R.id.mySearchView);
        mySearchView.setSearchCallback(this);
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
        recyclerViewReaderList = rootView.findViewById(R.id.recyclerReaderList);
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

    @Override
    public void searchAction(String s) {
        this.queryString = s;
    }
    /**
     * @Author: Wallace
     * @Description: 查询所有读者id,name,age,department和classroom
     * @Date: Created in 13:22 2021/3/8
     * @Modified By:
     * @return: void
     */
    private void query() {
        if("".equals(queryString)){
            queryString = "null";
        }
        if(!"null".equals(selectBy)&& "null".equals(queryString)){
            DialogUtil.showDialog(getActivity(),"当指定了检索方式时，检索内容不能为空。",false);
        }else {
            // 获取token
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(getParentFragment().getActivity());
            String token = userInfo.getToken();
            // 获取当前的查询方式，排序方式以及读者类别（全部，已审核，未审核，待审核）
            String url = HttpUtil.BASE_URL + "reader/librarian/integratedQuery";
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("queryString",queryString);
            hashMap.put("selectBy",selectBy);
            hashMap.put("orderBy",orderBy);
            hashMap.put("readerType",readerType);
            url = HttpUtil.newUrl(url,hashMap);
            HttpUtil.getRequest(token,url,this,GET_READER);
        }
    }

    private void fill() {
        // 将服务器响应包装成Adapter
        ReaderListAdapter adapter = new ReaderListAdapter(getActivity(), jsonArray, "id", "name", "age", "department", "classroom", this);
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

    @Override
    public void success(Response response, int code) throws IOException {
        //获取服务器响应字符串
        String result = response.body().string().trim();
        if(code == GET_READER){
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject jsonObject1 = jsonObject.getJSONObject("dataObject");
                jsonArray = jsonObject1.getJSONArray("readerList");
            } catch (JSONException e) {
                jsonArray = new JSONArray();
            } finally {
                myHandler.sendEmptyMessage(GET_READER);
            }
        }
    }

    @Override
    public void failed(IOException e, int code) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        String reason = null;
        if (e instanceof SocketTimeoutException) {
            reason = "连接超时";
            message.what = REQUEST_FAIL;
        } else if (e instanceof ConnectException) {
            reason = "连接服务器失败";
            message.what = REQUEST_FAIL;
        } else if (e instanceof UnknownHostException) {
            reason = "网络异常";
            message.what = REQUEST_FAIL;
        } else {
            reason = "未知错误";
            message.what = UNKNOWN_REQUEST_ERROR;
        }
        bundle.putString("reason", reason);
        message.setData(bundle);
        myHandler.sendMessage(message);
    }
}
