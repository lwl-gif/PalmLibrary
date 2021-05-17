package com.example.ul.librarian.main;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.ul.R;
import com.example.ul.fragment.BookFragment;
import com.example.ul.librarian.main.fragment.LApplicationFragment;
import com.example.ul.librarian.main.fragment.LReaderFragment;

import org.jetbrains.annotations.NotNull;

/**
 * @Author: Wallace
 * @Description:
 * @Date: 2021/3/23 22:46
 * @Modified By:
 */
public class LMainFragmentPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.l_tab_text_4, R.string.l_tab_text_2, R.string.l_tab_text_3};
    @StringRes
    private final int[] TAB_IMAGE = {R.drawable.book, R.drawable.apply, R.drawable.reader};

    private final Fragment parentFragment;

    public LMainFragmentPagerAdapter(Fragment parentFragment, FragmentManager fm, int i){
        super(fm,i);
        this.parentFragment = parentFragment;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new LApplicationFragment();
            case 2:
                return new LReaderFragment();
            case 0:
            default:
                return new BookFragment();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return parentFragment.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 3;
    }

    public View getTabView(int position){
        @SuppressLint("InflateParams") View v = LayoutInflater.from(parentFragment.getActivity()).inflate(R.layout.tab_item_view, null);
        ImageView iv = v.findViewById(R.id.tab_icon);
        TextView tv = v.findViewById(R.id.tab_text);
        iv.setBackgroundResource(TAB_IMAGE[position]);
        tv.setText(TAB_TITLES[position]);
        return v;
    }
}
