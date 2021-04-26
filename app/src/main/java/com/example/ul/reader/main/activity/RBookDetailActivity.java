package com.example.ul.reader.main.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ul.R;
import com.example.ul.activity.ShowPictureActivity;
import com.example.ul.adapter.ImagesOnlyReadAdapter;
import com.example.ul.callback.ImageAdapterItemListener;
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
import java.util.Date;
import java.util.HashMap;

import okhttp3.Response;
/**
 * @Author: Wallace
 * @Description: 读者查看书籍详情的活动
 * @Date: Created in 16:20 2021/3/22
 * @Modified By:
 */
public class RBookDetailActivity extends AppCompatActivity implements HttpUtil.MyCallback, ImageAdapterItemListener {

    private static final String TAG = "RBookDetailActivity";
    /**未知请求*/
    private static final int UNKNOWN_REQUEST = 600;
    /**请求失败*/
    private static final int REQUEST_FAIL = 6000;
    /**请求成功，但子线程解析数据失败*/
    private static final int REQUEST_BUT_FAIL_READ_DATA = 6001;
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
    private String id = null;
    private TextView tId;
    private TextView tName;
    private TextView tAuthor;
    private TextView tIsbn;
    private TextView tLibrary;
    private TextView tLocation;
    private TextView tCallNumber;
    private TextView tTheme;
    private TextView tDesc;
    private TextView tFirst;
    private TextView tThird;
    private TextView tType;
    private TextView tHouse;
    private TextView tDate;
    private TextView tPrice;
    private TextView tHot;
    private TextView tState;
    private ImagesOnlyReadAdapter imagesOnlyReadAdapter;
    private RecyclerView recyclerView;

    static class MyHandler extends Handler {
        private WeakReference<RBookDetailActivity> rBookDetailActivity;
        public MyHandler(WeakReference<RBookDetailActivity> rBookDetailActivity){
            this.rBookDetailActivity = rBookDetailActivity;
        }
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            RBookDetailActivity myActivity = rBookDetailActivity.get();
            if(what == UNKNOWN_REQUEST) {
                Toast.makeText(myActivity,"未知请求，无法处理！",Toast.LENGTH_SHORT).show();
            }
            else if(what == REQUEST_FAIL){
                Toast.makeText(myActivity,"网络异常！",Toast.LENGTH_SHORT).show();
            }else if(what == REQUEST_BUT_FAIL_READ_DATA){
                Toast.makeText(myActivity,"子线程解析数据异常！",Toast.LENGTH_SHORT).show();
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
                    View view = View.inflate(myActivity,R.layout.dialog_view,null);
                    TextView tvFrom = view.findViewById(R.id.dialog_from);
                    tvFrom.setText(TAG);
                    TextView tvCode = view.findViewById(R.id.dialog_code);
                    tvCode.setText(code);
                    TextView tvMessage = view.findViewById(R.id.dialog_message);
                    tvMessage.setText(message);
                    TextView tvTip = view.findViewById(R.id.dialog_tip);
                    tvTip.setText(tip);
                    DialogUtil.showDialog(myActivity,view,false);
                }
            }
        }
    }
    MyHandler myHandler = new MyHandler(new WeakReference(this));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_r_book_detail);
        // 判断传进来的id是否为空
        id = this.getIntent().getStringExtra("id");
        if(id == null){
            Toast.makeText(this,"无法获取书本详情！",Toast.LENGTH_SHORT).show();
            ActivityManager.getInstance().removeActivity(this);
            finish();
        }
        // 组件初始化
        // 界面组件:标题，Id，名称，作者，Isbn，所属馆，馆藏地点，索书号，主题，详情，一级分类，二级分类，文献类型，出版社，出版日期，定价，热度，状态
        TextView tTitle = findViewById(R.id.bookDetail_title);
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
        tPrice = findViewById(R.id.bookPrice);
        tHot = findViewById(R.id.bookHot);
        tState = findViewById(R.id.bookState);
        Button bBack = findViewById(R.id.bookDetail_back);
        bBack.setOnClickListener(view -> {
            ActivityManager.getInstance().removeActivity(this);
            finish();
        });
        Button bReserve = findViewById(R.id.bookDetail_reserve);
        bReserve.setOnClickListener(view -> {
            //发送预约请求
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
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        String token = userInfo.getToken();
        imagesOnlyReadAdapter = new ImagesOnlyReadAdapter(this,token,this);
        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setAdapter(imagesOnlyReadAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(jsonObjectBookDetail == null){
            // 发送获取书本详情的请求，获取书本详情
            // 获取token
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            String token = userInfo.getToken();
            // 使用Map封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", id);
            String url = HttpUtil.BASE_URL + "book/selectAllById";
            url = HttpUtil.newUrl(url,hashMap);
            HttpUtil.getRequest(token, url, this, GET_BOOK_DETAIL);
        }else {
            fillData();
        }
    }

    private void fillData() {
        try {
            id = jsonObjectBookDetail.getString("id");
            String s = "No." + id;
            tId.setText(s);
            tName.setText(jsonObjectBookDetail.getString("name"));
            tAuthor.setText(jsonObjectBookDetail.getString("isbn"));
            tIsbn.setText(jsonObjectBookDetail.getString("author"));
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
            JSONObject belong = new JSONObject(jsonObjectBookDetail.getString("classification"));
            tFirst.setText(belong.getString("first"));
            tThird.setText(belong.getString("third"));
            String d = jsonObjectBookDetail.getString("date");
            if (d == null || "null".equals(d) || "".equals(d)) {
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
            if (jsonArray1 != null && jsonArray1.length() > 0) {
                ArrayList<String> arrayList = new ArrayList<>();
                for (int i = 0; i < jsonArray1.length(); i++) {
                    String url = baseUrl + images + "/" + jsonArray1.get(i);
                    arrayList.add(url);
                }
                imagesOnlyReadAdapter.setImageNameUrlList(arrayList);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "主线程解析数据时异常！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClickToShow(int position) {
        Intent intent = new Intent(RBookDetailActivity.this, ShowPictureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("TAG",TAG);
        bundle.putParcelable("Adapter", imagesOnlyReadAdapter);
        bundle.putInt("position",position);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onClickToDelete(int position) {

    }

    @Override
    protected void onDestroy() {
        id = null;
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
                    if("查询成功！".equals(message)){
                        tip = jsonObject.getString("tip");
                        if(tip == null || "null".equals(tip)){
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
                    if("预约成功！".equals(message)){
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