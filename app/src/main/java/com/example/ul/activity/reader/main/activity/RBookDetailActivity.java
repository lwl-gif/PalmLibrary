package com.example.ul.activity.reader.main.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ul.R;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Response;
/**
 * @Author:Wallace
 * @Description:读者查看书籍详情的活动
 * @Date:Created in 16:20 2021/3/22
 * @Modified By:
 */
public class RBookDetailActivity extends AppCompatActivity implements HttpUtil.MyCallback {

    private static final String TAG = "RBookDetailActivity";
    //自定义消息代码
    //未知请求
    private static final int UNKNOWN_REQUEST = 0600;
    //请求失败
    private static final int REQUEST_FAIL = 06000;
    //请求成功，但子线程解析数据失败
    private static final int REQUEST_BUT_FAIL_READ_DATA = 06001;
    //获取书本详情
    private static final int GET_BOOK_DETAIL = 0601;
    //获取书本详情成功，有数据需要渲染
    private static final int GET_BOOK_DETAIL_FILL = 06011;
    //获取书本详情失败或无数据需要渲染
    private static final int GET_BOOK_DETAIL_NOT_FILL = 06010;
    //预约书本
    private static final int RESERVE_BOOK = 0602;
    //预约书本成功
    private static final int RESERVE_BOOK_SUCCESS = 06021;
    //预约书本失败
    private static final int RESERVE_BOOK_FAIL = 06020;
    //    private static final int ADD_BOOK = 0602;               //添加新书
//    private static final int UPDATE_BOOK_DETAIL = 0603;     //更新书本信息
//    private static final int DELETE_BOOK = 0604;            //删除书本信息
    //服务器返回的书本详情数据
    private JSONObject jsonObjectBookDetail = null;

    //当前书本id
    private String id = null;
    //界面组件
    //标题，Id，名称，作者，Isbn，所属馆，馆藏地点，索书号，主题，详情，一级分类，二级分类，文献类型，出版社，出版日期，热度，状态
    private TextView tTitle,tId,tName,tAuthor,tIsbn,tLibrary,tLocation,tCallNumber,tTheme,tDesc,tFirst,tThird,tType,tHouse,tDate,tHot,tState;

    private Button bBack,bReserve;

    static class MyHandler extends Handler {
        private WeakReference<RBookDetailActivity> rBookDetailActivity;
        public MyHandler(WeakReference<RBookDetailActivity> rBookDetailActivity){
            this.rBookDetailActivity = rBookDetailActivity;
        }
        public void handleMessage(Message msg){
            int what = msg.what;
            if(what == UNKNOWN_REQUEST) {
                Toast.makeText(rBookDetailActivity.get(),"未知请求，无法处理！",Toast.LENGTH_SHORT).show();
            }
            else if(what == REQUEST_FAIL){
                Toast.makeText(rBookDetailActivity.get(),"网络异常！",Toast.LENGTH_SHORT).show();
            }else if(what == REQUEST_BUT_FAIL_READ_DATA){
                Toast.makeText(rBookDetailActivity.get(),"子线程解析数据异常！",Toast.LENGTH_SHORT).show();
            } else if (what == GET_BOOK_DETAIL_FILL) {
                try {
                    rBookDetailActivity.get().id = rBookDetailActivity.get().jsonObjectBookDetail.getString("id");
                    rBookDetailActivity.get().tId.setText("No." + rBookDetailActivity.get().id);
                    rBookDetailActivity.get().tName.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("name"));
                    rBookDetailActivity.get().tAuthor.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("isbn"));
                    rBookDetailActivity.get().tIsbn.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("author"));
                    rBookDetailActivity.get().tLibrary.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("library"));
                    rBookDetailActivity.get().tLocation.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("location"));
                    rBookDetailActivity.get().tCallNumber.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("callNumber"));
                    rBookDetailActivity.get().tTheme.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("theme"));
                    rBookDetailActivity.get().tDesc.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("description"));
                    rBookDetailActivity.get().tType.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("type"));
                    rBookDetailActivity.get().tHouse.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("house"));
                    rBookDetailActivity.get().tHot.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("hot"));
                    rBookDetailActivity.get().tState.setText(rBookDetailActivity.get().jsonObjectBookDetail.getString("state"));
                    JSONObject belong = new JSONObject(rBookDetailActivity.get().jsonObjectBookDetail.getString("classification"));
                    rBookDetailActivity.get().tFirst.setText(belong.getString("first"));
                    rBookDetailActivity.get().tThird.setText(belong.getString("third"));
                    String d = rBookDetailActivity.get().jsonObjectBookDetail.getString("date");
                    if(d == null || d.equals("null") || d.equals("")){
                        rBookDetailActivity.get().tDate.setText(null);
                    }else {
                        Long l = Long.parseLong(d);
                        Date date = new Date(l);
                        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
                        String tvDate = format.format(date);
                        rBookDetailActivity.get().tDate.setText(tvDate);
                    }

                } catch (JSONException e) {
                    Toast.makeText(rBookDetailActivity.get(), "主线程解析数据时异常！", Toast.LENGTH_SHORT).show();
                }
            } else {
                Bundle data = msg.getData();
                String code = data.getString("code");
                String message = data.getString("message");
                String tip = data.getString("tip");
                if (what == RESERVE_BOOK_SUCCESS) {
                    Toast.makeText(rBookDetailActivity.get(), message + tip, Toast.LENGTH_LONG).show();
                }else {
                    View view = View.inflate(rBookDetailActivity.get(),R.layout.dialog_view,null);
                    TextView tvFrom = view.findViewById(R.id.dialog_from);
                    tvFrom.setText(TAG);
                    TextView tvCode = view.findViewById(R.id.dialog_code);
                    tvCode.setText(code);
                    TextView tvMessage = view.findViewById(R.id.dialog_message);
                    tvMessage.setText(message);
                    TextView tvTip = view.findViewById(R.id.dialog_tip);
                    tvTip.setText(tip);
                    DialogUtil.showDialog(rBookDetailActivity.get(),view);
                }
            }
        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        //判断传进来的id是否为空，若为空，则说明是添加新书，若不为空则说明是查看书本详情
        id = this.getIntent().getStringExtra("id");
        if(id != null){
            //发送获取书本详情的请求，获取书本详情
            //获取token
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            String token = userInfo.getToken();
            //使用Map封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", id);
            String url = HttpUtil.BASE_URL + "book/selectAllById";
            // 拼接请求参数
            StringBuffer buffer = new StringBuffer(url);
            buffer.append('?');
            for (HashMap.Entry<String, String> entry : hashMap.entrySet()) {
                buffer.append(entry.getKey());
                buffer.append('=');
                buffer.append(entry.getValue());
                buffer.append('&');
            }
            buffer.deleteCharAt(buffer.length() - 1);
            url = buffer.toString();
            HttpUtil.getRequest(token,url,this,GET_BOOK_DETAIL);
            id = null;
        }else {
            DialogUtil.showDialog(this,"无法获取书本详情！",false);
        }
        setContentView(R.layout.activity_r_book_detail);
        //组件初始化
        tTitle = findViewById(R.id.bookDetail_title);
        tTitle.setText("查看详情");
        tId = findViewById(R.id.bookId);
        tName = findViewById(R.id.bookName);
        tAuthor = findViewById(R.id.bookAuthor);
        tIsbn = findViewById(R.id.bookIsbn);
        tLibrary = findViewById(R.id.bookLibrary);
        tLocation = findViewById(R.id.bookLocation);
        tCallNumber = findViewById(R.id.bookCallNumber);
        tTheme = findViewById(R.id.bookTheme);
        tDesc = findViewById(R.id.bookDescription);
        tFirst = findViewById(R.id.bookFirst);
        tThird = findViewById(R.id.bookThird);
        tType = findViewById(R.id.bookType);
        tHouse = findViewById(R.id.bookHouse);
        tDate = findViewById(R.id.bookDate);
        tHot = findViewById(R.id.bookHot);
        tState = findViewById(R.id.bookState);
        bBack = findViewById(R.id.bookDetail_back);
        bBack.setOnClickListener(view -> {
            finish();
        });
        bReserve = findViewById(R.id.bookDetail_reserve);
        bReserve.setOnClickListener(view -> {
            //发送获取书本详情的请求，获取书本详情
            //获取token
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            String token = userInfo.getToken();
            //使用Map封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", id);
            String url = HttpUtil.BASE_URL + "reservation/reserveBook";
            HttpUtil.postRequest(token,url,hashMap,this,RESERVE_BOOK);
        });
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