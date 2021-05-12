package com.example.ul.reader.main.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.activity.ShowPictureActivity;
import com.example.ul.adapter.ImagesOnlyReadAdapter;
import com.example.ul.callback.ImageAdapterItemListener;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @author luoweili
 */
@SuppressLint("NonConstantResourceId")
public class RShareDetailActivity extends Activity implements HttpUtil.MyCallback, ImageAdapterItemListener {

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
    /**组件*/
    @BindView(R.id.bookId)
    public TextView tId;
    @BindView(R.id.bookName)
    public TextView tName;
    @BindView(R.id.bookAuthor)
    public TextView tAuthor;
    @BindView(R.id.bookLibrary)
    public TextView tLibrary;
    @BindView(R.id.bookContact)
    public TextView tBookContact;
    @BindView(R.id.bookTheme)
    public TextView tTheme;
    @BindView(R.id.bookDescription)
    public TextView tDesc;
    @BindView(R.id.bookFirst)
    public TextView tFirst;
    @BindView(R.id.bookThird)
    public TextView tThird;
    @BindView(R.id.bookType)
    public TextView tType;
    @BindView(R.id.shareDate)
    public TextView tShareDate;
    @BindView(R.id.bookPrice)
    public TextView tPrice;
    @BindView(R.id.bookHot)
    public TextView tHot;
    @BindView(R.id.bookState)
    public TextView tState;
    @BindView(R.id.recyclerView)
    public RecyclerView recyclerView;
    @BindView(R.id.bookDetail_back)
    public Button bBack;
    /**当前书本id*/
    private int id;
    /**token*/
    private String token;
    /**服务器返回的书本详情数据*/
    private JSONObject jsonObjectBookDetail = null;
    private ImagesOnlyReadAdapter imagesOnlyReadAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_r_share_detail);
        ButterKnife.bind(this);
        // 判断传进来的id是否为空
        int id = this.getIntent().getIntExtra("id",0);
        if(id == 0){
            Toast.makeText(this,"无法获取书本详情！",Toast.LENGTH_SHORT).show();
            ActivityManager.getInstance().removeActivity(this);
            finish();
        }
        bBack.setOnClickListener(v -> {
            RShareDetailActivity.this.finish();
        });
        imagesOnlyReadAdapter = new ImagesOnlyReadAdapter(this,token,this);
        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setAdapter(imagesOnlyReadAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        token = userInfo.getToken();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 使用Map封装请求参数
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", String.valueOf(id));
        String url = HttpUtil.BASE_URL + "book/selectAllById";
        url = HttpUtil.newUrl(url,hashMap);
        HttpUtil.getRequest(token, url, this, GET_BOOK_DETAIL);
    }

    private void fillData() {
        id = jsonObjectBookDetail.getInteger("id");
        String s = "No." + id;
        tId.setText(s);
        tName.setText(jsonObjectBookDetail.getString("name"));
        tAuthor.setText(jsonObjectBookDetail.getString("isbn"));
        tLibrary.setText(jsonObjectBookDetail.getString("library"));
        tBookContact.setText(jsonObjectBookDetail.getString("callNumber"));
        tTheme.setText(jsonObjectBookDetail.getString("theme"));
        tDesc.setText(jsonObjectBookDetail.getString("description"));
        tType.setText(jsonObjectBookDetail.getString("typeName"));
        tHot.setText(jsonObjectBookDetail.getString("hot"));
        tState.setText(jsonObjectBookDetail.getString("state"));
        tPrice.setText(jsonObjectBookDetail.getString("price"));
        JSONObject belong = jsonObjectBookDetail.getJSONObject("classification");
        tFirst.setText(belong.getString("first"));
        tThird.setText(belong.getString("third"));
        String d = jsonObjectBookDetail.getString("date");
        if ("null".equals(d) || "".equals(d)) {
            tShareDate.setText(null);
        } else {
            long l = Long.parseLong(d);
            Date date = new Date(l);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String tvDate = format.format(date);
            tShareDate.setText(tvDate);
        }
        // 获取图片的基本url
        String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
        String images = jsonObjectBookDetail.getString("images");
        // 获取图片名，构造出获取图片的url
        JSONArray jsonArray1 = jsonObjectBookDetail.getJSONArray("pictures");
        if (jsonArray1.size() > 0) {
            ArrayList<String> arrayList = new ArrayList<>();
            for (int i = 0; i < jsonArray1.size(); i++) {
                String url = baseUrl + images + "/" + jsonArray1.get(i);
                arrayList.add(url);
            }
            imagesOnlyReadAdapter.setImageNameUrlList(arrayList);
        }
    }

    @Override
    public void onClickToShow(int position) {
        Intent intent = new Intent(RShareDetailActivity.this, ShowPictureActivity.class);
        intent.putExtra("TAG", TAG);
        intent.putExtra("position",position);
        intent.putExtra("imagesPath", imagesOnlyReadAdapter.getImagesPath());
        startActivity(intent);
    }

    @Override
    public void onClickToDelete(int position) {

    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
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
                    // 查询成功，获取书籍数据，通知主线程渲染前端
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