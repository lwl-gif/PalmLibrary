package com.example.ul.reader.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

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
    /**扫码请求码*/
    private final int REQUEST_CODE_SCAN = 1001;
    /**转借(入)*/
    private static final int LENT_BOOK = 2101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_r_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RMainActivityPagerAdapter rMainActivityPagerAdapter = new RMainActivityPagerAdapter(this, getSupportFragmentManager());
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
                // 打开个人信息详情活动
                intent = new Intent(this, RReaderDetailActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_Card:
                // 电子卡
                intent = new Intent(this, CardActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_Setting:
                // 打开个人设置页面
                return true;
            case R.id.menu_Scan:
                // 打开扫码页面
                if (!initPermission()) {
                    new AlertDialog.Builder(RMainActivity.this).setMessage("没有开启摄像机权限，是否去设置开启？")
                            .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //调用系统内部去开启权限
                                    applicationInfo(RMainActivity.this);
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }
                // 跳到扫一扫页面
                intent = new Intent(RMainActivity.this, CaptureActivity.class);
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
                intent.putExtra("title","扫一扫");
                startActivityForResult(intent, REQUEST_CODE_SCAN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**查看是否开启摄像头权限*/
    private boolean initPermission() {
        // 需要在Android里面找到你要开的权限
        String permissions = Manifest.permission.CAMERA;
        boolean ret = false;
        // Android 6.0以上才有动态权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // permission granted 说明权限开了
            ret = PermissionChecker.checkSelfPermission(RMainActivity.this, permissions) == PermissionChecker.PERMISSION_GRANTED;
        }
        return ret;
    }

    /**调用系统内部开启权限*/
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
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                // 扫码的结果
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                JSONObject jsonObject = JSONObject.parseObject(content);
                String readerId = jsonObject.getString("readerId");
                String bookId = jsonObject.getString("bookId");
                String createTime = jsonObject.getString("createTime");
                if(readerId != null && bookId != null && createTime != null){
                    // 发送转借（入）请求
                    UserManager userManager = UserManager.getInstance();
                    UserInfo userInfo = userManager.getUserInfo(this);
                    String token = userInfo.getToken();
                    HashMap<String, String> hashMap = new HashMap<>(4);
                    hashMap.put("oldReaderId",readerId);
                    hashMap.put("bookId",bookId);
                    hashMap.put("createTime",createTime);
                    String url = HttpUtil.BASE_URL + "borrow/lentBookIn";
                    HttpUtil.postRequest(token,url,hashMap,this,LENT_BOOK);
                    Toast.makeText(this,"正在为您借入图书",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"二维码解析失败！",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    @Override
    public void clickToGetReaderDetail(String id) {

    }

    @Override
    public void clickToGetBookDetail(int id, String library, boolean edit) {
        Intent intent;
        if("读者书库".equals(library)){
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

    @Override
    public void success(Response response, int code) throws IOException {
        // 获取服务器响应字符串
        String result = response.body().string().trim();
        JSONObject jsonObject = JSON.parseObject(result);
        // 返回值为true,说明请求被拦截
        if (HttpUtil.requestIsIntercepted(jsonObject)) {
            Bundle bundle = new Bundle();
            String message = jsonObject.getString("message");
            String c = jsonObject.getString("code");
            String tip = jsonObject.getString("tip");
            bundle.putString("message",message);
            bundle.putString("code",c);
            bundle.putString("tip",tip);
            DialogUtil.showDialog(this,TAG,bundle,true);
        } else {
            if(code == LENT_BOOK){
                String message = jsonObject.getString("message");
                if("借入成功！".equals(message)){
                    Intent intent = new Intent(RMainActivity.this, RBorrowFragment.class);
                    startActivity(intent);
                }else {
                    Bundle bundle = new Bundle();
                    String c = jsonObject.getString("code");
                    String tip = jsonObject.getString("tip");
                    bundle.putString("message",message);
                    bundle.putString("code",c);
                    bundle.putString("tip",tip);
                    DialogUtil.showDialog(this,TAG,bundle,false);
                }
            }else {
                Toast.makeText(this,"未知请求，无法处理",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void failed(IOException e, int code) {
        String reason;
        if (e instanceof SocketTimeoutException) {
            reason = "连接超时";
        } else if (e instanceof ConnectException) {
            reason = "连接服务器失败";
        } else if (e instanceof UnknownHostException) {
            reason = "网络异常";
        } else {
            reason = "未知错误";
        }
        Toast.makeText(this,reason,Toast.LENGTH_SHORT).show();
    }
}