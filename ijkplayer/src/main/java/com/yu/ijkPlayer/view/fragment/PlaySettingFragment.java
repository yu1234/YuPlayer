package com.yu.ijkPlayer.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yu.ijkPlayer.R;
import com.yu.ijkPlayer.R2;
import com.yu.ijkPlayer.bean.daoBean.PlaySetting;
import com.yu.ijkPlayer.bean.enumBean.PlayMode;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/7/17 0017.
 */

public class PlaySettingFragment extends BaseFragment {
    /**
     * 依赖注入
     */
    @BindView(R2.id.ijk_play_setting_all_cycle_btn)
    Button allCycleBtn;
    /**
     * 播放模式 默认为循环
     */
    private static PlayMode PLAY_MODE = PlayMode.ALL_CYCLE;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.ijk_play_setting_fragment,container,false);
        ButterKnife.bind(this, view);
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }
    /**
     * 初始化页面
     */
   private void initView(){

    }
/*
 * ==========================================================消息监听函数====================================
 */
    /**
     * 播放器设置监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlaySetting playSetting) {
        PLAY_MODE = PlayMode.getPlayMode(playSetting.getPlayMode());
    }

}
