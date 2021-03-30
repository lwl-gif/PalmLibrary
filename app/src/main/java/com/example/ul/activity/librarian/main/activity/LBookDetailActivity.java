package com.example.ul.activity.librarian.main.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @Author:Wallace
 * @Description:管理员有两种方式进入到该页面(通过传入的id是否为null来判断)：
 * 1.添加新书时，打开该页面，数据为空，可以填入新书本的信息
 * 2.扫码或者点击查看书本详情时，进入该页面，获取书本的详情，可以修改书本信息
 * @Date:Created in 22:25 2021/3/28
 * @Modified By:
 */
public class LBookDetailActivity extends AppCompatActivity implements HttpUtil.MyCallback {

    private static final String TAG = "LBookDetailActivity";
    //自定义消息代码
    //未知请求
    private static final int UNKNOWN_REQUEST = 800;
    //请求失败
    private static final int REQUEST_FAIL = 8000;
    //请求成功，但子线程解析数据失败
    private static final int REQUEST_BUT_FAIL_READ_DATA = 8001;
    //获取书本详情
    private static final int GET_BOOK_DETAIL = 801;
    //获取书本详情成功，有数据需要渲染
    private static final int GET_BOOK_DETAIL_FILL = 8011;
    //获取书本详情失败或无数据需要渲染
    private static final int GET_BOOK_DETAIL_NOT_FILL = 8010;
    //添加书本
    private static final int ADD_BOOK = 802;
    //添加书本成功
    private static final int ADD_BOOK_SUCCESS = 8021;
    //添加书本失败
    private static final int ADD_BOOK_FAIL = 8020;
    //更新书本
    private static final int UPDATE_BOOK = 803;     //更新书本信息
    //更新成功
    private static final int UPDATE_BOOK_SUCCEED = 803;     //更新书本信息
    //更新失败
    private static final int UPDATE_BOOK_FAIL = 803;     //更新书本信息
    //删除书本信息
    private static final int DELETE_BOOK = 804;

    //服务器返回的书本详情数据
    private JSONObject jsonObjectBookDetail = null;

    @BindView(R.id.l_bookDetail_title)
    public TextView tTitle;
    @BindView(R.id.l_bookId)
    public TextView tId;
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
    public TextView tState;

    private Button bBack,bEdit,bSubmit;

    static class MyHandler extends Handler {
        private WeakReference<LBookDetailActivity> lBookDetailActivity;
        public MyHandler(WeakReference<LBookDetailActivity> lBookDetailActivity){
            this.lBookDetailActivity = lBookDetailActivity;
        }
        public void handleMessage(Message msg){
            int what = msg.what;
            if(what == UNKNOWN_REQUEST) {
                Toast.makeText(lBookDetailActivity.get(),"未知请求，无法处理！",Toast.LENGTH_SHORT).show();
            }
            else if(what == REQUEST_FAIL){
                Toast.makeText(lBookDetailActivity.get(),"网络异常！",Toast.LENGTH_SHORT).show();
            }else if(what == REQUEST_BUT_FAIL_READ_DATA){
                Toast.makeText(lBookDetailActivity.get(),"子线程解析数据异常！",Toast.LENGTH_SHORT).show();
            } else if (what == GET_BOOK_DETAIL_FILL) {
                try {
                    lBookDetailActivity.get().id = lBookDetailActivity.get().jsonObjectBookDetail.getString("id");
                    lBookDetailActivity.get().tId.setText("No." + lBookDetailActivity.get().id);
                    lBookDetailActivity.get().tName.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("name"));
                    lBookDetailActivity.get().tAuthor.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("isbn"));
                    lBookDetailActivity.get().tIsbn.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("author"));
//                    lBookDetailActivity.get().tLibrary.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("library"));
                    lBookDetailActivity.get().tLocation.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("location"));
                    lBookDetailActivity.get().tCallNumber.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("callNumber"));
                    lBookDetailActivity.get().tTheme.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("theme"));
                    lBookDetailActivity.get().tDesc.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("description"));
//                    lBookDetailActivity.get().tType.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("type"));
                    lBookDetailActivity.get().tHouse.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("house"));
                    lBookDetailActivity.get().tHot.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("hot"));
                    lBookDetailActivity.get().tState.setText(lBookDetailActivity.get().jsonObjectBookDetail.getString("state"));
                    JSONObject belong = new JSONObject(lBookDetailActivity.get().jsonObjectBookDetail.getString("classification"));
//                    lBookDetailActivity.get().tFirst.setText(belong.getString("first"));
//                    lBookDetailActivity.get().tThird.setText(belong.getString("third"));
                    String d = lBookDetailActivity.get().jsonObjectBookDetail.getString("date");
                    if(d == null || d.equals("null") || d.equals("")){
                        lBookDetailActivity.get().tDate.setText(null);
                    }else {
                        Long l = Long.parseLong(d);
                        Date date = new Date(l);
                        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
                        String tvDate = format.format(date);
                        lBookDetailActivity.get().tDate.setText(tvDate);
                    }
                } catch (JSONException e) {
                    Toast.makeText(lBookDetailActivity.get(), "主线程解析数据时异常！", Toast.LENGTH_SHORT).show();
                }
            } else {
                Bundle data = msg.getData();
                String code = data.getString("code");
                String message = data.getString("message");
                String tip = data.getString("tip");
//                if (what == RESERVE_BOOK_SUCCESS) {
//                    Toast.makeText(lBookDetailActivity.get(), message + tip, Toast.LENGTH_LONG).show();
//                }else {
//                    View view = View.inflate(lBookDetailActivity.get(),R.layout.dialog_view,null);
//                    TextView tvFrom = view.findViewById(R.id.dialog_from);
//                    tvFrom.setText(TAG);
//                    TextView tvCode = view.findViewById(R.id.dialog_code);
//                    tvCode.setText(code);
//                    TextView tvMessage = view.findViewById(R.id.dialog_message);
//                    tvMessage.setText(message);
//                    TextView tvTip = view.findViewById(R.id.dialog_tip);
//                    tvTip.setText(tip);
//                    DialogUtil.showDialog(lBookDetailActivity.get(),view);
//                }
            }
        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));

    //当前书本id
    private String id = null;

    //当前是否启动了编辑
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_l_book_detail);
        ButterKnife.bind(this);
        bBack = findViewById(R.id.l_bookDetail_back);
        bBack.setOnClickListener(view -> {
            ActivityManager.getInstance().removeActivity(this);
            finish();
        });
        bEdit = findViewById(R.id.l_bookDetail_edit);
        bEdit.setOnClickListener(view -> {
            //如果当前可编辑
            if(isEdit == true){
                isEdit = false;
            }else {
                isEdit = true;
            }
            isAllowEdit();
        });
        bSubmit = findViewById(R.id.l_bookDetail_submit);
        //判断传进来的id是否为空，若为空，则说明是添加新书，若不为空则说明是查看书本详情
        id = this.getIntent().getStringExtra("id");
        if(id != null){
            //发送查询书籍详情的请求
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
            //再发送一个获取图书分类的请求
            id = null;
            isEdit = false;
            tTitle.setText("书籍详情");
        }else {
            isEdit = true;
            tTitle.setText("添加新书");
        }
        isAllowEdit();
        //提交按钮绑定更新书籍的请求
        bSubmit.setOnClickListener(view -> {
            //获取token
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            String token = userInfo.getToken();
            //使用Map封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id",tId.getText().toString().trim());
            hashMap.put("name",tName.getText().toString().trim());
            hashMap.put("author",tAuthor.getText().toString().trim());
            hashMap.put("isbn",tIsbn.getText().toString().trim());
//            hashMap.put("library",tName.getText().toString().trim());
            hashMap.put("location",tLocation.getText().toString().trim());
            hashMap.put("callNumber",tCallNumber.getText().toString().trim());
            hashMap.put("theme",tTheme.getText().toString().trim());
            hashMap.put("desc",tDate.getText().toString().trim());
//            hashMap.put("first",tName.getText().toString().trim());
//            hashMap.put("third",tName.getText().toString().trim());
//            hashMap.put("type",tName.getText().toString().trim());
            hashMap.put("house",tHouse.getText().toString().trim());
            hashMap.put("date",tDate.getText().toString().trim());
            hashMap.put("hot",tHot.getText().toString().trim());
            hashMap.put("state",tState.getText().toString().trim());
            if(id != null){     //绑定更新图书请求
                String url = HttpUtil.BASE_URL + "book/updateBook";
//                HttpUtil.getRequest(token,url,this,GET_BOOK_DETAIL);
            }else {             //绑定添加图书请求
                String url = HttpUtil.BASE_URL + "book/addBook";
                HttpUtil.postRequest(token,url,hashMap,this,ADD_BOOK);
            }
        });
    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    //启动或禁止编辑
    private void isAllowEdit(){
        if(!isEdit){
            bEdit.setText(R.string.edit);
        }else {
            bEdit.setText(R.string.cancel);
        }
        //isEdit若为false，则所有EditText和Spinner不可获取焦点,不可点击，不可编辑
        tName.setFocusable(isEdit);
        tName.setFocusableInTouchMode(isEdit);
        tName.setClickable(isEdit);
        tName.setEnabled(isEdit);
        tName.requestFocus();

        tAuthor.setFocusable(isEdit);
        tAuthor.setFocusableInTouchMode(isEdit);
        tAuthor.setClickable(isEdit);
        tAuthor.setEnabled(isEdit);

        tIsbn.setFocusable(isEdit);
        tIsbn.setFocusableInTouchMode(isEdit);
        tIsbn.setClickable(isEdit);
        tIsbn.setEnabled(isEdit);

        spinnerLibrary.setFocusable(isEdit);
        spinnerLibrary.setFocusableInTouchMode(isEdit);
        spinnerLibrary.setClickable(isEdit);
        spinnerLibrary.setEnabled(isEdit);

        tLocation.setFocusable(isEdit);
        tLocation.setFocusableInTouchMode(isEdit);
        tLocation.setClickable(isEdit);
        tLocation.setEnabled(isEdit);

        tCallNumber.setFocusable(isEdit);
        tCallNumber.setFocusableInTouchMode(isEdit);
        tCallNumber.setClickable(isEdit);
        tCallNumber.setEnabled(isEdit);

        tTheme.setFocusable(isEdit);
        tTheme.setFocusableInTouchMode(isEdit);
        tTheme.setClickable(isEdit);
        tTheme.setEnabled(isEdit);

        tDesc.setFocusable(isEdit);
        tDesc.setFocusableInTouchMode(isEdit);
        tDesc.setClickable(isEdit);
        tDesc.setEnabled(isEdit);

        spinnerFirst.setFocusable(isEdit);
        spinnerFirst.setFocusableInTouchMode(isEdit);
        spinnerFirst.setClickable(isEdit);
        spinnerFirst.setEnabled(isEdit);

        spinnerThird.setFocusable(isEdit);
        spinnerThird.setFocusableInTouchMode(isEdit);
        spinnerThird.setClickable(isEdit);
        spinnerThird.setEnabled(isEdit);

        spinnerType.setFocusable(isEdit);
        spinnerType.setFocusableInTouchMode(isEdit);
        spinnerType.setClickable(isEdit);
        spinnerType.setEnabled(isEdit);

        tHouse.setFocusable(isEdit);
        tHouse.setFocusableInTouchMode(isEdit);
        tHouse.setClickable(isEdit);
        tHouse.setEnabled(isEdit);

        tDate.setFocusable(isEdit);
        tDate.setFocusableInTouchMode(isEdit);
        tDate.setClickable(isEdit);
        tDate.setEnabled(isEdit);

        tHot.setFocusable(isEdit);
        tHot.setFocusableInTouchMode(isEdit);
        tHot.setClickable(isEdit);
        tHot.setEnabled(isEdit);

//        tState.setFocusable(isEdit);
//        tState.setFocusableInTouchMode(isEdit);
//        tState.setClickable(isEdit);
//        tState.setEnabled(isEdit);
    }

    @Override
    public void success(Response response, int code) throws IOException {
        //获取服务器响应字符串
        String result = response.body().string().trim();
        switch (code) {
            case GET_BOOK_DETAIL:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String tip = null;
                    if(message.equals("查询成功！")){
                        tip = jsonObject.getString("tip");
                        if(tip.equals("null")){
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
            case ADD_BOOK:
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
                    if(message.equals("添加成功！")){
                        msg.what = ADD_BOOK_SUCCESS;
                    }else {
                        msg.what = ADD_BOOK_FAIL;
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
