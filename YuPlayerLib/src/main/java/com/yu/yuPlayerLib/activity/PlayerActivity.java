package com.yu.yuPlayerLib.activity;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;
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
    private final static String TAG = PlayerActivity.class.getSimpleName();
    private ImmersionBar immersionBar;
    @BindView(R2.id.toolbar_back)
    ImageView toolbarBack;
     @BindView(R2.id.orientation_overlay_button)
     LinearLayout orientationOverlayButton;
    @BindView(R2.id.videoPlayerView)
    VideoPlayerView videoPlayerView;

    private SimpleExoPlayer simpleExoPlayer;
    private int resumeWindow;
    private long resumePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearResumePosition();
        setContentView(R.layout.player_main);
        ButterKnife.bind(this);
        ImmersionBar.with(this).transparentBar().fullScreen(true).hideBar(BarHide.FLAG_HIDE_BAR).init();
        toolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerActivity.this.finish();
            }
        });
        orientationOverlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Configuration cf= PlayerActivity.this.getResources().getConfiguration(); //获取设置的配置信息
                int ori = cf.orientation ; //获取屏幕方向
                if(ori == cf.ORIENTATION_LANDSCAPE){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                }else if(ori == cf.ORIENTATION_PORTRAIT){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || simpleExoPlayer == null)) {
            initPlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ObjectUtil.isNotNull(immersionBar)) {
            immersionBar.destroy();
            releasePlayer();
        }

    }
    private void releasePlayer() {
        if (simpleExoPlayer != null) {
           simpleExoPlayer.getPlayWhenReady();
            updateResumePosition();
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
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

        //4.为SimpleExoPlayer设置播放器
        videoPlayerView.setPlayer(simpleExoPlayer);
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "test.mp4";
        videoPlayerView.playVideo(new File(path));
        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            simpleExoPlayer.seekTo(resumeWindow, resumePosition);
        }

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

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(TAG, ev.getActionMasked() + "=onTouchEvent");
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "onTouchEvent=" + ev.getActionMasked());
        }
        return true;
    }

    private void updateResumePosition() {
        resumeWindow = simpleExoPlayer.getCurrentWindowIndex();
        resumePosition = simpleExoPlayer.isCurrentWindowSeekable() ? Math.max(0, simpleExoPlayer.getCurrentPosition())
                : C.TIME_UNSET;
    }
    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }
}
