package com.example.ul.activity.reader.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ul.R;
import com.example.ul.activity.reader.main.activity.RBookDetailActivity;
import com.example.ul.callback.CallbackTOMainActivity;
import com.example.ul.activity.ReaderDetailActivity;
import com.example.ul.util.ActivityManager;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

public class RMainActivity extends AppCompatActivity implements CallbackTOMainActivity {

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.r_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_Person:
                //打开个人信息详情活动
                Intent intent = new Intent(this, ReaderDetailActivity.class);
                intent.putExtra("id","myself");
                startActivity(intent);
                return true;
            case R.id.menu_Setting:

                //打开个人设置页面
                return true;
            case R.id.menu_Scan:
                //打开扫码页面
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
    public void clickToGetBookDetail(String id) {
        Intent intent = new Intent(RMainActivity.this, RBookDetailActivity.class);
        intent.putExtra("id",id);
        startActivity(intent);
    }

    @Override
    public void clickToGetBorrowDetail(int i) {

    }

    @Override
    public void clickToGetApplicationDetail(int i) {

    }
}