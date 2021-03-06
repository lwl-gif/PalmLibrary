package com.example.ul.librarian.main.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.bumptech.glide.Glide;
import com.example.ul.R;
import com.example.ul.activity.ShowPictureActivity;
import com.example.ul.adapter.ImagesAdapter;
import com.example.ul.adapter.MySpinnerAdapter;
import com.example.ul.adapter.MySpinnerBelongAdapter;
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
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @author luoweili
 */
@SuppressLint("NonConstantResourceId")
public class LShareDetailActivity extends AppCompatActivity implements HttpUtil.MyCallback, ImageAdapterItemListener,
        DialogUtil.DialogActionCallback{
    private static final String TAG = "LShareDetailActivity";
    /**??????????????????*/
    private static final int UNKNOWN_REQUEST_ERROR = 1800;
    /**????????????*/
    private static final int REQUEST_FAIL = 18000;
    /**???????????????????????????????????????*/
    private static final int REQUEST_INTERCEPTED = 1806;
    /**??????????????????*/
    private static final int GET_BOOK_DETAIL = 1801;
    /**????????????????????????????????????????????????*/
    private static final int GET_BOOK_DETAIL_FILL = 18011;
    /**????????????*/
    private static final int ADD_BOOK = 1802;
    /**??????????????????*/
    private static final int ADD_BOOK_SUCCESS = 18021;
    /**??????????????????*/
    private static final int ADD_BOOK_FAIL = 18020;
    /**????????????*/
    private static final int UPDATE_BOOK = 1803;
    /**????????????*/
    private static final int UPDATE_BOOK_SUCCEED = 18031;
    /**????????????*/
    private static final int UPDATE_BOOK_FAIL = 18030;
    /**??????????????????*/
    private static final int DELETE_BOOK = 1804;
    private static final int DELETE_BOOK_SUCCEED = 18041;
    private static final int DELETE_BOOK_FAIL = 18040;
    /**????????????*/
    private static final int GET_TYPE = 1805;
    /**????????????????????????????????????*/
    private JSONObject jsonObjectBookDetail = null;
    /**??????????????????????????????????????????*/
    private JSONArray jsonArrayType;
    private List<String> firsts = new ArrayList<>();
    private List<List<String>> thirds = new ArrayList<>();
    /**???????????????/????????????/????????????*/
    private String first = null, third = null, typeName = null;
    @BindView(R.id.bookId)
    public TextView tId;
    @BindView(R.id.bookName)
    public EditText tName;
    @BindView(R.id.bookAuthor)
    public EditText tAuthor;
    @BindView(R.id.bookLibrary)
    public TextView tLibrary;
    @BindView(R.id.bookContact)
    public EditText tBookContact;
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
    /**????????????id*/
    private int id = -1;
    /**token*/
    private String token;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_l_share_detail);
        ButterKnife.bind(this);
        // ??????token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        token = userInfo.getToken();
        ImageView imageView = findViewById(R.id.iv_back);
        imageView.setOnClickListener(v -> LShareDetailActivity.this.finish());
        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(gridLayoutManager);
        imagesAdapter = new ImagesAdapter(this,token);
        recyclerView.setAdapter(imagesAdapter);
        // ????????????id?????????0?????????0????????????????????????????????????????????????????????????????????????
        id = this.getIntent().getIntExtra("id",-1);
        if(id == -1){
            bDelete.setVisibility(View.GONE);
        }else {
            // ??????????????????
            bDelete.setOnClickListener(v ->{
                HashMap<String, Object> hashMap = new HashMap<>(4);
                hashMap.put("requestCode",DELETE_BOOK);
                DialogUtil.showDialog(LShareDetailActivity.this,"????????????","??????????????????????????????????????????????????????????????????",this,hashMap);
            });
        }
        // ????????????????????????
        bSubmit.setOnClickListener(view -> {
            HashMap<String, Object> hashMap = new HashMap<>(4);
            String title;
            String message;
            if(id != -1){
                hashMap.put("requestCode",UPDATE_BOOK);
                title = "????????????";
            }else {
                hashMap.put("requestCode",ADD_BOOK);
                title = "????????????";
            }
            message = "?????????????????????";
            DialogUtil.showDialog(LShareDetailActivity.this,title,message,this,hashMap);
        });
        // ??????????????????????????????
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
        MySpinnerAdapter mySpinnerAdapter = new MySpinnerAdapter(this,jsonArrayType);
        spinnerType.setAdapter(mySpinnerAdapter);
        MySpinnerBelongAdapter mySpinnerBelongAdapter = new MySpinnerBelongAdapter(this,firsts);
        spinnerFirst.setAdapter(mySpinnerBelongAdapter);
        spinnerType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                typeName = (String) spinnerType.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // ??????spinnerFirst???????????????????????????spinnerThird
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
        // ????????????????????????????????????
        spinnerType.setSelection(0,true);
        spinnerFirst.setSelection(0,true);
        spinnerThird.setSelection(0,true);
        if(id != -1){
            // ?????????????????????????????????
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", String.valueOf(id));
            String url = HttpUtil.BASE_URL + "book/selectAllById";
            // ??????????????????
            url = HttpUtil.newUrl(url,hashMap);
            HttpUtil.getRequest(token,url,this,GET_BOOK_DETAIL);
        }
    }

    private void fillBookDetail() {
        this.id = this.jsonObjectBookDetail.getInteger("id");
        String sId = String.valueOf(id);
        this.tId.setText(sId);
        this.tName.setText(this.jsonObjectBookDetail.getString("name"));
        this.tAuthor.setText(this.jsonObjectBookDetail.getString("author"));
        this.tBookContact.setText(this.jsonObjectBookDetail.getString("callNumber"));
        this.tTheme.setText(this.jsonObjectBookDetail.getString("theme"));
        this.tDesc.setText(this.jsonObjectBookDetail.getString("description"));
        // ????????????????????????
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
        // ????????????????????????
        String tFirst = belong.getString("first");
        for (int i = 0; i < spinnerFirst.getCount(); i++) {
            String s = (String) spinnerFirst.getItemAtPosition(i);
            if (s != null && s.equals(tFirst)) {
                spinnerFirst.setSelection(i, true);
                break;
            }
        }
        // ????????????????????????
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
        // ??????????????????????????????????????????url
        // ?????????????????????url
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
        id = -1;
        ActivityManager.getInstance().removeActivity(this);
        new Thread(() -> {
            Glide.get(LShareDetailActivity.this).clearDiskCache();
        }).start();
        Glide.get(LShareDetailActivity.this).clearMemory();
    }

    @Override
    public void onClickToShow(int position) {
        // ???????????????????????????
        if(!imagesAdapter.getDeleting()){
            if(position == imagesAdapter.getItemCount()-1) {
                //???????????? ??????????????????????????????api????????????
                PictureSelector.create(this)
                        //??????.PictureMimeType.ofAll()?????????.ofImage()?????????.ofVideo()?????????.ofAudio()
                        .openGallery(PictureMimeType.ofImage())
                        //?????????????????? int
                        .imageSpanCount(3)
                        .maxSelectNum(30)
                        //?????? or ?????? PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .selectionMode(PictureConfig.MULTIPLE)
                        //?????????????????????
                        .previewImage(true)
                        //???????????????????????? true or false
                        .isCamera(false)
                        //??????????????????????????????,??????jpeg
                        .imageFormat(PictureMimeType.JPEG)
                        //?????????????????? ???????????? ??????true
                        .isZoomAnim(true)
                        //int ???????????? ???16:9 3:2 3:4 1:1 ????????????
                        .withAspectRatio(1, 1)
                        //????????????uCrop??????????????????????????? true or false
                        .hideBottomControls(false)
                        //???????????????????????? true or false
                        .freeStyleCropEnabled(false)
                        //?????????????????? true or false
                        .circleDimmedLayer(false)
                        //?????????????????????????????? ???????????????????????????false   true or false
                        .showCropFrame(false)
                        //?????????????????????????????? ???????????????????????????false    true or false
                        .showCropGrid(false)
                        //???????????????????????? true or false
                        .openClickSound(true)
                        //??????true?????????false ?????? ????????????
                        .synOrAsy(true)
                        //??????????????????????????? true or false
                        .rotateEnabled(false)
                        //????????????????????????????????? true or false
                        .scaleEnabled(true)
                        //????????????????????????(??????)
                        .isDragFrame(false)
                        //????????????onActivityResult requestCode
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
            // ????????????
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                imagesAdapter.setSelectList((ArrayList<LocalMedia>) PictureSelector.obtainMultipleResult(data));
            } else {
                imagesAdapter.setSelectList(new ArrayList<>());
            }
        }
    }

    @Override
    public void onClickToDelete(int position) {
        // ????????????????????????
        if(imagesAdapter.getDeleting()){
            // ??????????????????????????????????????????????????????
            if(imagesAdapter.isFirstDelete()){
                DialogUtil.showDialog(this,this.imagesAdapter,position);
            }else {
                imagesAdapter.removeItem(position);
            }
        }
    }

    @Override
    public void positiveAction(HashMap<String, Object> requestParam) {
        Integer requestCode = (Integer) requestParam.get("requestCode");
        if (requestCode == null) {
            Toast.makeText(this,"???????????????",Toast.LENGTH_SHORT).show();
        } else {
            if(requestCode == ADD_BOOK || requestCode == UPDATE_BOOK){
                Book book = new Book();
                book.setName(tName.getText().toString().trim());
                book.setAuthor(tAuthor.getText().toString().trim());
                book.setLibrary(tLibrary.getText().toString().trim());
                book.setCallNumber(tBookContact.getText().toString().trim());
                book.setTheme(tTheme.getText().toString().trim());
                book.setDescription(tDesc.getText().toString().trim());
                Classification classification = new Classification();
                classification.setFirst(first);
                classification.setThird(third);
                book.setClassification(classification);
                String dateString = tShareDate.getText().toString().trim();
                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date;
                try {
                    date = sd.parse(dateString);
                    book.setDate(date);
                    String priceString = tPrice.getText().toString().trim();
                    BigDecimal price = new BigDecimal(priceString);
                    book.setPrice(price);
                    book.setHot(Integer.valueOf(tHot.getText().toString().trim()));
                    book.setState(tState.getText().toString().trim());
                    if(requestCode == UPDATE_BOOK){
                        book.setId(Integer.valueOf(tId.getText().toString().trim()));
                    }
                    // ??????Map??????????????????
                    HashMap<String, String> hashMap = new HashMap<>(2);
                    ObjectMapper mapper = new ObjectMapper();
                    String bookString;
                    try {
                        bookString = mapper.writeValueAsString(book);
                        hashMap.put("bookString", bookString);
                        hashMap.put("typeName", typeName);
                        // ????????????????????????????????????
                        ArrayList<String> tempList = imagesAdapter.getImagesPath();
                        if(requestCode == ADD_BOOK){
                            // ????????????????????????
                            String url = HttpUtil.BASE_URL + "book/addBook";
                            HttpUtil.postRequest(token,url,hashMap,tempList,this,ADD_BOOK);
                        }else {
                            // ????????????????????????
                            String url = HttpUtil.BASE_URL + "book/updateBook";
                            HttpUtil.putRequest(token,url,hashMap,tempList,this,UPDATE_BOOK);
                        }
                    } catch (JsonProcessingException e) {
                        Toast.makeText(this,"?????????Json??????????????????",Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(this,"??????????????????",Toast.LENGTH_SHORT).show();
                }
            }else if(requestCode == DELETE_BOOK){
                // ??????Map??????????????????
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", String.valueOf(this.id));
                String url = HttpUtil.BASE_URL + "book/deleteBookById";
                url = HttpUtil.newUrl(url,hashMap);
                HttpUtil.deleteRequest(token,url,this,DELETE_BOOK);
            }else {
                Toast.makeText(this,"????????????",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void negativeAction(HashMap<String, Object> requestParam) {
        Integer requestCode = (Integer) requestParam.get("requestCode");
        if(requestCode == null){
            Toast.makeText(this,"???????????????",Toast.LENGTH_SHORT).show();
        }else {
            if(requestCode == ADD_BOOK || requestCode == UPDATE_BOOK || requestCode == DELETE_BOOK){
                Toast.makeText(this,"??????????????????",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"????????????",Toast.LENGTH_SHORT).show();
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
        // ??????????????????????????????
        String result = response.body().string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        Message msg = new Message();
        Bundle bundle = new Bundle();
        String message = jsonObject.getString("message");
        String c = jsonObject.getString("code");
        String tip = jsonObject.getString("tip");
        // ????????????true,?????????????????????
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
                    // ?????????????????????
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
                    if ("???????????????".equals(message)) {
                        tip = jsonObject.getString("tip");
                        if ("".equals(tip)) {
                            // ???????????????????????????????????????????????????????????????
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
                    if ("???????????????".equals(message)) {
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
                    if ("???????????????".equals(message)) {
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
                    if ("???????????????".equals(message)) {
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
            reason = "????????????";
            message.what = REQUEST_FAIL;
        } else if (e instanceof ConnectException) {
            reason = "?????????????????????";
            message.what = REQUEST_FAIL;
        } else if (e instanceof UnknownHostException) {
            reason = "????????????";
            message.what = REQUEST_FAIL;
        } else {
            reason = "????????????";
            message.what = UNKNOWN_REQUEST_ERROR;
        }
        bundle.putString("reason", reason);
        message.setData(bundle);
        myHandler.sendMessage(message);
    }
}