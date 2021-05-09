package com.example.ul.reader.main.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.adapter.ImagesOnlyReadAdapter;
import com.example.ul.util.HttpUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Response;

/**
 * @author luoweili
 */
public class RShareDetailActivity extends RBookDetailActivity {

    private static final String TAG = "RShareDetailActivity";
    /**未知请求*/
    private static final int UNKNOWN_REQUEST_ERROR = 1700;
    /**请求失败*/
    private static final int REQUEST_FAIL = 17000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 1701;
    /**获取书本详情*/
    private static final int GET_BOOK_DETAIL = 1702;
    /**获取书本详情成功，有数据需要渲染*/
    private static final int GET_BOOK_DETAIL_FILL = 17021;
    /**获取书本详情失败或无数据需要渲染*/
    private static final int GET_BOOK_DETAIL_NOT_FILL = 17020;
    /**服务器返回的书本详情数据*/
    private JSONObject jsonObjectBookDetail = null;
    private ImagesOnlyReadAdapter imagesOnlyReadAdapter;
    private RecyclerView recyclerView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_r_share_detail);

    }


    private void fillData() {

    }

    static class MyHandler extends Handler {
        private WeakReference<RShareDetailActivity> rShareDetailActivity;
        public MyHandler(WeakReference<RShareDetailActivity> rShareDetailActivity){
            this.rShareDetailActivity = rShareDetailActivity;
        }
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            RShareDetailActivity myActivity = rShareDetailActivity.get();
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            } else if (what == GET_BOOK_DETAIL) {
                myActivity.fillData();
            }
        }
    }
   MyHandler myHandler = new MyHandler(new WeakReference(this));
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
        } else if (code == GET_BOOK_DETAIL) {
            if ("查询成功！".equals(message)) {
                tip = jsonObject.getString("tip");
                if ("".equals(tip)) {
                    //查询成功，获取书籍数据，通知主线程渲染前端
                    jsonObjectBookDetail = jsonObject.getJSONObject("object");
                    myHandler.sendEmptyMessage(GET_BOOK_DETAIL_FILL);
                }
            } else {
                bundle.putString("code", c);
                bundle.putString("tip", tip);
                bundle.putString("message", message);
                msg.setData(bundle);
                msg.what = GET_BOOK_DETAIL_NOT_FILL;
                myHandler.sendMessage(msg);
            }
        } else {
            myHandler.sendEmptyMessage(UNKNOWN_REQUEST_ERROR);
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