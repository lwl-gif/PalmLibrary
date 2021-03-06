package com.example.ul.reader.main;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.activity.ApplicationDetailActivity;

import com.example.ul.activity.CardActivity;

import com.example.ul.librarian.main.activity.LShareDetailActivity;
import com.example.ul.model.UserInfo;
import com.example.ul.myscan.android.CaptureActivity;
import com.example.ul.myscan.bean.ZxingConfig;
import com.example.ul.myscan.common.Constant;
import com.example.ul.reader.main.activity.RBookDetailActivity;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.reader.main.activity.RReaderDetailActivity;
import com.example.ul.reader.main.activity.RShareDetailActivity;
import com.example.ul.reader.main.fragment.RBorrowFragment;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.PermissionChecker;
import androidx.viewpager.widget.ViewPager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import okhttp3.Response;
/**
 * @author luoweili
 */
public class RMainActivity extends AppCompatActivity implements CallbackToMainActivity, HttpUtil.MyCallback {

    private static final String TAG = "RMainActivity";
    /**???????????????*/
    private final int REQUEST_CODE_SCAN = 1001;
    /**????????????*/
    private static final int UNKNOWN_REQUEST_ERROR = 2100;
    /**????????????*/
    private static final int REQUEST_FAIL = 21000;
    /**???????????????????????????????????????*/
    private static final int REQUEST_INTERCEPTED = 21001;
    /**??????(???)*/
    private static final int LENT_BOOK = 2101;
    private static final int LENT_BOOK_OK = 21011;
    private static final int LENT_BOOK_FAIL = 21010;

    RMainActivityPagerAdapter rMainActivityPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_r_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rMainActivityPagerAdapter = new RMainActivityPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.r_main_activity_view_pager);
        viewPager.setAdapter(rMainActivityPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.r_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent;
        switch (item.getItemId()){
            case R.id.menu_Person:
                // ??????????????????????????????
                intent = new Intent(this, RReaderDetailActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_Card:
                // ?????????
                intent = new Intent(this, CardActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_Setting:
                // ????????????????????????
                return true;
            case R.id.menu_Scan:
                // ??????????????????
                if (!initPermission()) {
                    new AlertDialog.Builder(RMainActivity.this).setMessage("??????????????????????????????????????????????????????")
                            .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //?????????????????????????????????
                                    applicationInfo(RMainActivity.this);
                                }
                            }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }
                // ?????????????????????
                intent = new Intent(RMainActivity.this, CaptureActivity.class);
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
                intent.putExtra("title","?????????");
                startActivityForResult(intent, REQUEST_CODE_SCAN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**?????????????????????????????????*/
    private boolean initPermission() {
        // ?????????Android??????????????????????????????
        String permissions = Manifest.permission.CAMERA;
        boolean ret = false;
        // Android 21.0????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // permission granted ??????????????????
            ret = PermissionChecker.checkSelfPermission(RMainActivity.this, permissions) == PermissionChecker.PERMISSION_GRANTED;
        }
        return ret;
    }

    /**??????????????????????????????*/
    public static void applicationInfo(Activity activity) {
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
        // ???????????????/????????????
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                // ???????????????
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                JSONObject jsonObject = JSONObject.parseObject(content);
                String readerId = jsonObject.getString("readerId");
                String bookId = jsonObject.getString("bookId");
                String createTime = jsonObject.getString("createTime");
                if(readerId != null && bookId != null && createTime != null){
                    // ???????????????????????????
                    UserManager userManager = UserManager.getInstance();
                    UserInfo userInfo = userManager.getUserInfo(this);
                    String token = userInfo.getToken();
                    HashMap<String, String> hashMap = new HashMap<>(4);
                    hashMap.put("oldReaderId",readerId);
                    hashMap.put("bookId",bookId);
                    hashMap.put("createTime",createTime);
                    String url = HttpUtil.BASE_URL + "borrow/lentBookIn";
                    HttpUtil.postRequest(token,url,hashMap,this,LENT_BOOK);
                    Toast.makeText(this,"????????????????????????",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"????????????????????????",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivity(this);
    }

    @Override
    public void clickToGetReaderDetail(String id) {

    }

    @Override
    public void clickToGetBookDetail(int id, String library, boolean edit) {
        Intent intent;
        if("????????????".equals(library)){
            if(edit){
                intent = new Intent(RMainActivity.this, LShareDetailActivity.class);
            }else {
                intent = new Intent(RMainActivity.this, RShareDetailActivity.class);
            }
        }else {
            intent = new Intent(RMainActivity.this, RBookDetailActivity.class);
        }
        intent.putExtra("id",id);
        startActivity(intent);
    }

    @Override
    public void clickToGetBorrowDetail(int i) {

    }

    @Override
    public void clickToGetApplicationDetail(int id) {
        Intent intent = new Intent(RMainActivity.this, ApplicationDetailActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("TAG", TAG);
        startActivity(intent);
    }

    private void lentIn() {
        // ???????????????????????????
        rMainActivityPagerAdapter.getItem(0);
    }

    static class MyHandler extends Handler {
        private WeakReference<RMainActivity> rMainActivityWeakReference;
        public MyHandler(WeakReference<RMainActivity> rMainActivity){
            this.rMainActivityWeakReference = rMainActivity;
        }
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            RMainActivity myActivity = rMainActivityWeakReference.get();
            if(what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle data = msg.getData();
                Toast.makeText(myActivity,data.getString("reason"), Toast.LENGTH_SHORT).show();
            } else if (what == LENT_BOOK_OK) {
                myActivity.lentIn();
            } else {
                Bundle bundle = msg.getData();
                DialogUtil.showDialog(myActivity, TAG, bundle, what == REQUEST_INTERCEPTED);
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
        // ????????????true,?????????????????????
        if (HttpUtil.requestIsIntercepted(jsonObject)) {
            String message = jsonObject.getString("message");
            String c = jsonObject.getString("code");
            String tip = jsonObject.getString("tip");
            bundle.putString("message",message);
            bundle.putString("code",c);
            bundle.putString("tip",tip);
            msg.setData(bundle);
            msg.what = REQUEST_INTERCEPTED;
            myHandler.sendMessage(msg);
        } else {
            if(code == LENT_BOOK){
                String message = jsonObject.getString("message");
                if("???????????????".equals(message)){
                    msg.what = LENT_BOOK_OK;
                }else {
                    String c = jsonObject.getString("code");
                    String tip = jsonObject.getString("tip");
                    bundle.putString("message",message);
                    bundle.putString("code",c);
                    bundle.putString("tip",tip);
                    msg.setData(bundle);
                    msg.what = LENT_BOOK_FAIL;
                    myHandler.sendMessage(msg);
                }
            }else {
                bundle.putString("reason","????????????");
                msg.setData(bundle);
                msg.what = UNKNOWN_REQUEST_ERROR;
                myHandler.sendMessage(msg);
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