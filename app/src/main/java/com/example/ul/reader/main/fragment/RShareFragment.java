package com.example.ul.reader.main.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.activity.ApplicationDetailActivity;
import com.example.ul.adapter.BookListAdapter;
import com.example.ul.callback.CallbackToBookFragment;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.librarian.main.activity.LShareDetailActivity;
import com.example.ul.model.Application;
import com.example.ul.model.Book;
import com.example.ul.model.UserInfo;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @Author: Wallace
 * @Description: 读者管理分享的图书
 * @Date: 2021/3/9 20:31
 * @Modified By:
 */
public class RShareFragment extends Fragment implements HttpUtil.MyCallback, CallbackToBookFragment {

    private static final String TAG = "RShareFragment";

    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 1500;
    /**请求失败*/
    private static final int REQUEST_FAIL = 15000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 1501;
    /**获取我的书库列表*/
    private static final int GET_MY_BOOKS = 1502;
    /**回调接口*/
    private CallbackToMainActivity listClickedCallbackMain;
    /**token*/
    private String token = null;
    /**组件*/
    private RecyclerView recyclerView;
    /**适配器*/
    private BookListAdapter adapter;
    /**我的书库*/
    private JSONArray jsonArray;


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        // 如果Context没有实现callback,ListClickedCallback接口，则抛出异常
        if (!(context instanceof CallbackToMainActivity)) {
            throw new IllegalStateException(TAG+"所在的Context必须实现CallbackTOMainActivity接口");
        }
        // 把该Context当初listClickedCallback对象
        listClickedCallbackMain = (CallbackToMainActivity) context;
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
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // 获取当前界面视图
        View rootView = inflater.inflate(R.layout.share_manage, container, false);
        // 组件初始化
        recyclerView = rootView.findViewById(R.id.shareBooks);
        Button btnAdd = rootView.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LShareDetailActivity.class);
            intent.putExtra("id",0);
            startActivity(intent);
        });
        // 获取图片的基本url
        String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
        // 将服务器响应包装成Adapter
        adapter = new BookListAdapter(getActivity(),baseUrl,token,new JSONArray(),"id","name","author","description",
                "hot","state","theme","isbn","library","images",this);
        recyclerView.setAdapter(adapter);
        return rootView;

    }

    @Override
    public void onStart() {
        super.onStart();
        // 查询自己的书库
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("selectBy","作者");
        String url = HttpUtil.BASE_URL + "book/myBooks";
        url = HttpUtil.newUrl(url,hashMap);
        HttpUtil.getRequest(token,url,this,GET_MY_BOOKS);
    }

    private void fill() {
        if(jsonArray == null || jsonArray.size() <= 0){
            Toast.makeText(getActivity(),"无数据",Toast.LENGTH_SHORT).show();
        }else {
            adapter.setJsonArray(jsonArray);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        token = null;
    }

    @Override
    public void bookListClickPosition(int position) {
        Book book = (Book) adapter.getJsonArray().get(position);
        int id = book.getId();
        String library = book.getLibrary();
        // 返回id给activity
        listClickedCallbackMain.clickToGetBookDetail(id, library,true);
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    static class MyHandler extends Handler {
        private final WeakReference<RShareFragment> rShareFragment;

        public MyHandler(WeakReference<RShareFragment> rShareFragment){
            this.rShareFragment = rShareFragment;
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            RShareFragment myFragment = rShareFragment.get();
            // 缴费
            if (what == GET_MY_BOOKS) {
                myFragment.fill();
            } else if(what == REQUEST_INTERCEPTED) {
                Bundle data = msg.getData();
                DialogUtil.showDialog(myFragment.getActivity(), TAG, data, true);
            } else {
                Bundle bundle = msg.getData();
                Toast.makeText(myFragment.getActivity(),bundle.getString("reason"),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void success(Response response, int code) throws IOException {
        // 获取服务器响应字符串
        String result = response.body().string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        Message msg = new Message();
        Bundle bundle = new Bundle();
        // 返回值为true,说明请求被拦截
        if (HttpUtil.requestIsIntercepted(jsonObject)) {
            String message = jsonObject.getString("message");
            String c = jsonObject.getString("code");
            String tip = jsonObject.getString("tip");
            bundle.putString("code", c);
            bundle.putString("tip", tip);
            bundle.putString("message", message);
            msg.setData(bundle);
            msg.what = REQUEST_INTERCEPTED;
            myHandler.sendMessage(msg);
        } else {
            if (code == GET_MY_BOOKS) {
                jsonArray = (JSONArray) jsonObject.get("object");
                myHandler.sendEmptyMessage(GET_MY_BOOKS);
            } else {
                String reason = "未知错误";
                bundle.putString("reason",reason);
                msg.setData(bundle);
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
