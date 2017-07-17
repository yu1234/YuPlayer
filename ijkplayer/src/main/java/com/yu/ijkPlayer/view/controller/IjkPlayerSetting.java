package com.yu.ijkPlayer.view.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yu.ijkPlayer.R;
import com.yu.ijkPlayer.R2;
import com.yu.ijkPlayer.adapter.PlaySettingFragmentPagerAdapter;
import com.yu.ijkPlayer.bean.enumBean.EventBusCode;
import com.yu.ijkPlayer.utils.ScreenUtils;
import com.yu.ijkPlayer.view.BaseActivity;
import com.yu.ijkPlayer.view.fragment.PlaySettingFragment;
import com.yu.ijkPlayer.view.fragment.ShowSettingFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/7/13 0013.
 */

public class IjkPlayerSetting extends LinearLayout {
    private static final String TAG = IjkPlayerSetting.class.getSimpleName();
    /**
     * 依附的activity
     **/
    private BaseActivity activity;
    /**
     * 控件注入
     */
    @BindView(R2.id.ijk_player_setting_tab)
    TabLayout ijkPlayerSettingTab;
    @BindView(R2.id.ijk_player_setting_view_pager)
    ViewPager ijkPlayerSettingViewPager;

    public IjkPlayerSetting(Context context) {
        this(context, null);
    }

    public IjkPlayerSetting(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkPlayerSetting(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.activity = (BaseActivity) context;
        //填充页面
        View view = LayoutInflater.from(context).inflate(R.layout.ijk_player_setting, this);
        ButterKnife.bind(this, view);
        //事件注册
        registerListener();
        //EventBus注册
        EventBus.getDefault().register(this);
        this.post(new Runnable() {
            @Override
            public void run() {
                //参数初始化
                initParams();
            }
        });
    }


    /**
     * 参数初始化
     */
    private void initParams() {
        //设置宽度
        Point point = ScreenUtils.getAppUsableScreenSize(this.activity);
        if (ObjectUtil.isNotNull(point)) {
            int w = point.x;
            ViewGroup.LayoutParams params = this.getLayoutParams();
            params.width = w / 2;
            this.setLayoutParams(params);
        }
    }

    /**
     * 事件注册
     */
    private void registerListener() {
        if (ObjectUtil.isNotNull(ijkPlayerSettingViewPager)) {
            List<Fragment> fragments = new ArrayList<>();
            fragments.add(new PlaySettingFragment());
            fragments.add(new ShowSettingFragment());
            if (ObjectUtil.isNotNull(ijkPlayerSettingTab)) {
                List<String> titles = new ArrayList<>();
                for (int i = 0; i < ijkPlayerSettingTab.getTabCount(); i++) {
                    TabLayout.Tab tab = ijkPlayerSettingTab.getTabAt(i);
                    if (ObjectUtil.isNotNull(tab) && StrUtil.isNotBlank(tab.getText())) {
                        titles.add(tab.getText().toString());
                    }
                }
                ijkPlayerSettingViewPager.setAdapter(new PlaySettingFragmentPagerAdapter(this.activity.getSupportFragmentManager(), fragments, titles));
                ijkPlayerSettingTab.setupWithViewPager(ijkPlayerSettingViewPager);
            }

        }
    }

    /**
     * ==========================================================消息监听函数====================================
     */
    /**
     * eventBus 消息监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusCode eventBusCode) {
    }
}
