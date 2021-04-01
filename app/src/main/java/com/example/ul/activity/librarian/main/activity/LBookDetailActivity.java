package com.example.ul.activity.librarian.main.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ul.R;
import com.example.ul.adapter.MySpinnerAdapter;
import com.example.ul.adapter.MySpinnerBelongAdapter;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
    private static final int UPDATE_BOOK = 803;
    //更新成功
    private static final int UPDATE_BOOK_SUCCEED = 8031;
    //更新失败
    private static final int UPDATE_BOOK_FAIL = 8030;
    //删除书本信息
    private static final int DELETE_BOOK = 804;
    private static final int DELETE_BOOK_SUCCEED = 8041;
    private static final int DELETE_BOOK_FAIL = 8040;
    //查询分类
    private static final int GET_TYPE = 805;
    //服务器返回的书本详情数据
    private JSONObject jsonObjectBookDetail = null;
    /**要填充到各个下拉列表中的内容*/
    private JSONArray jsonArrayLibrary,jsonArrayType;
    private List<String> firsts = new ArrayList<>();
    private List<List<String>> thirds = new ArrayList<>();
    /**当前图书馆/图书类别/文献类型*/
    private String library = "null", first = "null", third = "null", type = "null";
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
    @BindView(R.id.l_bookPrice)
    public EditText tPrice;
    @BindView(R.id.l_bookHot)
    public EditText tHot;
    @BindView(R.id.l_bookState)
    public TextView tState;
    private Button bBack,bEdit,bSubmit,bDelete;
    /**当前书本id*/
    private String id = null;
    /**当前是否启动了编辑*/
    private boolean writing = false;
    static class MyHandler extends Handler {
        private WeakReference<LBookDetailActivity> lBookDetailActivity;
        public MyHandler(WeakReference<LBookDetailActivity> lBookDetailActivity){
            this.lBookDetailActivity = lBookDetailActivity;
        }
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            if(what == UNKNOWN_REQUEST) {
                Toast.makeText(lBookDetailActivity.get(),"未知请求，无法处理！",Toast.LENGTH_SHORT).show();
            }
            else if(what == REQUEST_FAIL){
                Toast.makeText(lBookDetailActivity.get(),"网络异常！",Toast.LENGTH_SHORT).show();
            }else if(what == REQUEST_BUT_FAIL_READ_DATA){
                Toast.makeText(lBookDetailActivity.get(),"子线程解析数据异常！",Toast.LENGTH_SHORT).show();
            }else if(what == GET_TYPE){
                lBookDetailActivity.get().fillSpinnerData();
            }else if (what == GET_BOOK_DETAIL_FILL) {
                lBookDetailActivity.get().fillBookDetail();
            } else {
                Bundle data = msg.getData();
                String code = data.getString("code");
                String message = data.getString("message");
                if(what == ADD_BOOK_SUCCESS){
                    lBookDetailActivity.get().clear();
                    Toast.makeText(lBookDetailActivity.get(), message, Toast.LENGTH_LONG).show();
                }else if(what == UPDATE_BOOK_SUCCEED){
                    Toast.makeText(lBookDetailActivity.get(), message, Toast.LENGTH_LONG).show();
                }else if(what == DELETE_BOOK_SUCCEED){
                    Toast.makeText(lBookDetailActivity.get(), message, Toast.LENGTH_LONG).show();
                    lBookDetailActivity.get().finish();
                }
                else {
                    String tip = data.getString("tip");
                    View view = View.inflate(lBookDetailActivity.get(),R.layout.dialog_view,null);
                    TextView tvFrom = view.findViewById(R.id.dialog_from);
                    tvFrom.setText(TAG);
                    TextView tvCode = view.findViewById(R.id.dialog_code);
                    tvCode.setText(code);
                    TextView tvMessage = view.findViewById(R.id.dialog_message);
                    tvMessage.setText(message);
                    TextView tvTip = view.findViewById(R.id.dialog_tip);
                    tvTip.setText(tip);
                    DialogUtil.showDialog(lBookDetailActivity.get(),view);
                }
            }
        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_l_book_detail);
        ButterKnife.bind(this);
        //先发送获取分类的请求
        getType();
        //初始化
        init();
    }

    private void init() {
        /**
         * @Author:Wallace
         * @Description:根据有无id值，有两种初始化方式，对应新添书籍和书籍详情两种打开方式。
         * @Date:Created in 12:48 2021/3/31
         * @Modified By:
          * @param
         * @return: void
         */
        bBack = findViewById(R.id.l_bookDetail_back);
        bBack.setOnClickListener(view -> {
            finish();
        });
        bEdit = findViewById(R.id.l_bookDetail_edit);
        bEdit.setOnClickListener(view -> {
            //如果当前可编辑
            if(writing == true){
                writing = false;
            }else {
                writing = true;
            }
            isAllowEdit();
        });
        bSubmit = findViewById(R.id.l_bookDetail_submit);
        //判断传进来的id是否为空，若为空，则说明是添加新书，若不为空则说明是查看书本详情
        id = this.getIntent().getStringExtra("id");
        if(id != null){
            writing = false;
            tTitle.setText("书籍详情");
            bSubmit.setText(R.string.update);
            bDelete = findViewById(R.id.l_bookDetail_delete);
            bDelete.setVisibility(View.VISIBLE);
            bDelete.setOnClickListener(view -> {
                //绑定删除请求
                //获取token
                UserManager userManager = UserManager.getInstance();
                UserInfo userInfo = userManager.getUserInfo(this);
                String token = userInfo.getToken();
                //使用Map封装请求参数
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id",this.id);
                String url = HttpUtil.BASE_URL + "book/deleteBookById";
                url = HttpUtil.newUrl(url,hashMap);
                HttpUtil.deleteRequest(token,url,this,DELETE_BOOK);
            });
        }else {
            writing = true;
            tTitle.setText("添加新书");
            tState.setText("在馆");
            bSubmit.setText(R.string.add);
        }
        isAllowEdit();
        //提交按钮绑定请求
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
            hashMap.put("library",library);
            hashMap.put("location",tLocation.getText().toString().trim());
            hashMap.put("callNumber",tCallNumber.getText().toString().trim());
            hashMap.put("theme",tTheme.getText().toString().trim());
            hashMap.put("desc",tDesc.getText().toString().trim());
            hashMap.put("first",first);
            hashMap.put("third",third);
            hashMap.put("typeName",type);
            hashMap.put("house",tHouse.getText().toString().trim());
            hashMap.put("date",tDate.getText().toString().trim());
            hashMap.put("price",tPrice.getText().toString().trim());
            hashMap.put("hot",tHot.getText().toString().trim());
            hashMap.put("state",tState.getText().toString().trim());
            if(id != null){     //绑定更新图书请求
                String url = HttpUtil.BASE_URL + "book/updateBook";
                HttpUtil.putRequest(token,url,hashMap,this,UPDATE_BOOK);
            }else {             //绑定添加图书请求
                String url = HttpUtil.BASE_URL + "book/addBook";
                HttpUtil.postRequest(token,url,hashMap,this,ADD_BOOK);
            }
        });
    }

    private void clear() {
        tName.setText(null);
        tAuthor.setText(null);
        tIsbn.setText(null);
        tLocation.setText(null);
        tCallNumber.setText(null);
        tTheme.setText(null);
        tDesc.setText(null);
        tHouse.setText(null);
        tDate.setText("1970-01-01");
        tPrice.setText("0.00");
        tHot.setText("0");
        spinnerLibrary.setSelection(0,true);
        spinnerType.setSelection(0,true);
        spinnerFirst.setSelection(0,true);
        spinnerThird.setSelection(0,true);
    }

    private void getType(){
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        String token = userInfo.getToken();
        //使用Map封装请求参数
        HashMap<String, String> hashMap = new HashMap<>();
        String url = HttpUtil.BASE_URL + "book/getDetailType";
        HttpUtil.getRequest(token,url,this,GET_TYPE);
    }

    private void fillSpinnerData() {
        /**
         * @Author:Wallace
         * @Description:为各个Spinner填充信息及绑定选中事件
         * @Date:Created in 13:16 2021/3/31
         * @Modified By:
         * @param
         * @return: void
         */
        MySpinnerAdapter sALibrary = new MySpinnerAdapter(this,jsonArrayLibrary);
        MySpinnerAdapter sAType = new MySpinnerAdapter(this,jsonArrayType);
        spinnerLibrary.setAdapter(sALibrary);
        spinnerType.setAdapter(sAType);
        MySpinnerBelongAdapter mySpinnerBelongAdapter = new MySpinnerBelongAdapter(this,firsts);
        spinnerFirst.setAdapter(mySpinnerBelongAdapter);
        //绑定事件
        spinnerLibrary.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                library = (String) spinnerLibrary.getItemAtPosition(i);
                Toast.makeText(LBookDetailActivity.this,"您选择了"+library,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                library = "null";
            }
        });
        spinnerType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                type = (String) spinnerType.getItemAtPosition(i);
                Toast.makeText(LBookDetailActivity.this,"您选择了"+type,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                type = "null";
            }
        });
        //根据spinnerFirst选的不同来动态渲染spinnerThird
        spinnerFirst.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                first = (String) spinnerFirst.getItemAtPosition(i);
                Toast.makeText(LBookDetailActivity.this,"您选择了"+first,Toast.LENGTH_SHORT).show();
                //给spinnerThird赋值
                spinnerThird.setAdapter(new MySpinnerBelongAdapter(LBookDetailActivity.this,thirds.get(i)));
                spinnerThird.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        third = (String) spinnerThird.getItemAtPosition(i);
                        Toast.makeText(LBookDetailActivity.this,"您选择了"+third,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
//                        third = "null";
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                first = "null";
            }
        });
        //各列表第一项为默认选择值
        spinnerLibrary.setSelection(0,true);
        spinnerType.setSelection(0,true);
        spinnerFirst.setSelection(0,true);
        spinnerThird.setSelection(0,true);
        if(id == null){

        }else {
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
        }
    }

    private void fillBookDetail() {
        try {
            this.id = this.jsonObjectBookDetail.getString("id");
            this.tId.setText(this.id);
            this.tName.setText(this.jsonObjectBookDetail.getString("name"));
            this.tAuthor.setText(this.jsonObjectBookDetail.getString("isbn"));
            this.tIsbn.setText(this.jsonObjectBookDetail.getString("author"));
            //改变列表的默认值
            String tLibrary = this.jsonObjectBookDetail.getString("library");
            for(int i = 0;i < spinnerLibrary.getCount(); i++){
                String s = (String) spinnerLibrary.getItemAtPosition(i);
                if(s != null && s.equals(tLibrary)){
                    spinnerLibrary.setSelection(i,true);
                    break;
                }
            }
            this.tLocation.setText(this.jsonObjectBookDetail.getString("location"));
            this.tCallNumber.setText(this.jsonObjectBookDetail.getString("callNumber"));
            this.tTheme.setText(this.jsonObjectBookDetail.getString("theme"));
            this.tDesc.setText(this.jsonObjectBookDetail.getString("description"));
            //改变列表的默认值
            String tType = this.jsonObjectBookDetail.getString("typeName");
            for(int i = 0;i < spinnerType.getCount(); i++){
                String s = (String) spinnerType.getItemAtPosition(i);
                if(s.equals(tType)){
                    spinnerType.setSelection(i,true);
                    break;
                }
            }
            this.tHouse.setText(this.jsonObjectBookDetail.getString("house"));
            this.tHot.setText(this.jsonObjectBookDetail.getString("hot"));
            this.tState.setText(this.jsonObjectBookDetail.getString("state"));
            this.tPrice.setText(this.jsonObjectBookDetail.getString("price"));
            JSONObject belong = new JSONObject(this.jsonObjectBookDetail.getString("classification"));
            //改变列表的默认值
            String tFirst = belong.getString("first");
            for(int i = 0;i < spinnerFirst.getCount(); i++){
                String s = (String) spinnerFirst.getItemAtPosition(i);
                if(!(s == null) && s.equals(tFirst)){
                    spinnerFirst.setSelection(i,true);
                    break;
                }
            }
            //改变列表的默认值
            String tThird = belong.getString("third");
            for(int i = 0;i < spinnerThird.getCount(); i++){
                String s = (String) spinnerThird.getItemAtPosition(i);
                if(!(s == null) && s.equals(tThird)){
                    spinnerThird.setSelection(i,true);
                    break;
                }
            }
            String d = this.jsonObjectBookDetail.getString("date");
            String n = "null";
            if(d == null || n.equals(d) || "".equals(d)){
                this.tDate.setText(null);
            }else {
                Long l = Long.parseLong(d);
                Date date = new Date(l);
                SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
                String tvDate = format.format(date);
                this.tDate.setText(tvDate);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "主线程解析数据时异常！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    //启动或禁止编辑
    private void isAllowEdit(){
        if(!writing){
            bEdit.setText(R.string.edit);
        }else {
            bEdit.setText(R.string.cancel);
        }
        //isEdit若为false，则所有EditText和Spinner不可获取焦点,不可点击，不可编辑
        tName.setFocusable(writing);
        tName.setFocusableInTouchMode(writing);
        tName.setClickable(writing);
        tName.setEnabled(writing);
        tName.requestFocus();

        tAuthor.setFocusable(writing);
        tAuthor.setFocusableInTouchMode(writing);
        tAuthor.setClickable(writing);
        tAuthor.setEnabled(writing);

        tIsbn.setFocusable(writing);
        tIsbn.setFocusableInTouchMode(writing);
        tIsbn.setClickable(writing);
        tIsbn.setEnabled(writing);

        spinnerLibrary.setFocusable(writing);
//        spinnerLibrary.setFocusableInTouchMode(writing);
        spinnerLibrary.setClickable(writing);
        spinnerLibrary.setEnabled(writing);

        tLocation.setFocusable(writing);
        tLocation.setFocusableInTouchMode(writing);
        tLocation.setClickable(writing);
        tLocation.setEnabled(writing);

        tCallNumber.setFocusable(writing);
        tCallNumber.setFocusableInTouchMode(writing);
        tCallNumber.setClickable(writing);
        tCallNumber.setEnabled(writing);

        tTheme.setFocusable(writing);
        tTheme.setFocusableInTouchMode(writing);
        tTheme.setClickable(writing);
        tTheme.setEnabled(writing);

        tDesc.setFocusable(writing);
        tDesc.setFocusableInTouchMode(writing);
        tDesc.setClickable(writing);
        tDesc.setEnabled(writing);

        spinnerFirst.setFocusable(writing);
//        spinnerFirst.setFocusableInTouchMode(writing);
        spinnerFirst.setClickable(writing);
        spinnerFirst.setEnabled(writing);

        spinnerThird.setFocusable(writing);
//        spinnerThird.setFocusableInTouchMode(writing);
        spinnerThird.setClickable(writing);
        spinnerThird.setEnabled(writing);

        spinnerType.setFocusable(writing);
//        spinnerType.setFocusableInTouchMode(writing);
        spinnerType.setClickable(writing);
        spinnerType.setEnabled(writing);

        tHouse.setFocusable(writing);
        tHouse.setFocusableInTouchMode(writing);
        tHouse.setClickable(writing);
        tHouse.setEnabled(writing);

        tDate.setFocusable(writing);
        tDate.setFocusableInTouchMode(writing);
        tDate.setClickable(writing);
        tDate.setEnabled(writing);

        tPrice.setFocusable(writing);
        tPrice.setFocusableInTouchMode(writing);
        tPrice.setClickable(writing);
        tPrice.setEnabled(writing);

        tHot.setFocusable(writing);
        tHot.setFocusableInTouchMode(writing);
        tHot.setClickable(writing);
        tHot.setEnabled(writing);

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
            case GET_TYPE:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    //将整个信息拆分
                    JSONArray jsonArraySpinners = jsonObject.getJSONArray("dataArray");
                    jsonArrayLibrary = jsonArraySpinners.getJSONArray(0);
                    JSONArray jsonArray0 = jsonArraySpinners.getJSONArray(1);
                    jsonArrayType = jsonArraySpinners.getJSONArray(2);
                    firsts.clear();
                    thirds.clear();
                    for(int i = 0; i < jsonArray0.length();i ++){
                        JSONArray belong = jsonArray0.getJSONArray(i);
                        String first = belong.getString(0);
                        String third = belong.getString(1);
                        String[] arrayStr = third.split(",");
                        List<String> list = new ArrayList<String>(Arrays.asList(arrayStr));
                        Log.e(TAG, "list: = " + list);
                        firsts.add(first);
                        thirds.add(list);
                    }
                    //发消息通知主线程进行UI更新
                    myHandler.sendEmptyMessage(GET_TYPE);
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                }
                break;
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
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("code",c);
                    data.putString("message",message);
                    msg.setData(data);
                    if(message.equals("添加成功！")){
                        msg.what = ADD_BOOK_SUCCESS;
                    }else {
                        String tip = jsonObject.getString("tip");
                        data.putString("tip",tip);
                        msg.what = ADD_BOOK_FAIL;
                    }
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                }
                break;
            case UPDATE_BOOK:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String c = jsonObject.getString("code");
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("code",c);
                    data.putString("message",message);
                    msg.setData(data);
                    if(message.equals("更新成功！")){
                        msg.what = UPDATE_BOOK_SUCCEED;
                    }else {
                        String tip = jsonObject.getString("tip");
                        data.putString("tip",tip);
                        msg.what = UPDATE_BOOK_FAIL;
                    }
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                }
                break;
            case DELETE_BOOK:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String c = jsonObject.getString("code");
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("code",c);
                    data.putString("message",message);
                    msg.setData(data);
                    if(message.equals("删除成功！")){
                        msg.what = DELETE_BOOK_SUCCEED;
                    }else {
                        String tip = jsonObject.getString("tip");
                        data.putString("tip",tip);
                        msg.what = DELETE_BOOK_FAIL;
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
