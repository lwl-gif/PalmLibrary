package com.example.ul.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.PermissionChecker;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.adapter.BorrowBookListAdapter;
import com.example.ul.callback.CallbackToBorrowBookActivity;
import com.example.ul.model.Book;
import com.example.ul.model.ReaderPermission;
import com.example.ul.model.UserInfo;
import com.example.ul.myscan.android.CaptureActivity;
import com.example.ul.myscan.bean.ZxingConfig;
import com.example.ul.myscan.common.Constant;
import com.example.ul.reader.main.activity.RBookDetailActivity;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @author luoweili
 */
@SuppressLint("NonConstantResourceId")
public class BorrowBookActivity extends Activity implements HttpUtil.MyCallback, View.OnClickListener,
        CallbackToBorrowBookActivity {

    private final static String TAG = "BorrowBookActivity";
    /**
     * ????????????
     */
    private final int SCAN_BORROW = 1001;
    /**
     * ????????????
     */
    private final int SCAN_RETURN = 1002;
    /**
     * ????????????
     */
    private static final int UNKNOWN_REQUEST_ERROR = 1200;
    /**
     * ????????????
     */
    private static final int REQUEST_FAIL = 12000;
    /**
     * ???????????????????????????????????????
     */
    private static final int REQUEST_INTERCEPTED = 12001;
    /**
     * ???????????????????????????
     */
    private static final int GET_READER_PERMISSION = 1201;
    /**
     * ???????????????????????????
     */
    private static final int TEST_BOOK_STATUS = 1202;
    /**??????*/
    private static final int TEST_BOOK_STATUS_OK = 12021;
    /**????????????*/
    private static final int TEST_BOOK_STATUS_NO = 12020;
    /**
     * ??????
     */
    private static final int RETURN_BOOK = 1203;
    /**
     * ??????????????????
     */
    private static final int RETURN_BOOK_TIP = 12031;
    /**
     * ??????????????????
     */
    private static final int RETURN_BOOK_NO_TIP = 12030;
    /**
     * ??????
     */
    private static final int BORROW_BOOK = 1204;
    /**
     * ???????????????????????????
     */
    public static final String NOT_ALLOW_BORROW = "?????????";
    /**
     * ????????????
     */
    @BindView(R.id.readerId)
    public TextView rdId;
    @BindView(R.id.readerCredit)
    public TextView rdCredit;
    @BindView(R.id.readerAmount)
    public TextView rdAmount;
    @BindView(R.id.readerPermission)
    public TextView rdPermission;
    @BindView(R.id.readerTerm)
    public TextView rdTerm;
    @BindView(R.id.readerType)
    public TextView rdType;
    @BindView(R.id.borrowBookList)
    public RecyclerView borrowBookList;
    @BindView(R.id.imageView_back)
    public ImageView imageViewBack;
    @BindView(R.id.btn_borrow)
    public Button btnBorrow;
    @BindView(R.id.btn_return)
    public Button btnReturn;
    @BindView(R.id.btn_add_borrow_book)
    public Button btnAddBorrowBook;
    /**
     * ?????????
     */
    private BorrowBookListAdapter adapter;
    /**
     * ??????????????????
     */
    private ReaderPermission readerPermission = null;
    /**??????????????????*/
    private boolean allowBorrow = false;
    /**
     * token
     */
    private String token;

    static class MyHandler extends Handler {
        private final WeakReference<BorrowBookActivity> borrowBookActivity;

        public MyHandler(WeakReference<BorrowBookActivity> borrowBookActivity) {
            this.borrowBookActivity = borrowBookActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            BorrowBookActivity myActivity = borrowBookActivity.get();
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            } else if (what == TEST_BOOK_STATUS_OK) {
                Bundle bundle = msg.getData();
                Book book = (Book) bundle.getSerializable("book");
                myActivity.updateBookData(book);
            } else if(what == TEST_BOOK_STATUS_NO){
                Bundle bundle = msg.getData();
                JSONObject jsonObject = (JSONObject) bundle.getSerializable("jsonObject");
                String readerId = jsonObject.getString("readerId");
                int bookId = jsonObject.getInteger("bookId");
                DialogUtil.showDialog(myActivity,"???????????????"+readerId+"???????????????????????????,??????????????????????????????.",false);
                myActivity.adapter.deleteItemByBookId(bookId);
            } else if(what == BORROW_BOOK){
                Bundle bundle = msg.getData();
                String tip = bundle.getString("tip");
                int amount = bundle.getInt("amount");
                Toast.makeText(myActivity,tip,Toast.LENGTH_LONG).show();
                myActivity.adapter.updateItem(bundle.getIntegerArrayList("diff"));
                myActivity.adapter.setMaxAmount(amount);
            } else if (what == GET_READER_PERMISSION) {
                myActivity.fillReaderPermissionData();
            } else if(what == RETURN_BOOK){
                int a = myActivity.readerPermission.getAmount();
                myActivity.adapter.setMaxAmount(++a);
            } else if(what == RETURN_BOOK_NO_TIP){
                Bundle bundle = msg.getData();
                String message = bundle.getString("message");
                Toast.makeText(myActivity,message,Toast.LENGTH_SHORT).show();
            }else if(what == RETURN_BOOK_TIP){
                Bundle bundle = msg.getData();
                DialogUtil.showDialog(myActivity,TAG,bundle,false);
            } else if(what == REQUEST_INTERCEPTED){
                Bundle bundle = msg.getData();
                DialogUtil.showDialog(myActivity,TAG,bundle,true);
            }
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_borrow_book);
        ButterKnife.bind(this);
        imageViewBack.setOnClickListener(this);
        btnBorrow.setOnClickListener(this);
        btnAddBorrowBook.setOnClickListener(this);
        btnReturn.setOnClickListener(this);
        String readerId = getIntent().getStringExtra("readerId");
        if(readerId != null){
            // ??????token,??????readerId????????????????????????
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            token = userInfo.getToken();
            // ?????????????????????url
            String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
            // ?????????
            adapter = new BorrowBookListAdapter(this,token,baseUrl);
            //???RecyclerView?????????????????????
            borrowBookList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            borrowBookList.setAdapter(adapter);
            // ???????????????URL
            String url = HttpUtil.BASE_URL + "readerPermission/selectById";
            // ??????Map??????????????????
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", readerId);
            url = HttpUtil.newUrl(url, hashMap);
            HttpUtil.getRequest(token, url, this, GET_READER_PERMISSION);
        }else {
            DialogUtil.showDialog(this,"readerId?????????",true);
        }
    }

    private void updateBookData(Book newBook) {
        adapter.updateItem(newBook);
    }

    private void fillReaderPermissionData() {
        String permissionLevel = readerPermission.getPermissionName();
        rdId.setText(readerPermission.getId());
        rdCredit.setText(String.valueOf(readerPermission.getCredit()));
        int amount = readerPermission.getAmount();
        adapter.setMaxAmount(amount);
        rdPermission.setText(permissionLevel);
        Date date = readerPermission.getTerm();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String tvDate = format.format(date);
        rdTerm.setText(tvDate);
        rdType.setText(readerPermission.getTypeName());
        // ?????????
        if (permissionLevel.equals(NOT_ALLOW_BORROW)) {
            // ??????
            DialogUtil.showDialog(this, "?????????????????????????????????????????????????????????", false);
            allowBorrow = false;
        } else {
            allowBorrow = true;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            // ????????????
            case R.id.btn_return:
                if (readerPermission == null) {
                    Toast.makeText(this, "??????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                } else {
                    startCaptureActivity(SCAN_RETURN);
                }
                break;
            // ??????????????????
            case R.id.btn_add_borrow_book:
                if (readerPermission == null) {
                    Toast.makeText(this, "??????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                } else {
                    if(allowBorrow){
                        startCaptureActivity(SCAN_BORROW);
                    }else {
                        Toast.makeText(this, "??????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            // ????????????
            case R.id.imageView_back:
                BorrowBookActivity.this.finish();
                break;
            // ??????????????????
            case R.id.btn_borrow:
                    if (readerPermission == null) {
                        Toast.makeText(this, "???????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayList<Book> books = adapter.getBookArrayList();
                        if(books.size() <= 0){
                            DialogUtil.showDialog(this,"????????????????????????????????????",false);
                        }else {
                            ArrayList<String> bookIds = new ArrayList<>();
                            for(Book book : books){
                                bookIds.add(String.valueOf(book.getId()));
                            }
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("readerId",readerPermission.getId());
                            hashMap.put("bookIds",bookIds);
                            String url = HttpUtil.BASE_URL + "borrow/borrowBook";
                            HttpUtil.postRequest(token,url,hashMap,this,BORROW_BOOK,null);
                        }
                    }
                    break;
            default:
                Toast.makeText(this, "??????????????????????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClickToShowDetail(int position) {
        Book book = adapter.getBook(position);
        String bookId = String.valueOf(book.getId());
        Intent intent = new Intent(BorrowBookActivity.this, RBookDetailActivity.class);
        intent.putExtra("id", bookId);
        startActivity(intent);
    }

    @Override
    public void onClickToDeleteBook(int position) {
        adapter.deleteItem(position);
    }

    @Override
    public void changeAmount(int amount) {
        readerPermission.setAmount(amount);
        rdAmount.setText(String.valueOf(amount));
    }
    /**
     * @Author: Wallace
     * @Description: ?????????????????????????????????????????????????????????
     * @Date: Created 20:51 2021/4/27
     * @Modified: by who yyyy-MM-dd
     * @param bookId ?????????id
     * @return: void
     */
    private void testBookStatus(int bookId){
        String url = HttpUtil.BASE_URL + "book/testBookStatus";
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("bookId",String.valueOf(bookId));
        hashMap.put("readerId",readerPermission.getId());
        url = HttpUtil.newUrl(url,hashMap);
        HttpUtil.getRequest(token,url,this,TEST_BOOK_STATUS);
    }
    /**
     * @Author: Wallace
     * @Description: ????????????
     * @Date: Created 11:59 2021/4/27
     * @Modified: by who yyyy-MM-dd
     * @param bookId ??????id???????????????????????????
     * @return: void
     */
    private void returnBook(int bookId){
        String url = HttpUtil.BASE_URL + "borrow/returnBook";
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("readerId",readerPermission.getId());
        hashMap.put("bookId",String.valueOf(bookId));
        HttpUtil.postRequest(token,url,hashMap,this,RETURN_BOOK);
    }
    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }
    @Override
    public void success(Response response, int code) throws IOException {
        // ??????????????????????????????
        String result = Objects.requireNonNull(response.body()).string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        String m = jsonObject.getString("message");
        String t = jsonObject.getString("tip");
        String c = jsonObject.getString("code");
        Message msg = new Message();
        Bundle data = new Bundle();
        // ????????????true,?????????????????????
        if (HttpUtil.requestIsIntercepted(jsonObject)) {
            data.putString("code", c);
            data.putString("tip", t);
            data.putString("message", m);
            msg.setData(data);
            msg.what = REQUEST_INTERCEPTED;
            myHandler.sendMessage(msg);
        } else {
            switch (code) {
                case GET_READER_PERMISSION:
                    readerPermission = JSON.parseObject(jsonObject.getString("object"), ReaderPermission.class);
                    msg.what = GET_READER_PERMISSION;
                    myHandler.sendMessage(msg);
                    break;
                case TEST_BOOK_STATUS:
                    if ("????????????".equals(m)) {
                        Book book = JSON.parseObject(jsonObject.getString("object"), Book.class);
                        data.putSerializable("book", book);
                        msg.setData(data);
                        msg.what = TEST_BOOK_STATUS_OK;
                    } else {
                        JSONObject jsonObject1 = jsonObject.getJSONObject("dataObject");
                        data.putString("message", m);
                        data.putSerializable("jsonObject", jsonObject1);
                        msg.setData(data);
                        msg.what = TEST_BOOK_STATUS_NO;
                    }
                    myHandler.sendMessage(msg);
                    break;
                case BORROW_BOOK:
                    JSONObject jsonObject1 = jsonObject.getJSONObject("dataObject");
                    int amount = jsonObject1.getInteger("amount");
                    String stringDiff = jsonObject1.getString("diff");
                    ArrayList<Integer> diff = (ArrayList<Integer>) JSON.parseArray(stringDiff, Integer.class);
                    data.putIntegerArrayList("diff", diff);
                    data.putString("tip", t);
                    data.putInt("amount", amount);
                    msg.setData(data);
                    msg.what = BORROW_BOOK;
                    //??????????????????????????????UI??????
                    myHandler.sendMessage(msg);
                    break;
                case RETURN_BOOK:
                    if ("???????????????".equals(m)) {
                        // ????????????????????????????????????????????????
                        myHandler.sendEmptyMessage(RETURN_BOOK);
                    }
                    if("".equals(t)){
                        data.putString("message", m);
                        msg.setData(data);
                        msg.what = RETURN_BOOK_NO_TIP;
                    }else {
                        data.putString("message", m);
                        data.putString("code", c);
                        data.putString("tip", t);
                        msg.setData(data);
                        msg.what = RETURN_BOOK_TIP;
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

    /**
     * @param requestCode ?????????
     * @Author: Wallace
     * @Description: ??????????????????
     * @Date: Created 21:47 2021/4/26
     * @Modified: by who yyyy-MM-dd
     * @return: void
     */
    private void startCaptureActivity(int requestCode) {
        // ??????????????????
        if (!initPermission()) {
            new AlertDialog.Builder(BorrowBookActivity.this).setMessage("??????????????????????????????????????????????????????")
                    .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //?????????????????????????????????
                            ApplicationInfo(BorrowBookActivity.this);
                        }
                    }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        }
        // ?????????????????????
        Intent intent = new Intent(BorrowBookActivity.this, CaptureActivity.class);
        /*ZxingConfig???????????? ????????????????????????????????????????????????????????????????????????????????? ??????????????? * ??????????????????????????? * ???????????? ??????????????????????????? ????????????true * */
        ZxingConfig config = new ZxingConfig();
        config.setShowbottomLayout(true);
        //??????????????????????????????????????????
        config.setPlayBeep(true);
        //?????????????????????
        config.setShake(true);
        //????????????
        config.setShowAlbum(true);
        //??????????????????
        config.setShowFlashLight(true);
        //?????????????????????
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        if(requestCode == SCAN_BORROW){
            intent.putExtra("title","????????????");
        }else {
            intent.putExtra("title","????????????");
        }
        startActivityForResult(intent, requestCode);
    }

    /**?????????????????????????????????*/
    private boolean initPermission() {
        // ?????????Android??????????????????????????????
        String permissions = Manifest.permission.CAMERA;
        boolean ret = false;
        // Android 6.0????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // permission granted ??????????????????
            ret = PermissionChecker.checkSelfPermission(BorrowBookActivity.this, permissions) == PermissionChecker.PERMISSION_GRANTED;
        }
        return ret;
    }

    /**??????????????????????????????*/
    public static void ApplicationInfo(Activity activity) {
        try {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
            activity.startActivity(localIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            String content;
            Book book;
            switch (requestCode){
                case SCAN_BORROW:
                    // ???????????????
                    content = data.getStringExtra(Constant.CODED_CONTENT);
                    book = JSON.parseObject(content, Book.class);
                    if(book != null){
                        BorrowBookActivity.this.adapter.addItem(book);
                        BorrowBookActivity.this.testBookStatus(book.getId());
                    }
                    break;
                case SCAN_RETURN:
                    // ???????????????
                    content = data.getStringExtra(Constant.CODED_CONTENT);
                    Log.e(TAG, "onActivityResult: content = "+content);
                    book = JSON.parseObject(content, Book.class);
                    if(book != null){
                        BorrowBookActivity.this.returnBook(book.getId());
                    }
                    break;
                default:
                    Log.e(TAG, "onActivityResult: requestCode = " + requestCode);
            }
        }
    }
}