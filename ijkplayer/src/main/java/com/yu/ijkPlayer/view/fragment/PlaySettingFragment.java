package com.yu.ijkPlayer.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkPlayer.R;
import com.yu.ijkPlayer.R2;
import com.yu.ijkPlayer.bean.daoBean.PlaySetting;
import com.yu.ijkPlayer.bean.enumBean.PlayMode;
import com.yu.ijkPlayer.bean.enumBean.PlayScreenSize;
import com.yu.ijkPlayer.utils.DensityUtils;
import com.yu.ijkPlayer.view.controller.IjkPlayerControllerBottom;
import com.yu.ijkPlayer.view.wget.PlayerSettingButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yu on 2017/7/17 0017.
 */

public class PlaySettingFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = PlaySettingFragment.class.getSimpleName();
    /**
     * 依赖注入
     */
    @BindView(R2.id.ijk_player_setting_player_mode_box)
    GridLayout playModeGridLayout;
    @BindView(R2.id.ijk_player_setting_screen_box)
    GridLayout playScreenSizeGridLayout;
    /**
     * 播放模式 默认为循环
     */
    private static PlayMode PLAY_MODE = PlayMode.ALL_CYCLE;
    /**
     * 播放页面尺寸
     */
    private static PlayScreenSize PLAY_SCREEN_SIZE = PlayScreenSize.FIT_PARENT;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ijk_play_setting_fragment, container, false);
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
    private void initView() {
        playModeSetting();
        playScreenSizeSetting();
    }

    /**
     * 播放模式设置
     */
    private void playModeSetting() {
        if (ObjectUtil.isNotNull(this.playModeGridLayout)) {
            int colCount = this.playModeGridLayout.getColumnCount();
            for (int i = 0; i < PlayMode.values().length; i++) {
                final PlayerSettingButton playModeButton = new PlayerSettingButton(this.getContext());
                playModeButton.setType(PlayMode.values()[i]);
                playModeButton.setText(PlayMode.values()[i].getName());
                if (PLAY_MODE == PlayMode.values()[i]) {
                    playModeButton.setSelected(true);
                }
                playModeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlayerSettingButton view = (PlayerSettingButton) v;
                        PlaySetting playerSetting = PlaySetting.dao.getLatest();
                        playerSetting.setPlayMode(((PlayMode) view.getType()).getId());
                        EventBus.getDefault().post(playerSetting);
                        for (int j = 0; j < playModeGridLayout.getChildCount(); j++) {
                            playModeGridLayout.getChildAt(j).setSelected(false);
                        }
                        view.setSelected(true);
                    }
                });
                //由于方法重载，注意这个地方的1.0f 必须是float
                GridLayout.Spec rowSpec = GridLayout.spec(i / colCount, 1.0f);
                GridLayout.Spec columnSpec = GridLayout.spec(i % colCount, 1.0f);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
                this.playModeGridLayout.addView(playModeButton, params);
            }

        }
    }

    /**
     * 播放页面尺寸设置
     */
    private void playScreenSizeSetting() {
        if (ObjectUtil.isNotNull(this.playScreenSizeGridLayout)) {
            int colCount = this.playScreenSizeGridLayout.getColumnCount();
            for (int i = 0; i < PlayScreenSize.values().length; i++) {
                final PlayerSettingButton playModeButton = new PlayerSettingButton(this.getContext());
                playModeButton.setType(PlayScreenSize.values()[i]);
                playModeButton.setText(PlayScreenSize.values()[i].getName());
                if (PLAY_SCREEN_SIZE == PlayScreenSize.values()[i]) {
                    playModeButton.setSelected(true);
                }
                playModeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlayerSettingButton view = (PlayerSettingButton) v;
                        PlaySetting playerSetting = PlaySetting.dao.getLatest();
                        playerSetting.setPlayScreenSize(((PlayScreenSize) view.getType()).getId());
                        EventBus.getDefault().post(playerSetting);
                        for (int j = 0; j < playScreenSizeGridLayout.getChildCount(); j++) {
                            playScreenSizeGridLayout.getChildAt(j).setSelected(false);
                        }
                        view.setSelected(true);
                    }
                });
                //由于方法重载，注意这个地方的1.0f 必须是float
                GridLayout.Spec rowSpec = GridLayout.spec(i / colCount, 1.0f);
                GridLayout.Spec columnSpec = GridLayout.spec(i % colCount, 1.0f);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
                this.playScreenSizeGridLayout.addView(playModeButton, params);
            }

        }
    }

    /**
     * 点击事件回调
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

    }
/*
 * ==========================================================消息监听函数====================================
 */

    /**
     * 播放器设置监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlaySetting playSetting) {
        if (ObjectUtil.isNotNull(playSetting)) {
            PLAY_MODE = PlayMode.getPlayMode(playSetting.getPlayMode());
            PLAY_SCREEN_SIZE = PlayScreenSize.getPlayScreenSize(playSetting.getPlayScreenSize());
        }

    }


}
