package com.example.ul.activity.librarian.main.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ul.R;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @Author:Wallace
 * @Description:管理员有两种方式进入到该页面：
 * 1.添加新书时，打开该页面，数据为空，可以填入新书本的信息
 * 2.扫码或者点击查看书本详情时，进入该页面，获取书本的详情，可以修改书本信息
 * @Date:Created in 22:25 2021/3/28
 * @Modified By:
 */
public class LBookDetailActivity extends AppCompatActivity implements HttpUtil.MyCallback {

    private static final String TAG = "LBookDetailActivity";
    //自定义消息代码
    //未知请求
    private static final int UNKNOWN_REQUEST = 0700;
    //请求失败
    private static final int REQUEST_FAIL = 07000;
    //请求成功，但子线程解析数据失败
    private static final int REQUEST_BUT_FAIL_READ_DATA = 07001;
    //获取书本详情
    private static final int GET_BOOK_DETAIL = 0701;
    //获取书本详情成功，有数据需要渲染
    private static final int GET_BOOK_DETAIL_FILL = 07011;
    //获取书本详情失败或无数据需要渲染
    private static final int GET_BOOK_DETAIL_NOT_FILL = 07010;
    //预约书本
    private static final int RESERVE_BOOK = 0702;
    //预约书本成功
    private static final int RESERVE_BOOK_SUCCESS = 07021;
    //预约书本失败
    private static final int RESERVE_BOOK_FAIL = 07020;
    //    private static final int ADD_BOOK = 0702;               //添加新书
//    private static final int UPDATE_BOOK_DETAIL = 0703;     //更新书本信息
//    private static final int DELETE_BOOK = 0704;            //删除书本信息
    //服务器返回的书本详情数据
    private JSONObject jsonObjectBookDetail = null;


    static class MyHandler extends Handler {
        private WeakReference<LBookDetailActivity> lBookDetailActivity;
        public MyHandler(WeakReference<LBookDetailActivity> lBookDetailActivity){
            this.lBookDetailActivity = lBookDetailActivity;
        }
        public void handleMessage(Message msg){

        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @BindView(R.id.l_bookDetail_title)
    public TextView tTitle;
    @BindView(R.id.l_bookName)
    public EditText tName;
    @BindView(R.id.l_bookAuthor)
    public EditText tAuthor;
    @BindView(R.id.l_bookIsbn)
    public EditText tIsbn;
    @BindView(R.id.l_bookLibrary)
    public Spinner spinnerLibrary;
    @BindView(R.id.l_bookLocation)
    public EditText tLocation;
    @BindView(R.id.l_bookCallNumber)
    public EditText tCallNumber;
    @BindView(R.id.l_bookTheme)
    public EditText tTheme;
    @BindView(R.id.l_bookDescription)
    public EditText tDesc;
    @BindView(R.id.l_bookFirst)
    public Spinner spinnerFirst;
    @BindView(R.id.l_bookThird)
    public Spinner spinnerThird;
    @BindView(R.id.l_bookType)
    public Spinner spinnerType;
    @BindView(R.id.l_bookHouse)
    public EditText tHouse;
    @BindView(R.id.l_bookDate)
    public EditText tDate;
    @BindView(R.id.l_bookHot)
    public EditText tHot;
    @BindView(R.id.l_bookState)
    public EditText tState;

    //当前是否启动了编辑
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_l_book_detail);
        ButterKnife.bind(this);
        ActivityManager.getInstance().addActivity(this);
        //判断当前页面的打开方式
        String action = null;
        action = getIntent().getStringExtra("action");
        if(action!=null){
            if(action.equals("addBook")){
                isEdit = true;
                tTitle.setText("添加新书");
            }else if(action.equals("checkBook")){
                isEdit = false;
                tTitle.setText("书籍详情");
            }else {
                //未知操作
                isEdit = false;
            }
        }else {
            //未知操作

        }



    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    @Override
    public void success(Response response, int code) throws IOException {
        //服务器返回的数据
        String result = null;
        //获取服务器响应字符串
        result = response.body().string().trim();
        switch (code) {
            case GET_BOOK_DETAIL:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String tip = null;
                    if(message.equals("查询成功！")){
                        tip = jsonObject.getString("tip");
                        if(tip == null || tip.equals("null")){
                            //查询成功，获取书籍数据，通知主线程渲染前端
                            jsonObjectBookDetail = jsonObject.getJSONObject("object");
                            myHandler.sendEmptyMessage(GET_BOOK_DETAIL_FILL);
                            break;
                        }
                    } else {
                        String c = jsonObject.getString("code");
                        tip = jsonObject.getString("tip");
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("code",c);
                        data.putString("tip",tip);
                        data.putString("message",message);
                        msg.setData(data);
                        msg.what = GET_BOOK_DETAIL_FILL;
                        myHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                    e.printStackTrace();
                }
                break;
            case RESERVE_BOOK:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String c = jsonObject.getString("code");
                    String tip = jsonObject.getString("tip");
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("code",c);
                    data.putString("tip",tip);
                    data.putString("message",message);
                    msg.setData(data);
                    if(message.equals("预约成功！")){
                        msg.what = RESERVE_BOOK_SUCCESS;
                    }else {
                        msg.what = RESERVE_BOOK_FAIL;
                    }
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
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
