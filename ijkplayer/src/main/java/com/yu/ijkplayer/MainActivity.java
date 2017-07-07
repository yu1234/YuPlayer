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
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yu.ijkplayer.bean.VideoijkBean;
import com.yu.ijkplayer.listener.OnPlayerBackListener;
import com.yu.ijkplayer.listener.OnShowThumbnailListener;
import com.yu.ijkplayer.utils.PlayerUtil;
import com.yu.ijkplayer.view.PlayStateParams;
import com.yu.ijkplayer.view.PlayerView;

import static android.R.attr.targetSdkVersion;

/**
 * Created by igreentree on 2017/6/28 0028.
 */

public class MainActivity extends AppCompatActivity {
    private PlayerView player;
    private Context mContext;
    private View rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        rootView = getLayoutInflater().from(this).inflate(R.layout.simple_player_view_player, null);
        setContentView(rootView);
        initPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (player != null) {
            player.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (player != null && player.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    private void initPlayer() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (ObjectUtil.isNotNull(uri)) {
            final VideoijkBean bean = PlayerUtil.getVideoInfo(this, uri);
            if(StrUtil.isNotBlank(bean.getUrl())){
                player = new PlayerView(this, rootView)
                        .setTitle(bean.getTitle())
                        .setScaleType(PlayStateParams.fitparent)
                        .forbidTouch(false)
                        .hideMenu(true)
                        .showThumbnail(new OnShowThumbnailListener() {
                            @Override
                            public void onShowThumbnail(ImageView ivThumbnail) {
                                ivThumbnail.setImageBitmap(bean.getThumbnails());
                            }
                        })
                        .setPlaySource(bean.getUrl())
                        .setPlayerBackListener(new OnPlayerBackListener() {
                            @Override
                            public void onPlayerBack() {
                                //这里可以简单播放器点击返回键
                                finish();
                            }
                        })
                        .startPlay();
            }

        }

    }
}
