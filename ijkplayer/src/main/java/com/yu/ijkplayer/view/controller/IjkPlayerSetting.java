package com.yu.ijkplayer.view.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkplayer.R;
import com.yu.ijkplayer.R2;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.utils.ScreenUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private Activity activity;
    /**
     * 控件注入
     */
    @BindView(R2.id.ijk_player_setting_tab)
    TabLayout ijkPlayerSettingTab;

    public IjkPlayerSetting(Context context) {
        this(context, null);
    }

    public IjkPlayerSetting(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkPlayerSetting(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.activity = (Activity) context;
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
