package com.example.ul.reader.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.adapter.ApplicationListAdapter;
import com.example.ul.callback.CallbackToApplicationFragment;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.callback.SearchCallback;

import com.example.ul.model.Application;
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
import java.util.ArrayList;

import java.util.HashMap;

import okhttp3.Response;

/**
 * @Author: Wallace
 * @Description: 读者挂失和缴费处理的页面
 * @Date: 2021/3/9 20:30
 * @Modified By:
 */
public class RApplicationFragment extends Fragment implements CallbackToApplicationFragment,SearchCallback,HttpUtil.MyCallback {

    private static final String TAG = "ApplicationFragment";
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 1500;
    /**请求失败*/
    private static final int REQUEST_FAIL = 15000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 1501;
    /**获取处罚列表*/
    private static final int GET_APPLICATIONS = 1502;
    /**获取处罚列表,有数据*/
    private static final int GET_APPLICATIONS_FILL = 15021;
    /**获取处罚列表，无数据*/
    private static final int GET_APPLICATIONS_NO_FILL = 15020;
    /**
     * token
     */
    private String token;
    /**
     * 搜索字符串
     */
    private String queryString = "";
    /**
     * 适配器
     */
    private ApplicationListAdapter adapter;
    /**
     * 回调接口
     */
    private CallbackToMainActivity callbackToMainActivity;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (!(context instanceof CallbackToMainActivity)) {
            throw new IllegalStateException(TAG+"所在的Context必须实现CallbackToMainActivity接口");
        }
        callbackToMainActivity = (CallbackToMainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getActivity());
        token = userInfo.getToken();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.application_manage, container, false);
        // 搜索框布局
        MySearchView mySearchView = rootView.findViewById(R.id.mySearchView);
        mySearchView.setSearchCallback(this);
        // “点击搜索”文本
        TextView textViewSelect = rootView.findViewById(R.id.textSelect);
        textViewSelect.setOnClickListener(v -> query());
        adapter = new ApplicationListAdapter(getActivity(),new ArrayList<>(),this);
        // 列表——申请记录
        RecyclerView recyclerApplication = rootView.findViewById(R.id.recyclerApplicationList);
        // 为RecyclerView设置布局管理器
        recyclerApplication.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerApplication.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        query();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        adapter = null;
        token = null;
        queryString = "";
        // 将接口赋值为null
        callbackToMainActivity = null;
    }

    @Override
    public void searchAction(String s) {
        queryString = s;
    }

    private void query() {
        String url = HttpUtil.BASE_URL + "application/selectByReaderId";
        HashMap<String, String> hashMap = new HashMap<>(4);
        hashMap.put("queryString", queryString);
        url = HttpUtil.newUrl(url, hashMap);
        HttpUtil.getRequest(token, url, this, GET_APPLICATIONS);
    }

    private void fill(ArrayList<Application> applications) {
        adapter.setApplications(applications);
    }

    @Override
    public void clickToGetApplicationDetail(int i) {
        int id = adapter.getApplications().get(i).getId();
        callbackToMainActivity.clickToGetApplicationDetail(id);
    }

    static class MyHandler extends Handler{

        private final WeakReference<RApplicationFragment> rApplicationManageFragment;

        public MyHandler(WeakReference<RApplicationFragment> rApplicationManageFragment){
            this.rApplicationManageFragment = rApplicationManageFragment;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            RApplicationFragment myFragment = rApplicationManageFragment.get();
            int what = msg.what;
            if(what == GET_APPLICATIONS_FILL){
                Bundle data = msg.getData();
                ArrayList<Application> applications = data.getParcelableArrayList("applications");
                myFragment.fill(applications);
            }else if(what == GET_APPLICATIONS_NO_FILL){
                Toast.makeText(myFragment.getActivity(),"无数据",Toast.LENGTH_SHORT).show();
            }else if(what == REQUEST_INTERCEPTED) {
                Bundle data = msg.getData();
                DialogUtil.showDialog(myFragment.getActivity(), RApplicationFragment.TAG, data, true);
            }else {
                Bundle bundle = msg.getData();
                Toast.makeText(myFragment.getActivity(), bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            }
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference<>(this));

    @Override
    public void success(Response response, int code) throws IOException {
        // 获取服务器响应字符串
        String result = response.body().string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        Message msg = new Message();
        Bundle data = new Bundle();
        // 返回值为true,说明请求被拦截
        if (HttpUtil.requestIsIntercepted(jsonObject)) {
            String message = jsonObject.getString("message");
            String c = jsonObject.getString("code");
            String tip = jsonObject.getString("tip");
            data.putString("code", c);
            data.putString("tip", tip);
            data.putString("message", message);
            msg.setData(data);
            msg.what = REQUEST_INTERCEPTED;
            myHandler.sendMessage(msg);
        } else {
            if (code == GET_APPLICATIONS) {
                String applicationsString = jsonObject.getString("object");
                if (applicationsString != null && applicationsString.length() > 0) {
                    ArrayList<Application> applications = (ArrayList<Application>) JSON.parseArray(applicationsString, Application.class);
                    data.putParcelableArrayList("applications", applications);
                    msg.setData(data);
                    msg.what = GET_APPLICATIONS_FILL;
                } else {
                    msg.what = GET_APPLICATIONS_NO_FILL;
                }
                myHandler.sendMessage(msg);
            } else {
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

