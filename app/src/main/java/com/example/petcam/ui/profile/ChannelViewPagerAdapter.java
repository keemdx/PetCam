package com.example.petcam.ui.profile;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ChannelViewPagerAdapter extends FragmentStatePagerAdapter {

    private final int MAX_PAGES;
    private final ArrayList<Fragment> fragmentList = new ArrayList<>();
    private final ArrayList<String> fragmentTitle = new ArrayList<>();

    public ChannelViewPagerAdapter(@NonNull FragmentManager fm, int maxPages) {
        super(fm, maxPages);
        this.MAX_PAGES = maxPages;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) { // 화면의 실제 Fragment를 반환
        return fragmentList.get(position);
    }

    @Override
    public int getCount() { // Page 개수
        return fragmentList.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {

        return POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) { // 페이지 타이틀
        return fragmentTitle.get(position);
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        fragmentTitle.add(title);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
