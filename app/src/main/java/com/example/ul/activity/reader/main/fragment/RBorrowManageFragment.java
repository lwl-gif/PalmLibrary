package com.example.ul.activity.reader.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.adapter.BorrowListAdapter;
import com.example.ul.callback.CallbackToRBorrowFragment;
import com.example.ul.callback.CallbackTOMainActivity;
import com.example.ul.callback.SearchCallback;
import com.example.ul.model.UserInfo;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.example.ul.view.MySearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import okhttp3.Response;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/9 20:29
 * @Modified By:
 */
public class RBorrowManageFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, CallbackToRBorrowFragment, SearchCallback,HttpUtil.MyCallback {
    //自定义消息代码
    //未知请求
    private static final int UNKNOWN_REQUEST = 400;
    //请求失败
    private static final int REQUEST_FAIL = 4000;
    //请求成功，但子线程解析数据失败
    private static final int REQUEST_BUT_FAIL_READ_DATA = 4001;
    //查询借阅、预约和已过期的记录
    private static final int GET_RECORD = 401;
    //查询成功，通知主线程渲染
    private static final int GET_RECORD_SUCCESS_FILL = 40111;
    //查询成功，但不需要渲染
    private static final int GET_RECORD_SUCCESS_NOT_FILL = 40110;
    //查询失败
    private static final int GET_RECORD_FAIL = 4010;

    private static final String TAG = "RBorrowFragment";
    //搜索框布局
    private MySearchView mySearchView;
    //搜索框内容
    String queryString = "null";
    //“点击搜索”文本
    private TextView textViewSelect;
    //复选框——当前借阅\当前预约\过期记录
    private CheckBox checkBox1,checkBox2,checkBox3;
    //checkBox选中标志
    private boolean box1,box2,box3;
    //文本框——当前借阅\当前预约\过期记录
    private TextView textView1,textView2,textView3;
    //列表——当前借阅\当前预约\过期记录
    private RecyclerView recyclerViewBorrow,recyclerViewReserve,recyclerExpired;
    //服务器返回的信息
    private JSONArray jsonArray;
    //每个列表的数据
    private JSONArray jsonArrayBorrow;
    private JSONArray jsonArrayReserve;
    private JSONArray jsonArrayExpired;
    //适配器
    private BorrowListAdapter adapter;
    //
    private CallbackTOMainActivity listItemClickedCallbackActivity;


    static class MyHandler extends Handler {
        private WeakReference<RBorrowManageFragment> rBorrowFragment;
        public MyHandler(WeakReference<RBorrowManageFragment> rBorrowFragment){
            this.rBorrowFragment = rBorrowFragment;
        }
        public void handleMessage(Message msg){
            int what = msg.what;
            if(what == UNKNOWN_REQUEST) {
                Toast.makeText(rBorrowFragment.get().getActivity(),"未知请求，无法处理！",Toast.LENGTH_SHORT).show();
            }
            else if(what == REQUEST_FAIL){
                Toast.makeText(rBorrowFragment.get().getActivity(),"网络异常！",Toast.LENGTH_SHORT).show();
            }else if(what == REQUEST_BUT_FAIL_READ_DATA){
                Toast.makeText(rBorrowFragment.get().getActivity(),"子线程解析数据异常！",Toast.LENGTH_SHORT).show();
            } else if (what == GET_RECORD_SUCCESS_FILL) {
                rBorrowFragment.get().fill();
            } else {
                Bundle data = msg.getData();
                String code = data.getString("code");
                String message = data.getString("message");
                String tip = data.getString("tip");
                if (what == GET_RECORD_SUCCESS_NOT_FILL) {
                    Toast.makeText(rBorrowFragment.get().getActivity(),message + tip, Toast.LENGTH_LONG).show();
                }else {
                    View view = View.inflate(rBorrowFragment.get().getActivity(),R.layout.dialog_view,null);
                    TextView tvFrom = view.findViewById(R.id.dialog_from);
                    tvFrom.setText(TAG);
                    TextView tvCode = view.findViewById(R.id.dialog_code);
                    tvCode.setText(code);
                    TextView tvMessage = view.findViewById(R.id.dialog_message);
                    tvMessage.setText(message);
                    TextView tvTip = view.findViewById(R.id.dialog_tip);
                    tvTip.setText(tip);
                    DialogUtil.showDialog(rBorrowFragment.get().getActivity(),view);
                }
            }
        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));

    public void onAttach(Context context) {
        /**
         * @Author:Wallace
         * @Description:当该Fragment被添加到Context时回调该方法,该方法只被调用一次
         * @Date:Created in 20:15 2021/3/10
         * @Modified By:
         * @param context
         * @return: void
         */
        super.onAttach(context);
        Log.i(TAG, "借阅界面加载了！");
        //如果Context没有实现ListClickedCallback接口，则抛出异常
        if (!(context instanceof CallbackTOMainActivity)) {
            throw new IllegalStateException("RBorrowFragment所在的Context必须实现ListClickedCallback接口");
        }
        //把该Context当初listClickedCallback对象
        listItemClickedCallbackActivity = (CallbackTOMainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        /**
         * @Author:Wallace
         * @Description:创建fragment时被回调，该方法只会被调用一次
         * @Date:Created in 20:16 2021/3/10
         * @Modified By:
         * @param savedInstanceState
         * @return: void
         */
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /**
         * @Author:Wallace
         * @Description:每次创建、绘制该fragment的View组件时调用的方法，Fragment将会显示该方法返回的View组件
         * @Date:Created in 20:17 2021/3/10
         * @Modified By:
         * @param inflater
         * @param container
         * @param savedInstanceState
         * @return: android.view.View
         */
        View rootView = inflater.inflate(R.layout.borrow_manage, container, false);
        mySearchView = rootView.findViewById(R.id.mySearchView);
        textViewSelect = rootView.findViewById(R.id.textSelect);
        checkBox1 = rootView.findViewById(R.id.checkBox1);
        checkBox1.setOnCheckedChangeListener(this);
        checkBox2 = rootView.findViewById(R.id.checkBox2);
        checkBox2.setOnCheckedChangeListener(this);
        checkBox3 = rootView.findViewById(R.id.checkBox3);
        checkBox3.setOnCheckedChangeListener(this);
        textView1 = rootView.findViewById(R.id.nowBorrow);
        textView1.setVisibility(View.GONE);
        recyclerViewBorrow = rootView.findViewById(R.id.recyclerBorrowList);
        recyclerViewBorrow.setVisibility(View.GONE);
        textView2 = rootView.findViewById(R.id.nowReserve);
        textView2.setVisibility(View.GONE);
        recyclerViewReserve = rootView.findViewById(R.id.recyclerReserveList);
        recyclerViewReserve.setVisibility(View.GONE);
        textView3 = rootView.findViewById(R.id.expiredRecord);
        textView3.setVisibility(View.GONE);
        recyclerExpired = rootView.findViewById(R.id.recyclerRecordList);
        recyclerExpired.setVisibility(View.GONE);
        //默认的界面设置
        checkBox1.setChecked(true);
        box1 = true;
        textView1.setVisibility(View.VISIBLE);
        recyclerViewBorrow.setVisibility(View.VISIBLE);
        checkBox2.setChecked(false);
        box2 = false;
        textView2.setVisibility(View.GONE);
        recyclerViewReserve.setVisibility(View.GONE);
        checkBox3.setChecked(false);
        box3 = false;
        textView3.setVisibility(View.GONE);
        recyclerExpired.setVisibility(View.GONE);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        /**
         * @Author:Wallace
         * @Description:当Fragment所在的Activity被启动完成后回调该方法
         * @Date:Created in 20:18 2021/3/10
         * @Modified By:
         * @param savedInstanceState
         * @return: void
         */
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        /**
         * @Author:Wallace
         * @Description:启动fragment时被回调
         * @Date:Created in 20:19 2021/3/10
         * @Modified By:
         * @param
         * @return: void
         */
        super.onStart();
    }

    @Override
    public void onStop() {
        /**
         * @Author:Wallace
         * @Description:停止fragment时被回调
         * @Date:Created in 20:19 2021/3/10
         * @Modified By:
         * @param
         * @return: void
         */
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        /**
         * @Author:Wallace
         * @Description:销毁该fragment所包含的View组件时回调
         * @Date:Created in 20:19 2021/3/10
         * @Modified By:
         * @param
         * @return: void
         */
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        /**
         * @Author:Wallace
         * @Description:销毁fragment时被回调，该方法只会执行一次
         * @Date:Created in 20:20 2021/3/10
         * @Modified By:
         * @param
         * @return: void
         */
        super.onDestroy();
    }


    public void onDetach() {
        /**
         * @Author:Wallace
         * @Description:当该Fragment从它所属的AContext中被删除、替换完成时回调该方法
         * @Date:Created in 20:20 2021/3/10
         * @Modified By:
         * @param
         * @return: void
         */
        super.onDetach();
        //将接口赋值为null
        listItemClickedCallbackActivity = null;
    }

    @Override
    public void searchAction(String s) {
        queryString = s;
    }

    private void query() {
        /**
         * @Author:Wallace
         * @Description:根据搜索框内容以及复选框选中的条件进行查询
         * @Date:Created in 20:43 2021/3/10
         * @Modified By:
         * @param
         * @return: void
         */
        //搜索框的内容
        if ((queryString == null) || (queryString.equals(""))) {
            queryString = "null";
        }
        //获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getActivity());
        String token = userInfo.getToken();
        //定义发送的URL
        String url = HttpUtil.BASE_URL + "borrow/myBorrow";
        //使用Map封装请求参数
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("checkBox1", Boolean.toString(box1));
        hashMap.put("checkBox2", Boolean.toString(box2));
        hashMap.put("checkBox3", Boolean.toString(box3));
        hashMap.put("queryString", queryString);
        // 拼接请求参数
        StringBuffer buffer = new StringBuffer(url);
        buffer.append('?');
        for (HashMap.Entry<String, String> entry : hashMap.entrySet()) {
            buffer.append(entry.getKey());
            buffer.append('=');
            buffer.append(entry.getValue());
            buffer.append('&');
        }
        buffer.deleteCharAt(buffer.length() - 1);
        url = buffer.toString();
        HttpUtil.getRequest(token, url, this, GET_RECORD);
    }

    private void fill() {
        /**
         * @Author:Wallace
         * @Description:根据复选框的情况将查询结果jsonArray渲染到界面
         * @Date:Created in 20:43 2021/3/10
         * @Modified By:
         * @param
         * @return: void
         */
        if((jsonArray==null)||!(jsonArray.length()>0)){
            Toast.makeText(getActivity(),"未获取到数据！",Toast.LENGTH_LONG);
        }else{
            if(box1==true){
                recyclerViewBorrow.setVisibility(View.VISIBLE);
                if(jsonArrayBorrow.length()>0){
                    recyclerViewBorrow.setHasFixedSize(true);
                    //为RecyclerView设置布局管理器
                    recyclerViewBorrow.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
                    //将服务器响应包装成Adapter
                    adapter = new BorrowListAdapter(getActivity(),jsonArrayBorrow,"id","name","readerId","readerName",
                            "state","start","end","box1",this);
                    recyclerViewBorrow.setAdapter(adapter);
                }
            }
            if(box2==true){
                recyclerViewReserve.setVisibility(View.VISIBLE);
                if(jsonArrayReserve.length()>0){
                    recyclerViewReserve.setHasFixedSize(true);
                    //为RecyclerView设置布局管理器
                    recyclerViewReserve.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
                    //将服务器响应包装成Adapter
                    adapter = new BorrowListAdapter(getActivity(),jsonArrayReserve,"id","name","readerId","readerName",
                            "state","start","end","box2",this);
                    recyclerViewReserve.setAdapter(adapter);
                }
            }
            if(box3==true){
                recyclerExpired.setVisibility(View.VISIBLE);
                if(jsonArrayExpired.length()>0){
                    recyclerExpired.setHasFixedSize(true);
                    //为RecyclerView设置布局管理器
                    recyclerExpired.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
                    //将服务器响应包装成Adapter
                    adapter = new BorrowListAdapter(getActivity(),jsonArrayExpired,"id","name","readerId","readerName",
                            "state","start","end","box3",this);
                    recyclerExpired.setAdapter(adapter);
                }
            }
        }
    }

    @Override
    public void borrowListToWantMore(int i) {

    }

    @Override
    public void borrowListToLent(int i) {

    }

    @Override
    public void borrowListToRenew(int i) {

    }

    @Override
    public void borrowListToLoss(int i) {

    }

    @Override
    public void borrowListToAbandon(int i) {

    }

    @Override
    public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
        switch (checkBox.getId()) {
            case R.id.checkBox1:
                if (checked) {// 选中当前借阅
                    box1 = true;
                    textView1.setVisibility(View.VISIBLE);
                    recyclerViewBorrow.setVisibility(View.VISIBLE);
                } else {
                    box1 = false;
                    textView1.setVisibility(View.GONE);
                    recyclerViewBorrow.setVisibility(View.GONE);
                }
                break;
            case R.id.checkBox2:
                if (checked) {// 选中当前预约
                    box2 = true;
                    textView2.setVisibility(View.VISIBLE);
                    recyclerViewReserve.setVisibility(View.VISIBLE);
                } else {
                    box2 = false;
                    textView2.setVisibility(View.GONE);
                    recyclerViewReserve.setVisibility(View.GONE);
                }
                break;
            case R.id.checkBox3:
                if (checked) {// 选中过期记录
                    box3 = true;
                    textView3.setVisibility(View.VISIBLE);
                    recyclerExpired.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(),"该功能未开发！",Toast.LENGTH_SHORT);
                } else {
                    box3 = false;
                    textView3.setVisibility(View.GONE);
                    recyclerExpired.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
        query();
    }

    @Override
    public void success(Response response, int code) throws IOException {
        //服务器返回的数据
        JSONObject jsonObject;
        String result = null;
        //获取服务器响应字符串
        result = response.body().string().trim();
        switch (code) {
            //获取验证码请求
            case GET_RECORD:
                try {
                    jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String c = jsonObject.getString("code");
                    String tip = jsonObject.getString("tip");
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("code",c);
                    data.putString("tip",tip);
                    data.putString("message",message);
                    msg.setData(data);
                    if(message.equals("查询成功！")){
                        jsonArray = jsonObject.getJSONArray("dataArray");
                        //将整个信息拆分
                        jsonArrayBorrow = jsonArray.getJSONArray(0);
                        jsonArrayReserve = jsonArray.getJSONArray(1);
                        jsonArrayExpired = jsonArray.getJSONArray(2);
                        if(jsonArrayBorrow.length()+jsonArrayReserve.length()+jsonArrayExpired.length()==0){
                            msg.what = GET_RECORD_SUCCESS_NOT_FILL;
                        }
                        //发消息通知主线程进行UI更新
                        msg.what = GET_RECORD_SUCCESS_FILL;
                    }else {
                       msg.what = GET_RECORD_FAIL;
                    }
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                    e.printStackTrace();
                }
                break;
            default:
                myHandler.sendEmptyMessage(UNKNOWN_REQUEST);
        }
    }

    @Override
    public void failed(IOException e, int code) {
        myHandler.sendEmptyMessage(REQUEST_FAIL);
        e.printStackTrace();
    }
}
