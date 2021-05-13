package com.example.ul.reader.main.fragment;

import android.annotation.SuppressLint;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.adapter.BorrowListAdapter;
import com.example.ul.callback.CallbackToRBorrowFragment;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.callback.SearchCallback;
import com.example.ul.model.UserInfo;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.example.ul.view.MySearchView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import okhttp3.Response;

/**
 * @Author: Wallace
 * @Description: 读者借阅管理
 * @Date: 2021/3/9 20:29
 * @Modified By:
 */
public class RBorrowFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, CallbackToRBorrowFragment, SearchCallback,HttpUtil.MyCallback {

    private static final String TAG = "RBorrowFragment";
    /**未知请求*/
    private static final int UNKNOWN_REQUEST_ERROR = 400;
    /**请求失败*/
    private static final int REQUEST_FAIL = 4000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 402;
    /**查询借阅、预约和已过期的记录*/
    private static final int GET_RECORD = 401;
    /**查询成功，通知主线程渲染*/
    private static final int GET_RECORD_SUCCESS_FILL = 40111;
    /**查询成功，但不需要渲染*/
    private static final int GET_RECORD_SUCCESS_NOT_FILL = 40110;
    /**查询失败*/
    private static final int GET_RECORD_FAIL = 4010;
    /**搜索框内容*/
    String queryString = "null";
    /**checkBox选中标志*/
    private boolean box1,box2,box3;
    /**文本框——当前借阅\当前预约\过期记录*/
    private TextView textView1,textView2,textView3;
    /**列表——当前借阅\当前预约\过期记录*/
    private RecyclerView recyclerViewBorrow,recyclerViewReserve,recyclerExpired;
    /**服务器返回的信息*/
    private JSONArray jsonArray;
    /**每个列表的数据*/
    private JSONArray jsonArrayBorrow;
    private JSONArray jsonArrayReserve;
    private JSONArray jsonArrayExpired;
    private String token;
    private CallbackToMainActivity callbackToMainActivity;

    static class MyHandler extends Handler {
        private WeakReference<RBorrowFragment> rBorrowFragment;
        
        public MyHandler(WeakReference<RBorrowFragment> rBorrowFragment){
            this.rBorrowFragment = rBorrowFragment;
        }
        
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            if(what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(rBorrowFragment.get().getActivity(),bundle.getString("reason"),Toast.LENGTH_SHORT).show();
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
                    DialogUtil.showDialog(rBorrowFragment.get().getActivity(),view,false);
                }
            }
        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        Log.i(TAG, "借阅界面加载了！");
        // 如果Context没有实现ListClickedCallback接口，则抛出异常
        if (!(context instanceof CallbackToMainActivity)) {
            throw new IllegalStateException("RBorrowFragment所在的Context必须实现CallbackToMainActivity接口");
        }
        // 把该Context当初CallbackToMainActivity对象
        callbackToMainActivity = (CallbackToMainActivity) context;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getActivity());
        token = userInfo.getToken();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.borrow_manage, container, false);
        MySearchView mySearchView = rootView.findViewById(R.id.mySearchView);
        mySearchView.setSearchCallback(this);
        TextView textView = rootView.findViewById(R.id.textSelect);
        textView.setOnClickListener(view -> {
            query();
        });
        // 复选框——当前借阅\当前预约\过期记录
        CheckBox checkBox1 = rootView.findViewById(R.id.checkBox1);
        checkBox1.setOnCheckedChangeListener(this);
        CheckBox checkBox2 = rootView.findViewById(R.id.checkBox2);
        checkBox2.setOnCheckedChangeListener(this);
        CheckBox checkBox3 = rootView.findViewById(R.id.checkBox3);
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
        // 默认的界面设置
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
    public void onDetach() {
        super.onDetach();
        // 将接口赋值为null
        callbackToMainActivity = null;
    }

    @Override
    public void searchAction(String s) {
        queryString = s;
    }
    /**
     * @Author: Wallace
     * @Description: 根据搜索框内容以及复选框选中的条件进行查询
     * @Date: Created in 20:43 2021/3/10
     * @Modified By:
     * @return: void
     */
    private void query() {
        // 搜索框的内容
        if ((queryString == null) || ("".equals(queryString))) {
            queryString = "null";
        }

        // 定义发送的URL
        String url = HttpUtil.BASE_URL + "borrow/myBorrow";
        // 使用Map封装请求参数
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("checkBox1", Boolean.toString(box1));
        hashMap.put("checkBox2", Boolean.toString(box2));
        hashMap.put("checkBox3", Boolean.toString(box3));
        hashMap.put("queryString", queryString);
        url = HttpUtil.newUrl(url,hashMap);
        HttpUtil.getRequest(token, url, this, GET_RECORD);
    }

    private void fill() {
        if((jsonArray == null)||(jsonArray.size() <= 0)){
            Toast.makeText(getActivity(),"未获取到数据！",Toast.LENGTH_LONG).show();
        }else{
            BorrowListAdapter adapter;
            if(box1){
                recyclerViewBorrow.setVisibility(View.VISIBLE);
                if(jsonArrayBorrow.size()>0){
                    recyclerViewBorrow.setHasFixedSize(true);
                    // 为RecyclerView设置布局管理器
                    recyclerViewBorrow.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
                    // 将服务器响应包装成Adapter
                    adapter = new BorrowListAdapter(getActivity(),jsonArrayBorrow,"id","name","readerId","readerName",
                            "state","start","end","box1",this);
                    recyclerViewBorrow.setAdapter(adapter);
                }
            }
            if(box2){
                recyclerViewReserve.setVisibility(View.VISIBLE);
                if(jsonArrayReserve.size()>0){
                    recyclerViewReserve.setHasFixedSize(true);
                    // 为RecyclerView设置布局管理器
                    recyclerViewReserve.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
                    // 将服务器响应包装成Adapter
                    adapter = new BorrowListAdapter(getActivity(),jsonArrayReserve,"id","name","readerId","readerName",
                            "state","start","end","box2",this);
                    recyclerViewReserve.setAdapter(adapter);
                }
            }
            if(box3){
                recyclerExpired.setVisibility(View.VISIBLE);
                if(jsonArrayExpired.size()>0){
                    recyclerExpired.setHasFixedSize(true);
                    // 为RecyclerView设置布局管理器
                    recyclerExpired.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
                    // 将服务器响应包装成Adapter
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
        switch (checkBox.getId()) {
            // 选中当前借阅
            case R.id.checkBox1:
                if (checked) {
                    box1 = true;
                    textView1.setVisibility(View.VISIBLE);
                    recyclerViewBorrow.setVisibility(View.VISIBLE);
                } else {
                    box1 = false;
                    textView1.setVisibility(View.GONE);
                    recyclerViewBorrow.setVisibility(View.GONE);
                }
                break;
            // 选中当前预约
            case R.id.checkBox2:
                if (checked) {
                    box2 = true;
                    textView2.setVisibility(View.VISIBLE);
                    recyclerViewReserve.setVisibility(View.VISIBLE);
                } else {
                    box2 = false;
                    textView2.setVisibility(View.GONE);
                    recyclerViewReserve.setVisibility(View.GONE);
                }
                break;
            // 选中过期记录
            case R.id.checkBox3:
                if (checked) {
                    box3 = true;
                    textView3.setVisibility(View.VISIBLE);
                    recyclerExpired.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(),"该功能未开发！",Toast.LENGTH_SHORT).show();
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
        // 获取服务器响应字符串
        String result = response.body().string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        Message msg = new Message();
        Bundle bundle = new Bundle();
        String message = jsonObject.getString("message");
        String c = jsonObject.getString("code");
        String tip = jsonObject.getString("tip");
        // 返回值为true,说明请求被拦截
        if (HttpUtil.requestIsIntercepted(jsonObject)) {
            bundle.putString("code", c);
            bundle.putString("tip", tip);
            bundle.putString("message", message);
            msg.setData(bundle);
            msg.what = REQUEST_INTERCEPTED;
            myHandler.sendMessage(msg);
        } else {
            if (code == GET_RECORD) {
                bundle.putString("code", c);
                bundle.putString("tip", tip);
                bundle.putString("message", message);
                msg.setData(bundle);
                if ("查询成功！".equals(message)) {
                    jsonObject.getJSONArray("dataArray");
                    jsonArray = jsonObject.getJSONArray("dataArray");
                    // 将整个信息拆分
                    jsonArrayBorrow = jsonArray.getJSONArray(0);
                    jsonArrayReserve = jsonArray.getJSONArray(1);
                    jsonArrayExpired = jsonArray.getJSONArray(2);
                    if (jsonArrayBorrow.size() + jsonArrayReserve.size() + jsonArrayExpired.size() == 0) {
                        msg.what = GET_RECORD_SUCCESS_NOT_FILL;
                    }
                    msg.what = GET_RECORD_SUCCESS_FILL;
                } else {
                    msg.what = GET_RECORD_FAIL;
                }
                myHandler.sendMessage(msg);
            } else {
                bundle.putString("reason","未知错误");
                myHandler.sendEmptyMessage(UNKNOWN_REQUEST_ERROR);
            }
        }
    }

    @Override
    public void failed(IOException e, int code) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        String reason;
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
        bundle.putString("reason",reason);
        message.setData(bundle);
        myHandler.sendMessage(message);
    }
}
