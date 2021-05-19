package com.example.ul.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.model.Application;
import com.example.ul.model.UserInfo;
import com.example.ul.pay.PayDemoActivity;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
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
 * @author luoweili
 */
@SuppressLint("NonConstantResourceId")
public class ApplicationDetailActivity extends Activity implements HttpUtil.MyCallback,DialogUtil.DialogActionCallback{

    private static final String TAG = "ApplicationDetailActivity";
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 1400;
    /**请求失败*/
    private static final int REQUEST_FAIL = 14000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 14001;
    /**获取详情*/
    private static final int GET_APPLICATION_DETAIL = 1401;
    /**获取详情成功，有数据需要渲染*/
    private static final int GET_APPLICATION_DETAIL_FILL = 14011;
    /**获取详情失败或无数据需要渲染*/
    private static final int GET_APPLICATION_DETAIL_NOT_FILL = 14010;
    /**更新详情*/
    private static final int UPDATE_APPLICATION_DETAIL = 1402;
    /**删除详情*/
    private static final int DELETE_APPLICATION_DETAIL = 1403;
    /**去缴费*/
    private static final int GO_TO_PAY = 1904;
    /**两个可能的来源*/
    private static final String FROM_1 = "RMainActivity";
    private static final String FROM_2 = "LMainActivity";
    /**token*/
    private String token = null;
    /**来源*/
    private String from = null;
    /**当前记录的id*/
    private int id = -1;
    /**控件*/
    @BindView(R.id.applicationDetail_title)
    public TextView tvTitle;
    @BindView(R.id.payId)
    public TextView tvPayId;
    @BindView(R.id.id)
    public TextView tvId;
    @BindView(R.id.bookName)
    public TextView tvBookName;
    @BindView(R.id.readerId)
    public EditText tvReaderId;
    @BindView(R.id.readerName)
    public EditText tvReaderName;
    @BindView(R.id.createTime)
    public TextView tvCreateTime;
    @BindView(R.id.endTime)
    public EditText tvEndTime;
    @BindView(R.id.days)
    public TextView tvDays;
    @BindView(R.id.payTime)
    public TextView tvPayTime;
    @BindView(R.id.bookDescription)
    public EditText tvBookDescription;
    @BindView(R.id.pay)
    public EditText tvPay;
    @BindView(R.id.librarianId)
    public TextView tvLibrarianId;
    @BindView(R.id.right_button)
    public Button rightButton;
    @BindView(R.id.left_button)
    public Button leftButton;
    @BindView(R.id.imageView_back)
    public ImageView imageViewBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_application_detail);
        ButterKnife.bind(this);
        imageViewBack.setOnClickListener(v -> ApplicationDetailActivity.this.finish());
        // 两个按钮不可见
        rightButton.setVisibility(View.GONE);
        leftButton.setVisibility(View.GONE);
        // 哪个主页面打开的详情
        Intent intent = getIntent();
        from = intent.getStringExtra("TAG");
        id = intent.getIntExtra("id", -1);
        if(from == null || id == -1){
            Toast.makeText(this,"打开错误！",Toast.LENGTH_SHORT).show();
            finish();
        }else {
            // 获取token
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            token = userInfo.getToken();
            if(from.equals(ApplicationDetailActivity.FROM_1)){
                rightButton.setText(R.string.goPay);
            }
            if(from.equals(ApplicationDetailActivity.FROM_2)){
                rightButton.setText(R.string.update);
            }
        }
        init(false);
    }

    void init(boolean update){
        tvReaderId.setEnabled(update);
        tvReaderName.setEnabled(update);
        tvEndTime.setEnabled(update);
        tvBookDescription.setEnabled(update);
        tvPay.setEnabled(update);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 通过id查询记录详情
        HashMap<String, String> hashMap  = new HashMap<>(4);
        hashMap.put("id", String.valueOf(id));
        String url = HttpUtil.BASE_URL + "application/selectById";
        url = HttpUtil.newUrl(url,hashMap);
        HttpUtil.getRequest(token,url,this,GET_APPLICATION_DETAIL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivity(this);
        token = null;
        from = null;
        id = -1;
    }

    @SuppressLint("SetTextI18n")
    private void fillData(Application application, boolean allowEdit) {
        String payId = application.getPayId() == null ? null : "订单号:"+application.getPayId() ;
        id = application.getId() == null ? -1 : application.getId();
        String name = application.getName() == null ? null : application.getName();
        String readerId = application.getReaderId() == null ? null : application.getReaderId();
        String readerName = application.getReaderName() == null ? null : application.getReaderName();
        Date time = application.getTime();
        Date end = application.getEnd();
        Integer days = application.getDays();
        String moneyString = application.getMoney() == null ? null : application.getMoney().toString();
        Date payTime = application.getPayTime();
        String description = application.getDescription() == null ? null : application.getDescription();
        String librarianId = application.getLibrarianId() == null ? null : application.getLibrarianId();
        tvPayId.setText(payId);
        tvId.setText(String.valueOf(id));
        tvBookName.setText(name);
        tvReaderId.setText(readerId);
        tvReaderName.setText(readerName);
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String endString = end == null ? null : sf1.format(end);
        tvEndTime.setText(endString);
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",  Locale.getDefault());
        String timeString = time == null ? null : sf2.format(time);
        tvCreateTime.setText(timeString);
        String payTimeString = payTime == null ? null : sf2.format(payTime);
        tvPayTime.setText(payTimeString);
        tvDays.setText(String.valueOf(days));
        tvPay.setText(moneyString);
        tvBookDescription.setText(description);
        tvLibrarianId.setText(librarianId);
        // 底部按钮可见
        rightButton.setVisibility(View.VISIBLE);
        // 按钮绑定方法
        if(from.equals(ApplicationDetailActivity.FROM_1)){
            rightButton.setOnClickListener(v -> {
                HashMap<String, Object> hashMap = new HashMap<>(4);
                hashMap.put("requestCode", GO_TO_PAY);
                DialogUtil.showDialog(ApplicationDetailActivity.this,"缴费","你确定要继续缴费吗？",this,hashMap);
            });
        }else {
            leftButton.setVisibility(View.VISIBLE);
            // 该管理员能更新数据
            if(allowEdit){
                init(true);
                rightButton.setOnClickListener(v -> {
                    HashMap<String, Object> hashMap = new HashMap<>(4);
                    hashMap.put("requestCode",UPDATE_APPLICATION_DETAIL);
                    DialogUtil.showDialog(ApplicationDetailActivity.this,"更新记录","修改记录可能会造成严重后果，你确定要修改吗？",this,hashMap);
                });
                leftButton.setOnClickListener(v -> {
                    HashMap<String, Object> hashMap = new HashMap<>(4);
                    hashMap.put("requestCode",DELETE_APPLICATION_DETAIL);
                    DialogUtil.showDialog(ApplicationDetailActivity.this,"删除记录","删除记录可能会造成严重后果，你确定要删除吗？",this,hashMap);
                });
            }else {
                // 底部按钮不可点击
                rightButton.setClickable(false);
            }
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    public void positiveAction(HashMap<String, Object> requestParam) {
        Integer requestCode = (Integer) requestParam.get("requestCode");
        if (requestCode == null) {
            Toast.makeText(this,"请求码为空",Toast.LENGTH_SHORT).show();
        } else {
            if(requestCode == UPDATE_APPLICATION_DETAIL){
                // 更新缴费信息
                HashMap<String, String> hashMap = new HashMap<>(16);
                hashMap.put("payId",tvPayId.getText().toString().trim());
                hashMap.put("id",tvId.getText().toString().trim());
                hashMap.put("name",tvBookName.getText().toString().trim());
                hashMap.put("readerId",tvReaderId.getText().toString().trim());
                hashMap.put("readerName",tvReaderName.getText().toString().trim());
                hashMap.put("time",tvCreateTime.getText().toString().trim());
                hashMap.put("end",tvEndTime.getText().toString().trim());
                hashMap.put("money",tvPay.getText().toString().trim());
                hashMap.put("payTime",tvPayTime.getText().toString().trim());
                hashMap.put("description",tvBookDescription.getText().toString().trim());
                hashMap.put("librarianId",tvLibrarianId.getText().toString().trim());
                String url = HttpUtil.BASE_URL + "application/updateById";
                HttpUtil.putRequest(token,url,hashMap,this,UPDATE_APPLICATION_DETAIL);
            }else if(requestCode == DELETE_APPLICATION_DETAIL){
                // 删除缴费信息
                HashMap<String, String> hashMap = new HashMap<>(4);
                hashMap.put("id",tvId.getText().toString().trim());
                String url = HttpUtil.BASE_URL + "application/deleteById";
                url = HttpUtil.newUrl(url,hashMap);
                HttpUtil.deleteRequest(token,url,this,DELETE_APPLICATION_DETAIL);
            }else if(requestCode == GO_TO_PAY){
                // 进入缴费页面
                Intent intent = new Intent(ApplicationDetailActivity.this,PayDemoActivity.class);
                intent.putExtra("id",id);
                startActivity(intent);
                finish();
            }else {
                Toast.makeText(this,"未知操作",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void negativeAction(HashMap<String, Object> requestParam) {
        Integer requestCode = (Integer) requestParam.get("requestCode");
        if(requestCode == null){
            Toast.makeText(this,"请求码为空",Toast.LENGTH_SHORT).show();
        }else {
            if(requestCode == UPDATE_APPLICATION_DETAIL || requestCode == DELETE_APPLICATION_DETAIL || requestCode == GO_TO_PAY){
                Toast.makeText(this,"您选择了取消",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"未知动作",Toast.LENGTH_SHORT).show();
            }
        }
    }

    static class MyHandler extends Handler {
        private final WeakReference<ApplicationDetailActivity> applicationDetailActivity;

        public MyHandler(WeakReference<ApplicationDetailActivity> applicationDetailActivity){
            this.applicationDetailActivity = applicationDetailActivity;
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            ApplicationDetailActivity myActivity = applicationDetailActivity.get();
            if (what == GET_APPLICATION_DETAIL_FILL) {
                Bundle bundle = msg.getData();
                Application application = bundle.getParcelable("application");
                boolean allowEdit = "true".equals(bundle.getString("tip"));
                myActivity.fillData(application,allowEdit);
            } else if(what == REQUEST_INTERCEPTED) {
                Bundle data = msg.getData();
                DialogUtil.showDialog(myActivity, TAG, data, true);
            } else if(what == UPDATE_APPLICATION_DETAIL){
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity,bundle.getString("message"),Toast.LENGTH_SHORT).show();
            } else if(what == DELETE_APPLICATION_DETAIL){
                Bundle bundle = msg.getData();
                String message = bundle.getString("message");
                Toast.makeText(myActivity,message,Toast.LENGTH_SHORT).show();
                if("删除成功！".equals(message)){
                    myActivity.finish();
                }
            } else  {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity,bundle.getString("reason"),Toast.LENGTH_SHORT).show();
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
            if (code == GET_APPLICATION_DETAIL) {
                String applicationString = jsonObject.getString("object");
                if (applicationString != null) {
                    Application application = JSON.parseObject(applicationString, Application.class);
                    bundle.putParcelable("application", application);
                    bundle.putString("tip",tip);
                    msg.setData(bundle);
                    msg.what = GET_APPLICATION_DETAIL_FILL;
                } else {
                    msg.what = GET_APPLICATION_DETAIL_NOT_FILL;
                }
                myHandler.sendMessage(msg);
            }else if(code == UPDATE_APPLICATION_DETAIL){
                bundle.putString("message",message);
                msg.setData(bundle);
                msg.what = UPDATE_APPLICATION_DETAIL;
                myHandler.sendMessage(msg);
            } else if(code == DELETE_APPLICATION_DETAIL){
                bundle.putString("message",message);
                msg.setData(bundle);
                msg.what = DELETE_APPLICATION_DETAIL;
                myHandler.sendMessage(msg);
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