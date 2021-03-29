package com.example.ul.activity.reader.main;

import android.content.Intent;
import android.os.Bundle;

import com.example.ul.R;
import com.example.ul.activity.reader.main.activity.RBookDetailActivity;
import com.example.ul.callback.CallbackTOMainActivity;
import com.example.ul.util.ActivityManager;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

public class RMainActivity extends AppCompatActivity implements CallbackTOMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_r_main);
        RMainActivityPagerAdapter rMainActivityPagerAdapter = new RMainActivityPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.r_main_activity_view_pager);
        viewPager.setAdapter(rMainActivityPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    @Override
    public void clickToGetReaderDetail(String id) {
        Intent intent = new Intent(RMainActivity.this, RBookDetailActivity.class);
        intent.putExtra("id",id);
        startActivity(intent);
    }

    @Override
    public void clickToGetBookDetail(String id) {

    }

    @Override
    public void clickToGetBorrowDetail(int i) {

    }

    @Override
    public void clickToGetApplicationDetail(int i) {

    }
}