package com.example.ul.reader.main.activity;


import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.activity.ShowPictureActivity;
import com.example.ul.adapter.ImagesOnlyReadAdapter;
import com.example.ul.callback.ImageAdapterItemListener;
import com.example.ul.model.Book;
import com.example.ul.model.Classification;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
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
 * @Description: 读者查看书籍详情的活动
 * @Date: Created in 16:20 2021/3/22
 * @Modified By:
 */
@SuppressLint("NonConstantResourceId")
public class RBookDetailActivity extends Activity implements HttpUtil.MyCallback, ImageAdapterItemListener{

    private static final String TAG = "RBookDetailActivity";
    /**未知请求*/
    private static final int UNKNOWN_REQUEST_ERROR = 600;
    /**请求失败*/
    private static final int REQUEST_FAIL = 6000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 6001;
    /**获取书本详情*/
    private static final int GET_BOOK_DETAIL = 601;
    /**获取书本详情成功，有数据需要渲染*/
    private static final int GET_BOOK_DETAIL_FILL = 6011;
    /**获取书本详情失败或无数据需要渲染*/
    private static final int GET_BOOK_DETAIL_NOT_FILL = 6010;
    /**预约书本*/
    private static final int RESERVE_BOOK = 602;
    /**预约书本成功*/
    private static final int RESERVE_BOOK_SUCCESS = 6021;
    /**预约书本失败*/
    private static final int RESERVE_BOOK_FAIL = 6020;
    /**服务器返回的书本详情数据*/
    private JSONObject jsonObjectBookDetail = null;
    /**当前书本id*/
    private int id;
    @BindView(R.id.bookId)
    public TextView tId;
    @BindView(R.id.bookName)
    public TextView tName;
    @BindView(R.id.bookAuthor)
    public TextView tAuthor;
    @BindView(R.id.bookIsbn)
    public TextView tIsbn;
    @BindView(R.id.bookLibrary)
    public TextView tLibrary;
    @BindView(R.id.bookLocation)
    public TextView tLocation;
    @BindView(R.id.bookCallNumber)
    public TextView tCallNumber;
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
    @BindView(R.id.bookHouse)
    public TextView tHouse;
    @BindView(R.id.bookDate)
    public TextView tDate;
    @BindView(R.id.bookPrice)
    public TextView tPrice;
    @BindView(R.id.bookHot)
    public TextView tHot;
    @BindView(R.id.bookState)
    public TextView tState;
    @BindView(R.id.iv_back)
    public ImageView ivBack;
    @BindView(R.id.bookDetail_reserve)
    public Button btnReserve;
    private String token;
    private ImagesOnlyReadAdapter imagesOnlyReadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_r_book_detail);
        ButterKnife.bind(this);
        // 判断传进来的id是否为空
        id = this.getIntent().getIntExtra("id",-1);
        if(id == -1){
            Toast.makeText(this,"无法获取书本详情！",Toast.LENGTH_SHORT).show();
            finish();
        }
        //获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        token = userInfo.getToken();
        TextView tTitle = findViewById(R.id.bookDetail_title);
        tTitle.setText("查看详情");
        ivBack.setOnClickListener(view -> {
            RBookDetailActivity.this.finish();
        });
        btnReserve.setVisibility(View.GONE);
        btnReserve.setOnClickListener(view -> {
            //发送预约请求
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", String.valueOf(id));
            String url = HttpUtil.BASE_URL + "reservation/reserveBook";
            HttpUtil.postRequest(token,url,hashMap,this,RESERVE_BOOK);
        });
        imagesOnlyReadAdapter = new ImagesOnlyReadAdapter(this,token);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(imagesOnlyReadAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(jsonObjectBookDetail == null){
            // 发送获取书本详情的请求，获取书本详情
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", String.valueOf(id));
            String url = HttpUtil.BASE_URL + "book/selectAllById";
            url = HttpUtil.newUrl(url,hashMap);
            HttpUtil.getRequest(token, url, this, GET_BOOK_DETAIL);
        }else {
            fillData();
        }
    }

    private void fillData() {
        id = jsonObjectBookDetail.getInteger("id");
        String s = "No." + id;
        tId.setText(s);
        tName.setText(jsonObjectBookDetail.getString("name"));
        tAuthor.setText(jsonObjectBookDetail.getString("author"));
        tIsbn.setText(jsonObjectBookDetail.getString("isbn"));
        tLibrary.setText(jsonObjectBookDetail.getString("library"));
        tLocation.setText(jsonObjectBookDetail.getString("location"));
        tCallNumber.setText(jsonObjectBookDetail.getString("callNumber"));
        tTheme.setText(jsonObjectBookDetail.getString("theme"));
        tDesc.setText(jsonObjectBookDetail.getString("description"));
        tType.setText(jsonObjectBookDetail.getString("typeName"));
        tHouse.setText(jsonObjectBookDetail.getString("house"));
        tHot.setText(jsonObjectBookDetail.getString("hot"));
        tState.setText(jsonObjectBookDetail.getString("state"));
        tPrice.setText(jsonObjectBookDetail.getString("price"));
        JSONObject belong = jsonObjectBookDetail.getJSONObject("classification");
        tFirst.setText(belong.getString("first"));
        tThird.setText(belong.getString("third"));
        String d = jsonObjectBookDetail.getString("date");
        if ("null".equals(d) || "".equals(d)) {
            tDate.setText(null);
        } else {
            long l = Long.parseLong(d);
            Date date = new Date(l);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String tvDate = format.format(date);
            tDate.setText(tvDate);
        }
        // 获取图片名，构造出获取图片的url
        // 获取图片的基本url
        String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
        String images = jsonObjectBookDetail.getString("images");
        JSONArray jsonArray1 = jsonObjectBookDetail.getJSONArray("pictures");
        if (jsonArray1.size() > 0) {
            ArrayList<String> arrayList = new ArrayList<>();
            for (int i = 0; i < jsonArray1.size(); i++) {
                String url = baseUrl + images + "/" + jsonArray1.get(i);
                arrayList.add(url);
            }
            imagesOnlyReadAdapter.setImageNameUrlList(arrayList);
        }
        btnReserve.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClickToShow(int position) {
        Intent intent = new Intent(RBookDetailActivity.this, ShowPictureActivity.class);
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
        super.onDestroy();
        id = -1;
        ActivityManager.getInstance().removeActivity(this);
    }

    static class MyHandler extends Handler {
        private WeakReference<RBookDetailActivity> rBookDetailActivity;
        public MyHandler(WeakReference<RBookDetailActivity> rBookDetailActivity){
            this.rBookDetailActivity = rBookDetailActivity;
        }
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            RBookDetailActivity myActivity = rBookDetailActivity.get();
            if(what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle data = msg.getData();
                Toast.makeText(myActivity,data.getString("reason"), Toast.LENGTH_SHORT).show();
            } else if (what == GET_BOOK_DETAIL_FILL) {
                myActivity.fillData();
            } else {
                Bundle data = msg.getData();
                String code = data.getString("code");
                String message = data.getString("message");
                String tip = data.getString("tip");
                if (what == RESERVE_BOOK_SUCCESS) {
                    Toast.makeText(myActivity, message + tip, Toast.LENGTH_LONG).show();
                }else {
                    Bundle bundle = new Bundle();
                    bundle.putString("message",message);
                    bundle.putString("code",code);
                    bundle.putString("tip",tip);
                    DialogUtil.showDialog(myActivity,TAG,bundle,false);
                }
            }
        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));
    @Override
    public void success(Response response, int code) throws IOException {
        // 获取服务器响应字符串
        String result = response.body().string().trim();
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
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
            if (code == GET_BOOK_DETAIL) {
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
            } else if (code == RESERVE_BOOK) {
                bundle.putString("code", c);
                bundle.putString("tip", tip);
                bundle.putString("message", message);
                msg.setData(bundle);
                if ("预约成功！".equals(message)) {
                    msg.what = RESERVE_BOOK_SUCCESS;
                } else {
                    msg.what = RESERVE_BOOK_FAIL;
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