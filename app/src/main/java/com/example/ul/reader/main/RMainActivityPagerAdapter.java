package com.example.ul.reader.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.example.ul.R;
import com.example.ul.reader.main.fragment.RApplicationFragment;
import com.example.ul.fragment.BookFragment;
import com.example.ul.reader.main.fragment.RBorrowFragment;
import com.example.ul.reader.main.fragment.RShareFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 * @author luoweili
 */
public class RMainActivityPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.r_tab_text_1, R.string.r_tab_text_2, R.string.r_tab_text_3, R.string.r_tab_text_4};
    private final Context mContext;

    public RMainActivityPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new BookFragment();
            case 2:
                return new RShareFragment();
            case 3:
                return new RApplicationFragment();
            case 0:
            default:
                return new RBorrowFragment();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 4;
    }
}