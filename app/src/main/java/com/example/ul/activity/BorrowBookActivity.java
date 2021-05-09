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
     * 扫码借书
     */
    private final int SCAN_BORROW = 1001;
    /**
     * 扫码还书
     */
    private final int SCAN_RETURN = 1002;
    /**
     * 未知错误
     */
    private static final int UNKNOWN_REQUEST_ERROR = 1200;
    /**
     * 请求失败
     */
    private static final int REQUEST_FAIL = 12000;
    /**
     * 请求被服务器拦截，请求失败
     */
    private static final int REQUEST_INTERCEPTED = 12001;
    /**
     * 获取读者的权限信息
     */
    private static final int GET_READER_PERMISSION = 1201;
    /**
     * 获取图书是否可借阅
     */
    private static final int TEST_BOOK_STATUS = 1202;
    /**可借*/
    private static final int TEST_BOOK_STATUS_OK = 12021;
    /**不可借阅*/
    private static final int TEST_BOOK_STATUS_NO = 12020;
    /**
     * 还书
     */
    private static final int RETURN_BOOK = 1203;
    /**
     * 操作后有提示
     */
    private static final int RETURN_BOOK_TIP = 12031;
    /**
     * 操作后无提示
     */
    private static final int RETURN_BOOK_NO_TIP = 12030;
    /**
     * 借书
     */
    private static final int BORROW_BOOK = 1204;
    /**
     * 禁止预约和借阅图书
     */
    public static final String NOT_ALLOW_BORROW = "无权限";
    /**
     * 页面控件
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
     * 适配器
     */
    private BorrowBookListAdapter adapter;
    /**
     * 当前读者权限
     */
    private ReaderPermission readerPermission = null;
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
                DialogUtil.showDialog(myActivity,"非常抱歉！"+readerId+"您不可以借阅该图书,已帮您从该列表中删除.",false);
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
            // 获取token,根据readerId获取读者权限信息
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            token = userInfo.getToken();
            // 获取图片的基本url
            String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
            // 适配器
            adapter = new BorrowBookListAdapter(this,token,baseUrl);
            //为RecyclerView设置布局管理器
            borrowBookList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            borrowBookList.setAdapter(adapter);
            // 定义发送的URL
            String url = HttpUtil.BASE_URL + "readerPermission/selectById";
            // 使用Map封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", readerId);
            url = HttpUtil.newUrl(url, hashMap);
            HttpUtil.getRequest(token, url, this, GET_READER_PERMISSION);
        }else {
            DialogUtil.showDialog(this,"readerId为空！",true);
        }
    }

    private void updateBookData(Book newBook) {
        adapter.updateItem(newBook);
    }

    private void fillReaderPermissionData() {
        String permissionLevel = readerPermission.getPermissionName();
        // 无权限
        if (permissionLevel.equals(NOT_ALLOW_BORROW)) {
            // 提示，直接退出借书程序
            DialogUtil.showDialog(this, "检测到您无借阅权限，您无法借阅图书，请退出！", true);
        } else {
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
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            // 还书按钮
            case R.id.btn_return:
                if (readerPermission == null) {
                    Toast.makeText(this, "请耐心等待一下，现在点我还没有用哦！", Toast.LENGTH_SHORT).show();
                } else {
                    startCaptureActivity(SCAN_RETURN);
                }
                break;
            // 添加图书按钮
            case R.id.btn_add_borrow_book:
                if (readerPermission == null) {
                    Toast.makeText(this, "请耐心等待一下，现在点我也没有用哦！", Toast.LENGTH_SHORT).show();
                } else {
                    startCaptureActivity(SCAN_BORROW);
                }
                break;
            // 返回按钮
            case R.id.imageView_back:
                BorrowBookActivity.this.finish();
                break;
            // 完成借书操作
            case R.id.btn_borrow:
                    if (readerPermission == null) {
                        Toast.makeText(this, "请耐心等待一下，我还不知道你是谁，无法帮你借书哦！", Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayList<Book> books = adapter.getBookArrayList();
                        if(books.size() <= 0){
                            DialogUtil.showDialog(this,"您还没添加要借的图书哦！",false);
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
                Toast.makeText(this, "不知道你按了啥，你想干嘛？！", Toast.LENGTH_SHORT).show();
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
     * @Description: 获取书籍的详情，再判断该书籍能否被借阅
     * @Date: Created 20:51 2021/4/27
     * @Modified: by who yyyy-MM-dd
     * @param bookId 书籍的id
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
     * @Description: 读者还书
     * @Date: Created 11:59 2021/4/27
     * @Modified: by who yyyy-MM-dd
     * @param bookId 书籍id，表明还的是哪本书
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
        // 获取服务器响应字符串
        String result = Objects.requireNonNull(response.body()).string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        String m = jsonObject.getString("message");
        String t = jsonObject.getString("tip");
        String c = jsonObject.getString("code");
        Message msg = new Message();
        Bundle data = new Bundle();
        // 返回值为true,说明请求被拦截
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
                    if ("可借阅！".equals(m)) {
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
                    //发消息通知主线程进行UI更新
                    myHandler.sendMessage(msg);
                    break;
                case RETURN_BOOK:
                    if ("还书成功！".equals(m)) {
                        // 先发一次消息，通知修改剩余借阅量
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

    /**
     * @param requestCode 请求码
     * @Author: Wallace
     * @Description: 打开扫码活动
     * @Date: Created 21:47 2021/4/26
     * @Modified: by who yyyy-MM-dd
     * @return: void
     */
    private void startCaptureActivity(int requestCode) {
        // 动态检测权限
        if (!initPermission()) {
            new AlertDialog.Builder(BorrowBookActivity.this).setMessage("没有开启摄像机权限，是否去设置开启？")
                    .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //调用系统内部去开启权限
                            ApplicationInfo(BorrowBookActivity.this);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        }
        // 跳到扫一扫页面
        Intent intent = new Intent(BorrowBookActivity.this, CaptureActivity.class);
        /*ZxingConfig是配置类 可以设置是否显示底部布局，闪光灯，相册，是否播放提示音 震动等动能 * 也可以不传这个参数 * 不传的话 默认都为默认不震动 其他都为true * */
        ZxingConfig config = new ZxingConfig();
        config.setShowbottomLayout(true);
        //底部布局（包括闪光灯和相册）
        config.setPlayBeep(true);
        //是否播放提示音
        config.setShake(true);
        //是否震动
        config.setShowAlbum(true);
        //是否显示相册
        config.setShowFlashLight(true);
        //是否显示闪光灯
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        if(requestCode == SCAN_BORROW){
            intent.putExtra("title","扫码借书");
        }else {
            intent.putExtra("title","扫码还书");
        }
        startActivityForResult(intent, requestCode);
    }

    /**查看是否开启摄像头权限*/
    private boolean initPermission() {
        // 需要在Android里面找到你要开的权限
        String permissions = Manifest.permission.CAMERA;
        boolean ret = false;
        // Android 6.0以上才有动态权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // permission granted 说明权限开了
            ret = PermissionChecker.checkSelfPermission(BorrowBookActivity.this, permissions) == PermissionChecker.PERMISSION_GRANTED;
        }
        return ret;
    }

    /**调用系统内部开启权限*/
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
                    // 扫码的结果
                    content = data.getStringExtra(Constant.CODED_CONTENT);
                    book = JSON.parseObject(content, Book.class);
                    if(book != null){
                        BorrowBookActivity.this.adapter.addItem(book);
                        BorrowBookActivity.this.testBookStatus(book.getId());
                    }
                    Log.e(TAG, "onActivityResult: SCAN_BORROW = " + requestCode);
                    break;
                case SCAN_RETURN:
                    // 扫码的结果
                    content = data.getStringExtra(Constant.CODED_CONTENT);
                    book = JSON.parseObject(content, Book.class);
                    if(book != null){
                        BorrowBookActivity.this.returnBook(book.getId());
                    }
                    Log.e(TAG, "onActivityResult: SCAN_RETURN = " + requestCode);
                    break;
                default:
                    Log.e(TAG, "onActivityResult: requestCode = " + requestCode);
            }
        }
    }
}