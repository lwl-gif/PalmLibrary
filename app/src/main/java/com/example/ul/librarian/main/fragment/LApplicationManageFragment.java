package com.example.ul.librarian.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.adapter.ApplicationListAdapter;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.callback.SearchCallback;
import com.example.ul.librarian.LMainActivity;
import com.example.ul.model.Application;
import com.example.ul.model.UserInfo;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.example.ul.view.MySearchView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Response;

/**
 * @Author: Wallace
 * @Description: 处罚管理界面
 * @Date: 2021/3/6 14:24
 * @Modified By:
 */
public class LApplicationManageFragment extends Fragment implements HttpUtil.MyCallback, SearchCallback {

    private static final String TAG = "LApplicationManage";
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 1300;
    /**请求失败*/
    private static final int REQUEST_FAIL = 13000;
    /**请求成功，但子线程解析数据失败*/
    private static final int REQUEST_BUT_FAIL_READ_DATA = 13001;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 1301;
    /**获取处罚记录部分信息*/
    private static final int GET_APPLICATION_LIST = 1302;
    /**获取处罚记录部分信息时无数据需要渲染*/
    private static final int GET_APPLICATION_LIST_NOT_FILL = 13020;
    /**获取处罚记录部分信息时，有数据*/
    private static final int GET_APPLICATION_LIST_FILL = 13021;

    private CallbackToMainActivity callbackToMainActivity;

    private String token;

    private String queryString;

    private RecyclerView recyclerView;

    private ApplicationListAdapter adapter;

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    public void searchAction(String s) {
        
    }

    static class MyHandler extends Handler {

        private WeakReference<LApplicationManageFragment> lApplicationManageFragment;

        public MyHandler(WeakReference<LApplicationManageFragment> lApplicationManageFragment) {
            this.lApplicationManageFragment = lApplicationManageFragment;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            LMainActivity myActivity = (LMainActivity) lApplicationManageFragment.get().getParentFragment().getActivity();
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            }else if (what == REQUEST_BUT_FAIL_READ_DATA) {
                Toast.makeText(myActivity, "子线程解析数据异常！", Toast.LENGTH_SHORT).show();
            }else if(what == GET_APPLICATION_LIST_FILL){
                Bundle bundle = msg.getData();

                ArrayList<Application> applications = bundle.getParcelableArrayList("applications");
                lApplicationManageFragment.get().fill(applications);
            }else if(what == GET_APPLICATION_LIST_NOT_FILL){
                Toast.makeText(myActivity, "无数据！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        // 如果Context没有实现CallbackToMainActivity接口，则抛出异常
        if (!(context instanceof CallbackToMainActivity)) {
            throw new IllegalStateException("LApplicationManageFragment所在的Context必须实现CallbackToMainActivity接口");
        }
        // 把该Context当初listClickedCallback对象
        callbackToMainActivity = (CallbackToMainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getParentFragment().getActivity());
        token = userInfo.getToken();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle bundle) {

        View rootView = inflater.inflate(R.layout.application_manage, container, false);
        // 输入框
        MySearchView mySearchView = rootView.findViewById(R.id.mySearchView);
        mySearchView.setSearchCallback(this);
        // “点击检索”
        TextView textView = rootView.findViewById(R.id.textSelect);
        textView.setOnClickListener(view -> {
            query();
        });
        adapter = new ApplicationListAdapter(getParentFragment().getActivity(),new ArrayList<>(),this.callbackToMainActivity);
        recyclerView = rootView.findViewById(R.id.recyclerApplicationList);
        // 为RecyclerView设置布局管理器
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);
        // 发送请求获取数据
        String url = HttpUtil.BASE_URL + "/application/librarian/selectSome";
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("queryString",queryString);
        url = HttpUtil.newUrl(url,hashMap);
        HttpUtil.getRequest(token,url,this, GET_APPLICATION_LIST);
        return rootView;
    }

    private void fill(ArrayList<Application> applications) {
        adapter.setApplications(applications);
    }

    private void query(){

    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        adapter = null;
        token = null;
        queryString = null;
        recyclerView = null;
        // 将接口赋值为null
        callbackToMainActivity = null;
    }

    @Override
    public void success(Response response, int code) throws IOException {
        // 获取服务器响应字符串
        String result = response.body().string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        Message msg = new Message();
        Bundle data = new Bundle();
        String message = jsonObject.getString("message");
        String c = jsonObject.getString("code");
        String tip = jsonObject.getString("tip");
        // 返回值为true,说明请求被拦截
        if (HttpUtil.requestIsIntercepted(jsonObject)) {
            data.putString("code", c);
            data.putString("tip", tip);
            data.putString("message", message);
            msg.setData(data);
            msg.what = REQUEST_INTERCEPTED;
            myHandler.sendMessage(msg);
        } else {
            switch (code) {
                case GET_APPLICATION_LIST:
                    String listString = jsonObject.getString("object");
                    ArrayList<Application> applications = (ArrayList<Application>) JSON.parseArray(listString,Application.class);
                    if(applications == null || applications.size() <= 0){
                        // 发消息通知主线程无数据
                        msg.what = GET_APPLICATION_LIST_NOT_FILL;
                    }else {
                        data.putParcelableArrayList("applications", applications);
                        msg.setData(data);
                        msg.what = GET_APPLICATION_LIST_FILL;
                    }
                    myHandler.sendMessage(msg);
                    break;
                default:
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
        bundle.putString("reason", reason);
        message.setData(bundle);
        myHandler.sendMessage(message);
    }
}
