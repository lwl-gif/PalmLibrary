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
import com.example.ul.R;
import com.example.ul.adapter.BorrowBookListAdapter;
import com.example.ul.callback.CallbackToBorrowBookActivity;
import com.example.ul.model.Book;
import com.example.ul.model.ReaderPermission;
import com.example.ul.model.UserInfo;
import com.example.ul.myscan.android.CaptureActivity;
import com.example.ul.myscan.bean.ZxingConfig;
import com.example.ul.myscan.common.Constant;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @author luoweili
 */
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
     * 网络异常，请求失败
     */
    private static final int REQUEST_FAIL = 12000;
    /**
     * 请求被服务器拦截，请求失败
     */
    private static final int REQUEST_INTERCEPTED = 12001;
    /**
     * 请求成功，但子线程解析数据失败
     */
    private static final int REQUEST_BUT_FAIL_READ_DATA = 12002;
    /**
     * 获取读者的权限信息
     */
    private static final int GET_READER_PERMISSION = 1201;
    /**
     * 获取书籍信息
     */
    private static final int GET_BOOK = 1202;

    /**
     * 权限级别对应的权限说明
     * 借阅权限为0，表明该读者可预约图书
     * 权限为1，则不可预约图书，但可以借阅图书
     * 权限为2，则禁止预约和借阅图书
     */
    public static final String ALLOW_RESERVE = "可预约";
    public static final String ALLOW_BORROW = "只借阅";
    public static final String NOT_ALLOW_BORROW = "无权限";
    public static final List<String> PERMISSION_LEVEL = new ArrayList<String>() {{
        add(ALLOW_RESERVE);
        add(ALLOW_BORROW);
        add(NOT_ALLOW_BORROW);
    }};
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
    @BindView(R.id.iv_return)
    public Button btnReturn;
    @BindView(R.id.iv_borrow)
    public Button btnBorrow;
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
            } else if (what == GET_BOOK) {
                //获取书本信息成功
                myActivity.fillBookData();
            } else if (what == GET_READER_PERMISSION) {
                myActivity.fillReaderPermissionData();
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
        String readerId = getIntent().getStringExtra("readerId");
        if(readerId != null){
            // 获取token,根据readerId获取读者权限信息
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            token = userInfo.getToken();
            // 获取图片的基本url
            String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
            ArrayList<Book> books = new ArrayList<>();
            Book book = new Book();
            String s = "乱写的";
            book.setId(1);
            book.setName(s);
            book.setAuthor(s);
            book.setDescription(s);
            book.setHot(1);
            book.setState(s);
            book.setTheme(s);
            book.setIsbn(s);
            book.setLibrary(s);
            book.setImages("1");
            ArrayList<String> pictures = new ArrayList<>();
            pictures.add("1.jpg");
            book.setPictures(pictures);
            books.add(book);
            // 适配器
            adapter = new BorrowBookListAdapter(this,token,baseUrl,books);
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

    private void fillBookData() {
        // 获取图片的基本url
        String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
        //将服务器响应包装成Adapter
//        adapter = new BookListAdapter(this,baseUrl,token,jsonArray,"id","name","author","description",
//                "hot","state","theme","isbn","library","images",null);
//        borrowBookList.setAdapter(adapter);
    }

    private void fillReaderPermissionData() {
        String permissionLevel = readerPermission.getPermissionName();
        // 无权限
        if (permissionLevel.equals(PERMISSION_LEVEL.get(2))) {
            // 提示，直接退出借书程序
            DialogUtil.showDialog(this, "检测到您无借阅权限，您无法借阅图书，请退出！", true);
        } else {
            rdId.setText(readerPermission.getId());
            rdCredit.setText(String.valueOf(readerPermission.getCredit()));
            rdAmount.setText(String.valueOf(readerPermission.getAmount()));
            rdPermission.setText(permissionLevel);
            rdTerm.setText(readerPermission.getTerm().toString());
            rdType.setText(readerPermission.getTypeName());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            // 还书按钮
            case R.id.iv_return:
                if (readerPermission == null) {
                    Toast.makeText(this, "请耐心等待一下，现在点我还没有用哦！", Toast.LENGTH_SHORT).show();
                } else {
                    startCaptureActivity(SCAN_BORROW);
                }
                break;
            // 借书按钮
            case R.id.iv_borrow:
                if (readerPermission == null) {
                    Toast.makeText(this, "请耐心等待一下，现在点我也没有用哦！", Toast.LENGTH_SHORT).show();
                } else {
                    startCaptureActivity(SCAN_RETURN);
                }
                break;
            // 返回按钮
            case R.id.imageView_back:
                this.finish();
                break;
            default:
        }
    }


    @Override
    public void onClickToShowDetail(int position) {
        DialogUtil.showDialog(this,"查看详情！",false);
    }

    @Override
    public void onClickToDeleteBook(int position) {
        DialogUtil.showDialog(this,"删除书本！",false);
    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    @Override
    public void success(Response response, int code) throws IOException {
        //获取服务器响应字符串
        String result = response.body().string().trim();
        JSONObject jsonObject = null;
        Message msg = new Message();
        try {
            jsonObject = new JSONObject(result);
            //返回值为true,说明请求被拦截
            if (HttpUtil.requestIsIntercepted(jsonObject)) {
                Bundle data = new Bundle();
                data.putString("code", jsonObject.getString("code"));
                data.putString("tip", jsonObject.getString("tip"));
                data.putString("message", jsonObject.getString("message"));
                msg.setData(data);
                msg.what = REQUEST_INTERCEPTED;
                myHandler.sendMessage(msg);
            } else {
                switch (code) {
                    case GET_READER_PERMISSION:
                        try {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("object");
                            readerPermission = new ReaderPermission();
                            readerPermission.setId(jsonObject1.getString("id"));
                            readerPermission.setCredit(jsonObject1.getInt("credit"));
                            readerPermission.setAmount(jsonObject1.getInt("amount"));
                            readerPermission.setPermissionName(jsonObject1.getString("permissionName"));
                            long l = jsonObject1.getLong("term");
                            Date date = new Date(l);
                            readerPermission.setTerm(date);
                            readerPermission.setTypeName(jsonObject1.getString("typeName"));
                            Log.e(TAG, "success: readerPermission.id = " + readerPermission.getId());
                            Log.e(TAG, "success: readerPermission.credit = " + readerPermission.getCredit());
                            Log.e(TAG, "success: readerPermission.amount = " + readerPermission.getAmount());
                            Log.e(TAG, "success: readerPermission.permissionName = " + readerPermission.getPermissionName());
                            Log.e(TAG, "success: readerPermission.term = " + readerPermission.getTerm());
                            Log.e(TAG, "success: readerPermission.typeName = " + readerPermission.getTypeName());
                            msg.what = GET_READER_PERMISSION;
                            //发消息通知主线程进行UI更新
                            myHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            msg.what = REQUEST_BUT_FAIL_READ_DATA;
                            myHandler.sendMessage(msg);
                        }
                        break;
                    case GET_BOOK:
                        try {
                            JSONObject book = jsonObject.getJSONObject("jsonObject");
                            Bundle data = new Bundle();
                            data.putString("bookId", book.getString("id"));
                            data.putString("images", book.getString("images"));
                            data.putString("pictureName", book.getString("pictureName"));
                            data.putString("state", book.getString("state"));
                            msg.what = GET_BOOK;
                            //发消息通知主线程进行UI更新
                            myHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            msg.what = REQUEST_BUT_FAIL_READ_DATA;
                            myHandler.sendMessage(msg);
                        }
                        break;
                    default:
                        myHandler.sendEmptyMessage(UNKNOWN_REQUEST_ERROR);
                }
            }
        } catch (JSONException e) {
            myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
        }
    }

    @Override
    public void failed(IOException e, int code) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        String reason = null;
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
        startActivityForResult(intent, requestCode);
    }

    /**查看是否开启摄像头权限*/
    private boolean initPermission() {
        //需要在Android里面找到你要开的权限
        String permissions = Manifest.permission.CAMERA;
        boolean ret = false;
        //Android 6.0以上才有动态权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //permission granted 说明权限开了
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
            switch (requestCode){
                case SCAN_BORROW:
                    // 扫码的结果
                    String content = data.getStringExtra(Constant.CODED_CONTENT);
                    try {
                        JSONObject jsonObject = new JSONObject(content);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "onActivityResult: content = " + content);

                    break;
                case SCAN_RETURN:

                    break;
                default:
            }
        }

    }
}