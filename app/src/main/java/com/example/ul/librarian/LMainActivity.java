package com.example.ul.librarian;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.alibaba.fastjson.JSON;

import com.example.ul.R;
import com.example.ul.activity.BorrowBookActivity;
import com.example.ul.librarian.main.activity.LBookDetailActivity;
import com.example.ul.librarian.main.activity.LReaderDetailActivity;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.librarian.main.activity.LShareDetailActivity;
import com.example.ul.model.Application;
import com.example.ul.model.Reader;
import com.example.ul.myscan.android.CaptureActivity;
import com.example.ul.myscan.bean.ZxingConfig;
import com.example.ul.myscan.common.Constant;
import com.example.ul.util.ActivityManager;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.PermissionChecker;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * @author luoweili
 */
public class LMainActivity extends AppCompatActivity implements CallbackToMainActivity {

    private static final String TAG = "LMainActivity";

    private AppBarConfiguration mAppBarConfiguration;
    /**扫码请求码*/
    private final int REQUEST_CODE_SCAN = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_l_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        //将每个菜单ID作为一组ID传递，因为每个菜单应被视为顶级目的地
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_main, R.id.nav_myself, R.id.nav_setting)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        //开启手势滑动打开侧滑菜单栏，如果要关闭手势滑动，将后面的UNLOCKED替换成LOCKED_CLOSED 即可
//        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.l_main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_AddBook:
                // 打开书本详情活动
                intent = new Intent(this, LBookDetailActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_AddReader:
                // 打开读者详情页面
                return true;
            case R.id.menu_Scan:
                // 打开扫码活动
                if (!initPermission()) {
                    new AlertDialog.Builder(LMainActivity.this).setMessage("没有开启摄像机权限，是否去设置开启？")
                            .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //调用系统内部去开启权限
                                    ApplicationInfo(LMainActivity.this);
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }
                // 跳到扫一扫页面
                intent = new Intent(LMainActivity.this, CaptureActivity.class);
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
                intent.putExtra("title","扫电子借阅卡");
                startActivityForResult(intent, REQUEST_CODE_SCAN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    @Override
    public void clickToGetReaderDetail(String id) {
        Intent intent = new Intent(LMainActivity.this, LReaderDetailActivity.class);
        intent.putExtra("readerId", id);
        startActivity(intent);
    }

    @Override
    public void clickToGetBookDetail(int id, String library, boolean edit) {
        Intent intent;
        if("读者书库".equals(library)){
            intent = new Intent(LMainActivity.this, LShareDetailActivity.class);
        }else {
            intent = new Intent(LMainActivity.this, LBookDetailActivity.class);
        }
        intent.putExtra("id", id);
        startActivity(intent);
    }

    @Override
    public void clickToGetBorrowDetail(int i) {

    }

    @Override
    public void clickToGetApplicationDetail(int id) {
        Intent intent = new Intent(LMainActivity.this, Application.class);
        intent.putExtra("id", id);
        intent.putExtra("TAG", TAG);
        startActivity(intent);
    }

    /**查看是否开启摄像头权限*/
    private boolean initPermission() {
        // 需要在Android里面找到你要开的权限
        String permissions = Manifest.permission.CAMERA;
        boolean ret = false;
        // Android 6.0以上才有动态权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // permission granted 说明权限开了
            ret = PermissionChecker.checkSelfPermission(LMainActivity.this, permissions) == PermissionChecker.PERMISSION_GRANTED;
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
//            if (Build.VERSION.SDK_INT >= 9) {
//                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
//                localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
//            }
//            else if (Build.VERSION.SDK_INT <= 8) {
//                localIntent.setAction(Intent.ACTION_VIEW);
//                localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
//                localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
//            }
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
//                String content = data.getStringExtra(Constant.CODED_CONTENT);
//                Reader reader = JSON.parseObject(content,Reader.class);
//                Log.e(TAG, "onActivityResult: reader = " + reader);
                Intent intent = new Intent(LMainActivity.this, BorrowBookActivity.class);
//                intent.putExtra("readerId", Objects.requireNonNull(reader).getId());
                intent.putExtra("readerId","0121710880503");
                startActivity(intent);
//                LMainActivity.this.finish();
            }
        }
    }
}