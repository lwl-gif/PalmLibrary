package com.example.ul.librarian.main;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import com.example.ul.R;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class LMainFragment extends Fragment {

    private static final String LAG = "LMainFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_l_main, container, false);
        FragmentManager manager = getChildFragmentManager();
        LMainFragmentPagerAdapter lMainFragmentPagerAdapter = new LMainFragmentPagerAdapter(this, manager);
        ViewPager viewPager = rootView.findViewById(R.id.l_main_fragment_view_pager);
        viewPager.setAdapter(lMainFragmentPagerAdapter);
        TabLayout tabs = rootView.findViewById(R.id.tabs);
        Drawable d = null;
        tabs.setupWithViewPager(viewPager);
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Objects.requireNonNull(tabs.getTabAt(i)).setCustomView(lMainFragmentPagerAdapter.getTabView(i));
        }
        return rootView;
    }
}