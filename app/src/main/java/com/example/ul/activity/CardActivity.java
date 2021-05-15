package com.example.ul.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.ul.R;

import com.example.ul.model.ReaderPermission;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.StringUtil;
import com.example.ul.util.UserManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;
/**
 * 展示电子卡的页面
 * @author luoweili
 */
@SuppressLint("NonConstantResourceId")
public class CardActivity extends Activity implements HttpUtil.MyCallback {
    
    private static final String TAG = "CardActivity";
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 2000;
    /**请求失败*/
    private static final int REQUEST_FAIL = 20000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 20001;
    /**获取详情*/
    private static final int GET_READER_PERMISSION_DETAIL = 2001;

    @BindView(R.id.iv_back)
    public ImageButton ivBack;
    @BindView(R.id.readerId)
    public TextView readerId;
    @BindView(R.id.readerType)
    public TextView readerType;
    @BindView(R.id.readerTerm)
    public TextView readerTerm;
    @BindView(R.id.iv_qrcode)
    public ImageView ivQrcode;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_card);
        ButterKnife.bind(this);
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        token = userInfo.getToken();
        ivBack.setOnClickListener(v -> CardActivity.this.finish());
        Log.d(TAG, "onCreate: TAG = " + TAG);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String url = HttpUtil.BASE_URL + "readerPermission/selectById";
        HttpUtil.getRequest(token,url,this,GET_READER_PERMISSION_DETAIL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivity(this);
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    static class MyHandler extends Handler {

        private final WeakReference<CardActivity> cardActivityWeakReference;

        public MyHandler(WeakReference<CardActivity> cardActivityWeakReference){
            this.cardActivityWeakReference = cardActivityWeakReference;
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            CardActivity myActivity =  cardActivityWeakReference.get();
            if (what == GET_READER_PERMISSION_DETAIL) {
                Bundle bundle = msg.getData();
                String readerPermissionString = bundle.getString("readerPermissionString");
                ReaderPermission readerPermission = JSON.parseObject(readerPermissionString, ReaderPermission.class);
                myActivity.fillData(readerPermission);
            } 
            else  {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity,bundle.getString("reason"),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fillData(ReaderPermission readerPermission) {
        String rdString = readerPermission.getId();
        rdString = StringUtil.getStarString(rdString,2, rdString.length() - 2);
        String rdTypeString = readerPermission.getTypeName();
        Date dateTerm = readerPermission.getTerm();
        String rdTermString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateTerm);
        readerId.setText(rdString);
        readerType.setText(rdTypeString);
        readerTerm.setText(rdTermString);
        String rdImageString = readerPermission.getImage();
        String url = HttpUtil.BASE_URL + "readerPermission/getImage";
        GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", this.token)
                .addHeader("image", rdImageString)
                .build());
        Glide.with(this)
                .load(glideUrl)
                .into(ivQrcode);
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
            if (code == GET_READER_PERMISSION_DETAIL) {
                String readerPermissionString = jsonObject.getString("object");
                if (readerPermissionString != null) {
                    bundle.putString("readerPermissionString",readerPermissionString);
                    msg.setData(bundle);
                    msg.what = GET_READER_PERMISSION_DETAIL;
                    myHandler.sendMessage(msg);
                }else {
                    String reason = "无数据";
                    bundle.putString("reason",reason);
                    msg.setData(bundle);
                    myHandler.sendEmptyMessage(UNKNOWN_REQUEST_ERROR);
                }
            }else {
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