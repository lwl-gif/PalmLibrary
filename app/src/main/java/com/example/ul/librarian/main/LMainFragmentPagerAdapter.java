package com.example.ul.librarian.main;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.ul.R;
import com.example.ul.fragment.BookFragment;
import com.example.ul.librarian.main.fragment.LApplicationManageFragment;
import com.example.ul.librarian.main.fragment.LNotificationFragment;
import com.example.ul.librarian.main.fragment.LReaderManageFragment;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/3/23 22:46
 * @Modified By:
 */
public class LMainFragmentPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.l_tab_text_1, R.string.l_tab_text_2, R.string.l_tab_text_3, R.string.l_tab_text_4};
    @StringRes
    private int[] TAB_IMAGE = {R.drawable.notice, R.drawable.apply, R.drawable.reader, R.drawable.book};

    private final Fragment parentFragment;

    public LMainFragmentPagerAdapter(Fragment parentFragment, FragmentManager fm ) {
        super(fm);
        this.parentFragment = parentFragment;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new LNotificationFragment();
            case 1:
                return new LApplicationManageFragment();
            case 2:
                return new LReaderManageFragment();
            case 3:
                return new BookFragment();
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return parentFragment.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 4;
    }

    public View getTabView(int position){
        View v = LayoutInflater.from(parentFragment.getActivity()).inflate(R.layout.tab_item_view, null);
        ImageView iv = v.findViewById(R.id.tab_icon);
        TextView tv = v.findViewById(R.id.tab_text);
        iv.setBackgroundResource(TAB_IMAGE[position]);
        tv.setText(TAB_TITLES[position]);
        return v;
    }
}
