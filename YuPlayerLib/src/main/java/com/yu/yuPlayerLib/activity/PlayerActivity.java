package com.yu.yuPlayerLib.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.yuPlayerLib.R;
import com.yu.yuPlayerLib.R2;
import com.yu.yuPlayerLib.media.ui.VideoPlayerView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.targetSdkVersion;


public class PlayerActivity extends AppCompatActivity {
    private ImmersionBar immersionBar;
    @BindView(R2.id.toolbar)
    Toolbar toolbar;
    @BindView(R2.id.orientation_overlay_button)
    LinearLayout orientationOverlayButton;
    @BindView(R2.id.videoPlayerView)
    VideoPlayerView videoPlayerView;

    private boolean barHideFlag = true;
    //显示状态倒计时
    private CountDownTimer barShowCountDownTimer;

    private SimpleExoPlayer simpleExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_player_controller_top);
        ButterKnife.bind(this);
        immersionBar = ImmersionBar.with(this).titleBar(toolbar).transparentBar().hideBar(BarHide.FLAG_HIDE_BAR);
        immersionBar.init();
        setSupportActionBar(toolbar);
        hideBar();

        videoPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barHideFlag) {
                    showBar();
                } else {
                    hideBar();
                }

            }
        });
        requestPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tool_bar, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ObjectUtil.isNotNull(immersionBar)) {
            immersionBar.destroy();
        }

    }

    private void hideBar() {
        immersionBar.titleBar(toolbar).transparentBar().hideBar(BarHide.FLAG_HIDE_BAR).init();
        getSupportActionBar().hide();
        orientationOverlayButton.setVisibility(View.GONE);
        barHideFlag = true;
        if (ObjectUtil.isNotNull(barShowCountDownTimer)) {
            barShowCountDownTimer.cancel();
        }
        barShowCountDownTimer = null;
    }

    private void showBar() {
        immersionBar.titleBar(toolbar).transparentBar().hideBar(BarHide.FLAG_SHOW_BAR).init();
        getSupportActionBar().show();
        orientationOverlayButton.setVisibility(View.VISIBLE);
        barHideFlag = false;
        if (ObjectUtil.isNotNull(barShowCountDownTimer)) {
            barShowCountDownTimer.cancel();

        }
        barShowCountDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                hideBar();
            }
        };
        barShowCountDownTimer.start();
    }

    /**
     * 初始化player
     */
    private void initPlayer() {
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTackSelectionFactory);
        //2.创建ExoPlayer
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        //3.为SimpleExoPlayer设置播放器
        videoPlayerView.setPlayer(simpleExoPlayer);
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "test.mp4";
        //videoPlayerView.playVideo(new File(path));

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            requestPermissions();

        }
    }
    private void requestPermissions() {
        if (this.selfPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            initPlayer();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
    public boolean selfPermissionGranted(String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = this.checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(this, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }
}
