package com.example.ul.librarian.main.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.ul.reader.main.activity.RReaderDetailActivity;
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
 * @Author: Wallace
 * @Description: ??????????????????????????????????????????(???????????????id?????????null?????????)???
 * 1.?????????????????????????????????????????????????????????????????????????????????
 * 2.????????????????????????????????????????????????????????????????????????????????????????????????????????????
 * @Date: Created in 22:25 2021/3/28
 * @Modified By:
 */
@SuppressLint("NonConstantResourceId")
public class LBookDetailActivity extends Activity implements HttpUtil.MyCallback, DialogUtil.DialogActionCallback,
        ImageAdapterItemListener {

    private static final String TAG = "LBookDetailActivity";
    /**????????????*/
    private static final int UNKNOWN_REQUEST_ERROR = 800;
    /**????????????*/
    private static final int REQUEST_FAIL = 8000;
    /**???????????????????????????????????????*/
    private static final int REQUEST_INTERCEPTED = 8002;
    /**??????????????????*/
    private static final int GET_BOOK_DETAIL = 801;
    /**????????????*/
    private static final int ADD_BOOK = 802;
    /**??????????????????*/
    private static final int ADD_BOOK_SUCCESS = 8021;
    /**??????????????????*/
    private static final int ADD_BOOK_FAIL = 8020;
    /**????????????*/
    private static final int UPDATE_BOOK = 803;
    /**????????????*/
    private static final int UPDATE_BOOK_SUCCEED = 8031;
    /**????????????*/
    private static final int UPDATE_BOOK_FAIL = 8030;
    /**??????????????????*/
    private static final int DELETE_BOOK = 804;
    private static final int DELETE_BOOK_SUCCEED = 8041;
    private static final int DELETE_BOOK_FAIL = 8040;
    /**????????????*/
    private static final int GET_TYPE = 805;
    /**??????????????????????????????????????????*/
    private JSONArray jsonArrayLibrary,jsonArrayType;
    private List<String> firsts = new ArrayList<>();
    private List<List<String>> thirds = new ArrayList<>();
    /**???????????????/????????????/????????????*/
    private String library = "null", first = "null", third = "null", typeName = "null";
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
    @BindView(R.id.iv_back)
    public ImageView ivBack;
    @BindView(R.id.l_bookDetail_submit)
    public Button btnSubmit;

    private RecyclerView recyclerView;
    private ImagesAdapter imagesAdapter;
    private String token;
    /**????????????id*/
    private int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_l_book_detail);
        ButterKnife.bind(this);
        // ??????token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        token = userInfo.getToken();
        imagesAdapter = new ImagesAdapter(this,token);
        recyclerView = findViewById(R.id.l_book_recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(imagesAdapter);
        ivBack.setOnClickListener(view -> LBookDetailActivity.this.finish());
        // ??????????????????id????????????????????????????????????????????????????????????????????????????????????????????????
        id = this.getIntent().getIntExtra("id",-1);
        if(id != -1){
            tTitle.setText("????????????");
            btnSubmit.setText(R.string.update);
            Button bDelete = findViewById(R.id.l_bookDetail_delete);
            bDelete.setVisibility(View.VISIBLE);
            // ??????????????????
            bDelete.setOnClickListener(view -> {
                HashMap<String, Object> hashMap = new HashMap<>(4);
                hashMap.put("requestCode",DELETE_BOOK);
                DialogUtil.showDialog(LBookDetailActivity.this,"????????????","??????????????????????????????????????????????????????????????????",this,hashMap);
            });
        }else {
            tTitle.setText("????????????");
            tState.setText("??????");
            btnSubmit.setText(R.string.add);
        }
        // ????????????????????????
        btnSubmit.setOnClickListener(view -> {
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
            DialogUtil.showDialog(LBookDetailActivity.this,title,message,this,hashMap);
        });
        // ??????????????????????????????
        String url = HttpUtil.BASE_URL + "book/getDetailType";
        HttpUtil.getRequest(token,url,this,GET_TYPE);
    }

    private void clear() {
        id = -1;
        tName.setText(null);
        tAuthor.setText(null);
        tIsbn.setText(null);
        tLocation.setText(null);
        tCallNumber.setText(null);
        tTheme.setText(null);
        tDesc.setText(null);
        tHouse.setText(null);
        tDate.setText(R.string.bookDateAutoFill);
        tPrice.setText(R.string.bookPrice_0);
        tHot.setText(R.string.math_0);
        spinnerLibrary.setSelection(0,true);
        spinnerType.setSelection(0,true);
        spinnerFirst.setSelection(0,true);
        spinnerThird.setSelection(0,true);
    }
    /**
     * @Author: Wallace
     * @Description: ?????????Spinner?????????????????????????????????
     * @Date: Created in 13:16 2021/3/31
     * @Modified By:
     * @return: void
     */
    private void fillSpinnerData() {
        MySpinnerAdapter sALibrary = new MySpinnerAdapter(this,jsonArrayLibrary);
        MySpinnerAdapter sAType = new MySpinnerAdapter(this,jsonArrayType);
        spinnerLibrary.setAdapter(sALibrary);
        spinnerType.setAdapter(sAType);
        MySpinnerBelongAdapter mySpinnerBelongAdapter = new MySpinnerBelongAdapter(this,firsts);
        spinnerFirst.setAdapter(mySpinnerBelongAdapter);
        // ????????????
        spinnerLibrary.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                library = (String) spinnerLibrary.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
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
                // ???spinnerThird??????
                spinnerThird.setAdapter(new MySpinnerBelongAdapter(LBookDetailActivity.this,thirds.get(i)));
                spinnerThird.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        third = (String) spinnerThird.getItemAtPosition(i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // ????????????????????????????????????
        spinnerLibrary.setSelection(0,true);
        spinnerType.setSelection(0,true);
        spinnerFirst.setSelection(0,true);
        spinnerThird.setSelection(0,true);
        if(id != -1){
            // ?????????????????????????????????
            // ??????Map??????????????????
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", String.valueOf(id));
            String url = HttpUtil.BASE_URL + "book/selectAllById";
            // ??????????????????
            url = HttpUtil.newUrl(url,hashMap);
            HttpUtil.getRequest(token,url,this,GET_BOOK_DETAIL);
        }
    }

    private void fillBookDetail(Book book) {
        id = book.getId();
        this.tId.setText(String.valueOf(id));
        this.tName.setText(book.getName());
        this.tAuthor.setText(book.getAuthor());
        this.tIsbn.setText(book.getIsbn());
        // ????????????????????????
        String tLibrary = book.getLibrary();
        for (int i = 0; i < spinnerLibrary.getCount(); i++) {
            String s = (String) spinnerLibrary.getItemAtPosition(i);
            if (s != null && s.equals(tLibrary)) {
                spinnerLibrary.setSelection(i, true);
                break;
            }
        }
        this.tLocation.setText(book.getLocation());
        this.tCallNumber.setText(book.getCallNumber());
        this.tTheme.setText(book.getTheme());
        this.tDesc.setText(book.getDescription());
        // ????????????????????????
        String tType = book.getTypeName();
        for (int i = 0; i < spinnerType.getCount(); i++) {
            String s = (String) spinnerType.getItemAtPosition(i);
            if (s.equals(tType)) {
                spinnerType.setSelection(i, true);
                break;
            }
        }
        this.tHouse.setText(book.getHouse());
        String hotString = String.valueOf(book.getHot());
        this.tHot.setText(hotString);
        this.tState.setText(book.getState());
        String price = book.getPrice().toString();
        this.tPrice.setText(price);
        Classification classification = book.getClassification();
        // ????????????????????????
        String tFirst = classification.getFirst();
        for (int i = 0; i < spinnerFirst.getCount(); i++) {
            String s = (String) spinnerFirst.getItemAtPosition(i);
            if (s != null && s.equals(tFirst)) {
                spinnerFirst.setSelection(i, true);
                break;
            }
        }
        // ????????????????????????
        String tThird = classification.getThird();
        for (int i = 0; i < spinnerThird.getCount(); i++) {
            String s = (String) spinnerThird.getItemAtPosition(i);
            if (s != null && s.equals(tThird)) {
                spinnerThird.setSelection(i, true);
                break;
            }
        }
        Date date = book.getDate();
        String rdTermString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        this.tDate.setText(rdTermString);
        // ??????????????????????????????????????????url
        // ?????????????????????url
        String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
        String images = book.getImages();
        ArrayList<String> pictures = book.getPictures();
        ArrayList<String> arrayList = new ArrayList<>();
        if (pictures != null && pictures.size() > 0) {
            for (int i = 0; i < pictures.size(); i++) {
                String url = baseUrl + images + "/" + pictures.get(i);
                arrayList.add(url);
            }
        }
        imagesAdapter.setImageNameUrlList(arrayList);
    }
    /**
     * @Author: Wallace
     * @Description: ??????????????????????????????item
     * 1.??????????????????item,???????????????Activity????????????????????????????????????
     * 2.???????????????item????????????????????????
     * @Date: Created 19:28 2021/4/20
     * @Modified: by who yyyy-MM-dd
     * @param position item?????????
     * @return: void
     */
    @Override
    public void onClickToShow(int position) {
        // ???????????????????????????
        if(!imagesAdapter.getDeleting()){
            if(position == imagesAdapter.getItemCount()-1) {
                // ???????????? ??????????????????????????????api????????????
                PictureSelector.create(this)
                        // ??????.PictureMimeType.ofAll()?????????.ofImage()?????????.ofVideo()?????????.ofAudio()
                        .openGallery(PictureMimeType.ofImage())
                        // ?????????????????? int
                        .imageSpanCount(3)
                        .maxSelectNum(30)
                        // ?????? or ?????? PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .selectionMode(PictureConfig.MULTIPLE)
                        // ?????????????????????
                        .previewImage(true)
                        // ???????????????????????? true or false
                        .isCamera(false)
                        // ??????????????????????????????,??????jpeg
                        .imageFormat(PictureMimeType.JPEG)
                        // ?????????????????? ???????????? ??????true
                        .isZoomAnim(true)
                        // int ???????????? ???16:9 3:2 3:4 1:1 ????????????
                        .withAspectRatio(1, 1)
                        // ????????????uCrop??????????????????????????? true or false
                        .hideBottomControls(false)
                        // ???????????????????????? true or false
                        .freeStyleCropEnabled(false)
                        // ?????????????????? true or false
                        .circleDimmedLayer(false)
                        // ?????????????????????????????? ???????????????????????????false   true or false
                        .showCropFrame(false)
                        // ?????????????????????????????? ???????????????????????????false    true or false
                        .showCropGrid(false)
                        // ???????????????????????? true or false
                        .openClickSound(true)
                        // ??????true?????????false ?????? ????????????
                        .synOrAsy(true)
                        // ??????????????????????????? true or false
                        .rotateEnabled(false)
                        // ????????????????????????????????? true or false
                        .scaleEnabled(true)
                        // ????????????????????????(??????)
                        .isDragFrame(false)
                        // ????????????onActivityResult requestCode
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
            else {
                Intent intent = new Intent(LBookDetailActivity.this, ShowPictureActivity.class);
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
    protected void onDestroy() {
        super.onDestroy();
        id = -1;
        token = null;
        ActivityManager.getInstance().removeActivity(this);
        new Thread(() -> {
            Glide.get(LBookDetailActivity.this).clearDiskCache();
        }).start();
        Glide.get(LBookDetailActivity.this).clearMemory();
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
                book.setIsbn(tIsbn.getText().toString().trim());
                book.setLibrary(library);
                book.setLocation(tLocation.getText().toString().trim());
                book.setCallNumber(tCallNumber.getText().toString().trim());
                book.setTheme(tTheme.getText().toString().trim());
                book.setDescription(tDesc.getText().toString().trim());
                Classification classification = new Classification();
                classification.setFirst(first);
                classification.setThird(third);
                book.setClassification(classification);
                book.setHouse(tHouse.getText().toString().trim());
                String dateString = tDate.getText().toString().trim();
                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
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
                        ArrayList<String> tempList = this.imagesAdapter.getImagesPath();
                        if(requestCode == ADD_BOOK){
                            // ????????????????????????
                            String url = HttpUtil.BASE_URL + "book/addBook";
                            HttpUtil.postRequest(token,url,hashMap,tempList,this,ADD_BOOK);
                        }else {
                            // ????????????????????????
                            book.setId(Integer.valueOf(tId.getText().toString().trim()));
                            String url = HttpUtil.BASE_URL + "book/updateBook";
                            HttpUtil.putRequest(token,url,hashMap,tempList,this,UPDATE_BOOK);
                        }
                    } catch (JsonProcessingException e) {
                        Toast.makeText(this,"?????????Json??????????????????",Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
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
        private WeakReference<LBookDetailActivity> lBookDetailActivity;
        public MyHandler(WeakReference<LBookDetailActivity> lBookDetailActivity){
            this.lBookDetailActivity = lBookDetailActivity;
        }
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            LBookDetailActivity myActivity = lBookDetailActivity.get();
            if(what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity,bundle.getString("reason"),Toast.LENGTH_SHORT).show();
            }else if(what == GET_TYPE){
                myActivity.fillSpinnerData();
            }else {
                Bundle data = msg.getData();
                String message = data.getString("message");
                if(what == ADD_BOOK_SUCCESS){
                    myActivity.clear();
                    Toast.makeText(myActivity, message, Toast.LENGTH_LONG).show();
                }else if(what == GET_BOOK_DETAIL){
                    String bookDetail = data.getString("bookDetail");
                    if(bookDetail == null){
                        DialogUtil.showDialog(myActivity,TAG,data, false);
                    }else {
                        Book book = JSON.parseObject(bookDetail, Book.class);
                        myActivity.fillBookDetail(book);
                    }
                }
                else if(what == UPDATE_BOOK_SUCCEED){
                    Toast.makeText(myActivity, message, Toast.LENGTH_LONG).show();
                }else if(what == DELETE_BOOK_SUCCEED){
                    Toast.makeText(myActivity, message, Toast.LENGTH_LONG).show();
                    myActivity.finish();
                }
                else {
                    DialogUtil.showDialog(myActivity,TAG,data, what == REQUEST_INTERCEPTED);
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
        Bundle data = new Bundle();
        String message = jsonObject.getString("message");
        String c = jsonObject.getString("code");
        String tip = jsonObject.getString("tip");
        // ????????????true,?????????????????????
        if (HttpUtil.requestIsIntercepted(jsonObject)) {
            data.putString("code", c);
            data.putString("tip", tip);
            data.putString("message", message);
            msg.setData(data);
            msg.what = REQUEST_INTERCEPTED;
            myHandler.sendMessage(msg);
        } else {
            if (code == GET_TYPE) {
                // ?????????????????????
                JSONArray jsonArraySpinners = jsonObject.getJSONArray("dataArray");
                jsonArrayLibrary = jsonArraySpinners.getJSONArray(0);
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
            } else if (code == GET_BOOK_DETAIL) {
                message = jsonObject.getString("message");
                if ("???????????????".equals(message)) {
                    String bookDetail = jsonObject.getString("object");
                    if(bookDetail != null){
                        data.putString("bookDetail",bookDetail);
                    }
                } else {
                    tip = jsonObject.getString("tip");
                    c = jsonObject.getString("code");
                    data.putString("code", c);
                    data.putString("tip", tip);
                    data.putString("message", message);
                    data.putString("bookDetail",null);
                }
                msg.setData(data);
                msg.what = GET_BOOK_DETAIL;
                myHandler.sendMessage(msg);
            } else if (code == ADD_BOOK) {
                message = jsonObject.getString("message");
                c = jsonObject.getString("code");
                data.putString("code", c);
                data.putString("message", message);
                msg.setData(data);
                if ("???????????????".equals(message)) {
                    msg.what = ADD_BOOK_SUCCESS;
                } else {
                    tip = jsonObject.getString("tip");
                    data.putString("tip", tip);
                    msg.what = ADD_BOOK_FAIL;
                }
                myHandler.sendMessage(msg);
            } else if (code == UPDATE_BOOK) {
                message = jsonObject.getString("message");
                c = jsonObject.getString("code");
                msg = new Message();
                data = new Bundle();
                data.putString("code", c);
                data.putString("message", message);
                msg.setData(data);
                if ("???????????????".equals(message)) {
                    msg.what = UPDATE_BOOK_SUCCEED;
                } else {
                    tip = jsonObject.getString("tip");
                    data.putString("tip", tip);
                    msg.what = UPDATE_BOOK_FAIL;
                }
                myHandler.sendMessage(msg);
            } else if (code == DELETE_BOOK) {
                message = jsonObject.getString("message");
                c = jsonObject.getString("code");
                data.putString("code", c);
                data.putString("message", message);
                msg.setData(data);
                if ("???????????????".equals(message)) {
                    msg.what = DELETE_BOOK_SUCCEED;
                } else {
                    tip = jsonObject.getString("tip");
                    data.putString("tip", tip);
                    msg.what = DELETE_BOOK_FAIL;
                }
                myHandler.sendMessage(msg);
            } else {
                data.putString("reason", "????????????");
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
        bundle.putString("reason",reason);
        message.setData(bundle);
        myHandler.sendMessage(message);
    }
}