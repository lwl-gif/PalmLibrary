package com.example.ul.librarian.main;

import android.os.Bundle;
import android.util.Log;
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

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

/**
 * @author luoweili
 */
public class LMainFragment extends Fragment {

    private static final String TAG = "LMainFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_l_main, container, false);
        Log.d(TAG, "onCreateView: rootView =" + rootView);
        FragmentManager manager = getChildFragmentManager();
        LMainFragmentPagerAdapter lMainFragmentPagerAdapter = new LMainFragmentPagerAdapter(this, manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        ViewPager viewPager = rootView.findViewById(R.id.l_main_fragment_view_pager);
        viewPager.setAdapter(lMainFragmentPagerAdapter);
        TabLayout tabs = rootView.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Objects.requireNonNull(tabs.getTabAt(i)).setCustomView(lMainFragmentPagerAdapter.getTabView(i));
        }
        return rootView;
    }
}