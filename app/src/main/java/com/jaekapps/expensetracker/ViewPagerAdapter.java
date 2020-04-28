package com.jaekapps.expensetracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> fragmentTitleList = new ArrayList<>();

    ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

    }

    public void addFragment(Fragment fragment, String title) {

        fragmentList.add(fragment);
        fragmentTitleList.add(title);

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        return fragmentList.get(position);

    }

    @Override
    public int getCount() {

        return fragmentTitleList.size();

    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        return fragmentTitleList.get(position);

    }
}