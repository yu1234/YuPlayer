package com.yu.ijkplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.bean.PlayerListenerCode;
import com.yu.ijkplayer.bean.VideoijkBean;
import com.yu.ijkplayer.listener.OnPlayerBackListener;
import com.yu.ijkplayer.listener.OnShowThumbnailListener;
import com.yu.ijkplayer.utils.PlayerUtil;
import com.yu.ijkplayer.utils.ScreenRotateUtil;
import com.yu.ijkplayer.view.IjkPlayerView;
import com.yu.ijkplayer.view.IjkVideoView;
import com.yu.ijkplayer.view.PlayStateParams;
import com.yu.ijkplayer.view.PlayerView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.targetSdkVersion;

/**
 * Created by igreentree on 2017/6/28 0028.
 */

public class MainActivity extends AppCompatActivity {
    @BindView(R2.id.ijk_player_view)
    IjkPlayerView player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ijk_player);
        ButterKnife.bind(this);
        ScreenRotateUtil.getInstance(this).start(this);
        initPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().post(PlayerListenerCode.PAUSE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().post(PlayerListenerCode.RESUME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ScreenRotateUtil.getInstance(this).stop();
        EventBus.getDefault().post(PlayerListenerCode.RELEASE);
        EventBus.getDefault().post(EventBusCode.ACTIVITY_FINISH);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

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
}
