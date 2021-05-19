package com.example.ul.librarian.main.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.activity.ShowPictureActivity;
import com.example.ul.adapter.ImagesAdapter;
import com.example.ul.adapter.MySpinnerAdapter;
import com.example.ul.adapter.MySpinnerBelongAdapter;
import com.example.ul.callback.ImageAdapterItemListener;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
 * @author luoweili
 */
@SuppressLint("NonConstantResourceId")
public class LShareDetailActivity extends AppCompatActivity implements HttpUtil.MyCallback, ImageAdapterItemListener {
    
    private static final String TAG = "LShareDetailActivity";
    /**未知请求错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 1800;
    /**请求失败*/
    private static final int REQUEST_FAIL = 18000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 1806;
    /**获取书本详情*/
    private static final int GET_BOOK_DETAIL = 1801;
    /**获取书本详情成功，有数据需要渲染*/
    private static final int GET_BOOK_DETAIL_FILL = 18011;
    /**添加书本*/
    private static final int ADD_BOOK = 1802;
    /**添加书本成功*/
    private static final int ADD_BOOK_SUCCESS = 18021;
    /**添加书本失败*/
    private static final int ADD_BOOK_FAIL = 18020;
    /**更新书本*/
    private static final int UPDATE_BOOK = 1803;
    /**更新成功*/
    private static final int UPDATE_BOOK_SUCCEED = 18031;
    /**更新失败*/
    private static final int UPDATE_BOOK_FAIL = 18030;
    /**删除书本信息*/
    private static final int DELETE_BOOK = 1804;
    private static final int DELETE_BOOK_SUCCEED = 18041;
    private static final int DELETE_BOOK_FAIL = 18040;
    /**查询分类*/
    private static final int GET_TYPE = 1805;
    /**服务器返回的书本详情数据*/
    private JSONObject jsonObjectBookDetail = null;
    /**要填充到各个下拉列表中的内容*/
    private JSONArray jsonArrayType;
    private List<String> firsts = new ArrayList<>();
    private List<List<String>> thirds = new ArrayList<>();
    /**当前图书馆/图书类别/文献类型*/
    private String first = "null", third = "null", type = "null";
    @BindView(R.id.bookId)
    public TextView tId;
    @BindView(R.id.bookName)
    public EditText tName;
    @BindView(R.id.bookAuthor)
    public EditText tAuthor;
    @BindView(R.id.bookLibrary)
    public TextView tLibrary;
    @BindView(R.id.bookContact)
    public TextView tBookContact;
    @BindView(R.id.bookTheme)
    public EditText tTheme;
    @BindView(R.id.bookDescription)
    public EditText tDesc;
    @BindView(R.id.bookFirst)
    public Spinner spinnerFirst;
    @BindView(R.id.bookThird)
    public Spinner spinnerThird;
    @BindView(R.id.bookType)
    public Spinner spinnerType;
    @BindView(R.id.shareDate)
    public EditText tShareDate;
    @BindView(R.id.bookPrice)
    public EditText tPrice;
    @BindView(R.id.bookHot)
    public EditText tHot;
    @BindView(R.id.bookState)
    public TextView tState;
    @BindView(R.id.btn_submit)
    public Button bSubmit;
    @BindView(R.id.btn_delete)
    public Button bDelete;
    private ImagesAdapter imagesAdapter;
    /**当前书本id*/
    private int id = 0;
    /**token*/
    private String token;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_l_share_detail);
        ButterKnife.bind(this);
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        token = userInfo.getToken();
        ImageView imageView = findViewById(R.id.iv_back);
        imageView.setOnClickListener(v -> LShareDetailActivity.this.finish());
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(gridLayoutManager);
        imagesAdapter = new ImagesAdapter(this,token);
        recyclerView.setAdapter(imagesAdapter);
        // 传进来的id是否为0，若为0，则说明是添加新书，若不为空则说明是查看书本详情
        id = this.getIntent().getIntExtra("id",0);
        if(id == 0){
            bDelete.setVisibility(View.GONE);
        }else {
            // 删除书籍请求
            bDelete.setOnClickListener(v ->{
                // 使用Map封装请求参数
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", String.valueOf(id));
                String url = HttpUtil.BASE_URL + "book/deleteBookById";
                url = HttpUtil.newUrl(url,hashMap);
                HttpUtil.deleteRequest(token,url,this,DELETE_BOOK);
            });
        }
        // 提交按钮绑定请求
        bSubmit.setOnClickListener(view -> {
            // 使用Map封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id",tId.getText().toString().trim());
            hashMap.put("name",tName.getText().toString().trim());
            hashMap.put("author",tAuthor.getText().toString().trim());
            hashMap.put("library",tLibrary.getText().toString().trim());
            hashMap.put("callNumber",tBookContact.getText().toString().trim());
            hashMap.put("theme",tTheme.getText().toString().trim());
            hashMap.put("desc",tDesc.getText().toString().trim());
            hashMap.put("first",first);
            hashMap.put("third",third);
            hashMap.put("typeName",type);
            hashMap.put("date",tShareDate.getText().toString().trim());
            hashMap.put("price",tPrice.getText().toString().trim());
            hashMap.put("hot",tHot.getText().toString().trim());
            hashMap.put("state",tState.getText().toString().trim());
            // 获取要提交的图片的全路径
            ArrayList<String> tempList = this.imagesAdapter.getImagesPath();
            if(id != 0){     // 绑定更新图书请求
                String url = HttpUtil.BASE_URL + "book/updateBook";
                HttpUtil.putRequest(token,url,hashMap,tempList,this,UPDATE_BOOK);
            }else {             // 绑定添加图书请求
                String url = HttpUtil.BASE_URL + "book/addBook";
                HttpUtil.postRequest(token,url,hashMap,tempList,this,ADD_BOOK);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 先发送获取分类的请求
        String getTypeUrl = HttpUtil.BASE_URL + "book/getDetailType";
        HttpUtil.getRequest(token,getTypeUrl,this,GET_TYPE);
    }

    private void clear() {
        tName.setText(null);
        tAuthor.setText(null);
        tBookContact.setText(null);
        tTheme.setText(null);
        tDesc.setText(null);
        tShareDate.setText(R.string.bookDateAutoFill);
        tPrice.setText(R.string.bookPrice_0);
        tHot.setText(R.string.math_0);
        spinnerType.setSelection(0,true);
        spinnerFirst.setSelection(0,true);
        spinnerThird.setSelection(0,true);
    }

    private void fillSpinnerData() {
        MySpinnerAdapter sAType = new MySpinnerAdapter(this,jsonArrayType);
        spinnerType.setAdapter(sAType);
        MySpinnerBelongAdapter mySpinnerBelongAdapter = new MySpinnerBelongAdapter(this,firsts);
        spinnerFirst.setAdapter(mySpinnerBelongAdapter);
        spinnerType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                type = (String) spinnerType.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // 根据spinnerFirst选的不同来动态渲染spinnerThird
        spinnerFirst.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                first = (String) spinnerFirst.getItemAtPosition(i);
                spinnerThird.setAdapter(new MySpinnerBelongAdapter(LShareDetailActivity.this,thirds.get(i)));
                spinnerThird.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        third = (String) spinnerThird.getItemAtPosition(i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
//
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//
            }
        });
        // 各列表第一项为默认选择值
        spinnerType.setSelection(0,true);
        spinnerFirst.setSelection(0,true);
        spinnerThird.setSelection(0,true);
        if(id != 0){
            // 发送查询书籍详情的请求
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", String.valueOf(id));
            String url = HttpUtil.BASE_URL + "book/selectAllById";
            // 拼接请求参数
            url = HttpUtil.newUrl(url,hashMap);
            HttpUtil.getRequest(token,url,this,GET_BOOK_DETAIL);
        }
    }

    private void fillBookDetail() {
        this.id = this.jsonObjectBookDetail.getInteger("id");
        String sId = "NO."+id;
        this.tId.setText(sId);
        this.tName.setText(this.jsonObjectBookDetail.getString("name"));
        this.tAuthor.setText(this.jsonObjectBookDetail.getString("author"));
        this.tBookContact.setText(this.jsonObjectBookDetail.getString("callNumber"));
        this.tTheme.setText(this.jsonObjectBookDetail.getString("theme"));
        this.tDesc.setText(this.jsonObjectBookDetail.getString("description"));
        // 改变列表的默认值
        String tType = this.jsonObjectBookDetail.getString("typeName");
        for (int i = 0; i < spinnerType.getCount(); i++) {
            String s = (String) spinnerType.getItemAtPosition(i);
            if (s.equals(tType)) {
                spinnerType.setSelection(i, true);
                break;
            }
        }
        this.tHot.setText(this.jsonObjectBookDetail.getString("hot"));
        this.tState.setText(this.jsonObjectBookDetail.getString("state"));
        this.tPrice.setText(this.jsonObjectBookDetail.getString("price"));
        JSONObject belong = JSON.parseObject(this.jsonObjectBookDetail.getString("classification"));
        // 改变列表的默认值
        String tFirst = belong.getString("first");
        for (int i = 0; i < spinnerFirst.getCount(); i++) {
            String s = (String) spinnerFirst.getItemAtPosition(i);
            if (s != null && s.equals(tFirst)) {
                spinnerFirst.setSelection(i, true);
                break;
            }
        }
        // 改变列表的默认值
        String tThird = belong.getString("third");
        for (int i = 0; i < spinnerThird.getCount(); i++) {
            String s = (String) spinnerThird.getItemAtPosition(i);
            if (s != null && s.equals(tThird)) {
                spinnerThird.setSelection(i, true);
                break;
            }
        }
        String d = this.jsonObjectBookDetail.getString("date");
        String n = "null";
        if (d == null || n.equals(d) || "".equals(d)) {
            this.tShareDate.setText(null);
        } else {
            long l = Long.parseLong(d);
            Date date = new Date(l);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String tvDate = format.format(date);
            this.tShareDate.setText(tvDate);
        }
        // 获取图片名，构造出获取图片的url
        // 获取图片的基本url
        String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
        String images = jsonObjectBookDetail.getString("images");
        JSONArray jsonArray1 = jsonObjectBookDetail.getJSONArray("pictures");
        ArrayList<String> arrayList = new ArrayList<>();
        if (jsonArray1 != null && jsonArray1.size() > 0) {
            for (int i = 0; i < jsonArray1.size(); i++) {
                String url = baseUrl + images + "/" + jsonArray1.get(i);
                arrayList.add(url);
            }
        }
        imagesAdapter.setImageNameUrlList(arrayList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        id = 0;
        ActivityManager.getInstance().removeActivity(this);
    }

    @Override
    public void onClickToShow(int position) {
        // 当前不处于删除状态
        if(!imagesAdapter.getDeleting()){
            if(position == imagesAdapter.getItemCount()-1) {
                //进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create(this)
                        //全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .openGallery(PictureMimeType.ofImage())
                        //每行显示个数 int
                        .imageSpanCount(3)
                        .maxSelectNum(30)
                        //多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .selectionMode(PictureConfig.MULTIPLE)
                        //是否可预览图片
                        .previewImage(true)
                        //是否显示拍照按钮 true or false
                        .isCamera(false)
                        //拍照保存图片格式后缀,默认jpeg
                        .imageFormat(PictureMimeType.JPEG)
                        //图片列表点击 缩放效果 默认true
                        .isZoomAnim(true)
                        //int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .withAspectRatio(1, 1)
                        //是否显示uCrop工具栏，默认不显示 true or false
                        .hideBottomControls(false)
                        //裁剪框是否可拖拽 true or false
                        .freeStyleCropEnabled(false)
                        //是否圆形裁剪 true or false
                        .circleDimmedLayer(false)
                        //是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                        .showCropFrame(false)
                        //是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                        .showCropGrid(false)
                        //是否开启点击声音 true or false
                        .openClickSound(true)
                        //同步true或异步false 压缩 默认同步
                        .synOrAsy(true)
                        //裁剪是否可旋转图片 true or false
                        .rotateEnabled(false)
                        //裁剪是否可放大缩小图片 true or false
                        .scaleEnabled(true)
                        //是否可拖动裁剪框(固定)
                        .isDragFrame(false)
                        //结果回调onActivityResult requestCode
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
            else {
                Intent intent = new Intent(LShareDetailActivity.this, ShowPictureActivity.class);
                intent.putExtra("TAG", TAG);
                intent.putExtra("position",position);
                intent.putExtra("imagesPath", imagesAdapter.getImagesPath());
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 结果回调
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                imagesAdapter.setSelectList((ArrayList<LocalMedia>) PictureSelector.obtainMultipleResult(data));
            }
        }
    }

    @Override
    public void onClickToDelete(int position) {
        // 当前处于删除状态
        if(imagesAdapter.getDeleting()){
            // 如果当前是第一次删除图片，弹出提示框
            if(imagesAdapter.isFirstDelete()){
                DialogUtil.showDialog(this,this.imagesAdapter,position);
            }else {
                imagesAdapter.removeItem(position);
            }
        }
    }

    static class MyHandler extends Handler {
        private WeakReference<LShareDetailActivity> lShareDetailActivity;
        public MyHandler(WeakReference<LShareDetailActivity> lShareDetailActivity){
            this.lShareDetailActivity = lShareDetailActivity;
        }
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            LShareDetailActivity myActivity = lShareDetailActivity.get();
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            }else if(what == GET_TYPE){
                myActivity.fillSpinnerData();
            }else if (what == GET_BOOK_DETAIL_FILL) {
                myActivity.fillBookDetail();
            } else {
                Bundle data = msg.getData();
                String message = data.getString("message");
                if(what == ADD_BOOK_SUCCESS){
                    myActivity.clear();
                    Toast.makeText(myActivity, message, Toast.LENGTH_LONG).show();
                }else if(what == UPDATE_BOOK_SUCCEED){
                    Toast.makeText(myActivity, message, Toast.LENGTH_LONG).show();
                }else if(what == DELETE_BOOK_SUCCEED){
                    Toast.makeText(myActivity, message, Toast.LENGTH_LONG).show();
                    myActivity.finish();
                }
                else {
                    DialogUtil.showDialog(myActivity,TAG,data,what == REQUEST_INTERCEPTED);
                }
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
        } else {
            switch (code) {
                case GET_TYPE:
                    // 将整个信息拆分
                    JSONArray jsonArraySpinners = jsonObject.getJSONArray("dataArray");
                    JSONArray jsonArray0 = jsonArraySpinners.getJSONArray(1);
                    jsonArrayType = jsonArraySpinners.getJSONArray(2);
                    firsts.clear();
                    thirds.clear();
                    for (int i = 0; i < jsonArray0.size(); i++) {
                        JSONArray belong = jsonArray0.getJSONArray(i);
                        String first = belong.getString(0);
                        String third = belong.getString(1);
                        String[] arrayStr = third.split(",");
                        List<String> list = new ArrayList<>(Arrays.asList(arrayStr));
                        firsts.add(first);
                        thirds.add(list);
                    }
                    myHandler.sendEmptyMessage(GET_TYPE);
                    break;
                case GET_BOOK_DETAIL:
                    message = jsonObject.getString("message");
                    if ("查询成功！".equals(message)) {
                        tip = jsonObject.getString("tip");
                        if ("".equals(tip)) {
                            // 查询成功，获取书籍数据，通知主线程渲染前端
                            jsonObjectBookDetail = jsonObject.getJSONObject("object");
                            myHandler.sendEmptyMessage(GET_BOOK_DETAIL_FILL);
                            break;
                        }
                    } else {
                        tip = jsonObject.getString("tip");
                        c = jsonObject.getString("code");
                        bundle = new Bundle();
                        bundle.putString("code", c);
                        bundle.putString("tip", tip);
                        bundle.putString("message", message);
                        msg.setData(bundle);
                        msg.what = GET_BOOK_DETAIL_FILL;
                        myHandler.sendMessage(msg);
                    }
                    break;
                case ADD_BOOK:
                    message = jsonObject.getString("message");
                    c = jsonObject.getString("code");
                    bundle = new Bundle();
                    bundle.putString("code", c);
                    bundle.putString("message", message);
                    msg.setData(bundle);
                    if ("添加成功！".equals(message)) {
                        msg.what = ADD_BOOK_SUCCESS;
                    } else {
                        tip = jsonObject.getString("tip");
                        bundle.putString("tip", tip);
                        msg.what = ADD_BOOK_FAIL;
                    }
                    myHandler.sendMessage(msg);
                    break;
                case UPDATE_BOOK:
                    message = jsonObject.getString("message");
                    c = jsonObject.getString("code");
                    msg = new Message();
                    bundle = new Bundle();
                    bundle.putString("code", c);
                    bundle.putString("message", message);
                    msg.setData(bundle);
                    if ("更新成功！".equals(message)) {
                        msg.what = UPDATE_BOOK_SUCCEED;
                    } else {
                        tip = jsonObject.getString("tip");
                        bundle.putString("tip", tip);
                        msg.what = UPDATE_BOOK_FAIL;
                    }
                    myHandler.sendMessage(msg);
                    break;
                case DELETE_BOOK:
                    message = jsonObject.getString("message");
                    c = jsonObject.getString("code");
                    msg = new Message();
                    bundle = new Bundle();
                    bundle.putString("code", c);
                    bundle.putString("message", message);
                    msg.setData(bundle);
                    if ("删除成功！".equals(message)) {
                        msg.what = DELETE_BOOK_SUCCEED;
                    } else {
                        tip = jsonObject.getString("tip");
                        bundle.putString("tip", tip);
                        msg.what = DELETE_BOOK_FAIL;
                    }
                    myHandler.sendMessage(msg);
                    break;
                default:
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