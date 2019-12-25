package com.btsoft.opencvapp2.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.btsoft.opencvapp2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final List<String> FragmentTitles = new ArrayList<>();
    private static final List<Fragment> FragmentListArray = new ArrayList<>();

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentListArray.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return FragmentTitles.get(position);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return FragmentTitles.size();
    }

    public void AddFragment(Fragment fragment, String Title) {
        FragmentListArray.add(fragment);
        FragmentTitles.add(Title);
    }
}