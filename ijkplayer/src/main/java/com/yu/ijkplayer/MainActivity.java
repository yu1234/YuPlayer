package com.yu.ijkplayer;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.bean.PlayerListenerEnum;
import com.yu.ijkplayer.bean.PlayerSettingEnum;
import com.yu.ijkplayer.bean.VideoijkBean;
import com.yu.ijkplayer.utils.PlayerUtil;
import com.yu.ijkplayer.utils.ScreenRotateUtil;
import com.yu.ijkplayer.view.controller.IjkPlayerView;
import com.yu.ijkplayer.view.playerView.PlayStateParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yu on 2017/6/28 0028.
 */

public class MainActivity extends AppCompatActivity {
    @BindView(R2.id.ijk_player_view)
    IjkPlayerView player;
    @BindView(R2.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ijk_player);
        ButterKnife.bind(this);
        //注册重力感应
        ScreenRotateUtil.getInstance(this).start(this);
        //EventBus注册
        EventBus.getDefault().register(this);
        //参数初始化
        initParams();
        //初始化播放器
        initPlayer();
    }

    /**
     * 参数初始化
     */
    private void initParams() {

        if (ObjectUtil.isNotNull(this.drawerLayout)) {
            //禁止手势滑动
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            //去除阴影
            drawerLayout.setScrimColor(Color.TRANSPARENT);
            drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {

                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    EventBus.getDefault().post(PlayerListenerEnum.IN_PAUSE);

                    EventBus.getDefault().post(PlayerSettingEnum.OUT_SHOW );
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    //TODO
                    EventBus.getDefault().post(PlayerListenerEnum.IN_RESUME);

                    EventBus.getDefault().post(PlayerSettingEnum.OUT_HIDE );
                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });
        }
    }

    /**
     * ========================================================activity生命周期 start================================================================
     */
    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().post(PlayerListenerEnum.IN_PAUSE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(PlayerListenerEnum.IN_RESUME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ScreenRotateUtil.getInstance(this).stop();
        EventBus.getDefault().post(PlayerListenerEnum.IN_RELEASE);
        EventBus.getDefault().post(EventBusCode.ACTIVITY_FINISH);
    }

    /**
     * ========================================================activity生命周期 end================================================================
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (ObjectUtil.isNotNull(uri)) {
            final VideoijkBean bean = PlayerUtil.getVideoInfo(this, uri);
            if (StrUtil.isNotBlank(bean.getUrl()) && ObjectUtil.isNotNull(player)) {
                player.setTitle(bean.getTitle())
                        .setScaleType(PlayStateParams.fitparent)
                        .forbidTouch(false)
                        .setPlaySource(bean.getUrl())
                        .setResolution(bean.getHeight())
                        .setGravitySensor(true)
                        .startPlay();
            }

        }

    }

    /**
     * eventBus 消息监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlayerSettingEnum playerSettingEnum) {
        if (PlayerSettingEnum.IN_SHOW == playerSettingEnum) {
            drawerLayout.openDrawer(Gravity.END);
        } else if (PlayerSettingEnum.IN_HIDE == playerSettingEnum) {
            drawerLayout.closeDrawer(Gravity.END);
        }
    }

}
