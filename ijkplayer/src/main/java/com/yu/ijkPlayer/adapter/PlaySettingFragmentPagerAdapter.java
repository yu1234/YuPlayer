package com.yu.ijkPlayer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;

import java.util.List;

/**
 * Created by igreentree on 2017/7/17 0017.
 */

public class PlaySettingFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private List<String> titles;

    public PlaySettingFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        if (CollectionUtil.isNotEmpty(fragments)) {
            return fragments.get(position);
        }
        return null;
    }

    @Override
    public int getCount() {
        if (CollectionUtil.isNotEmpty(fragments)) {
            return fragments.size();
        }
        return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (CollectionUtil.isNotEmpty(titles) && StrUtil.isNotBlank(titles.get(position))) {
            return titles.get(position);
        }
        return "";
    }
}
