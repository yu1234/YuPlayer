package com.yu.ijkplayer.view;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yu.ijkplayer.R;
import com.yu.ijkplayer.R2;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.bean.VideoijkBean;
import com.yu.ijkplayer.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by igreentree on 2017/7/7 0007.
 * <p>
 * 作 者：余流庆
 * <p>
 */

public class IjkPlayerView extends FrameLayout implements View.OnClickListener {
    /**
     * 控件注入
     */
    @BindView(R2.id.ijk_video_view)
    IjkVideoView ijkVideoView;
    @BindView(R2.id.ijk_player_controller_top)
    IjkPlayerControllerTop controllerTop;
    @BindView(R2.id.ijk_player_controller_center)
    IjkPlayerControllerCenter controllerCenter;
    @BindView(R2.id.ijk_player_controller_bottom)
    IjkPlayerControllerBottom controllerBottom;
    /**
     * 打印日志的TAG
     */
    private static final String TAG = IjkPlayerView.class.getSimpleName();
    /**
     * 全局context
     */
    private Context context;

    /**
     * 依附的容器Activity
     */
    private Activity activity;
    /**
     * 第三方so是否支持，默认不支持，true为支持
     */
    private boolean playerSupport;
    /**
     * 获取当前设备的宽度
     */
    private int screenWidthPixels;
    /**
     * 获取当前设备的高度
     */
    private int screenHeightPixels;
    /**
     * 视频显示比例,全屏
     */
    private int currentShowType = PlayStateParams.fitxy;
    /**
     * 禁止触摸，默认可以触摸，true为禁止false为可触摸
     */
    private boolean isForbidTouch;
    /**
     * 当前播放位置
     */
    private int currentPosition;
    /**
     * 记录进行后台时的播放状态0为播放，1为暂停
     */
    private int bgState;
    /**
     * 当前状态
     */
    private int status = PlayStateParams.STATE_IDLE;
    /**
     * 控制页面显示 默认不显示 true为显示，false不显示
     */
    private boolean isControllerViewShow = false;
    /**
     * 页面显示倒计时
     */
    private CountDownTimer countDownTimer;
    /**
     * 页面显示时间
     */
    private final static long SHOW_VIEW_TIMEOUT = 5000;

    public IjkPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public IjkPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkPlayerView(@NonNull  Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //填充页面
        View view = LayoutInflater.from(context).inflate(R.layout.ijk_player_view, this);
        ButterKnife.bind(this, view);
        //初始化参数
        initParams(context, attrs, defStyleAttr);
        //播放器初始化
        initPlayer();
        //事件注册
        registerListener();
        view.post(new Runnable() {
            @Override
            public void run() {
                showControllerView();
            }
        });



    }

    /**
     * 初始化参数
     */
    private void initParams(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this.context = context;
        this.activity = (Activity) context;
        this.screenWidthPixels = this.context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeightPixels = this.context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 播放器初始化
     */
    private void initPlayer() {
        //加载播放库
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            playerSupport = true;
        } catch (Throwable e) {
            Log.e(TAG, "loadLibraries error", e);
        }
    }

    /**
     * 事件注册
     */
    private void registerListener() {
        //返回按键点击事件注册
        this.setOnClickListener(this);

    }

    /**
     * 设置视频名称
     */
    public IjkPlayerView setTitle(String title) {
        if (ObjectUtil.isNotNull(controllerTop)) {
            this.controllerTop.setTitle(title);
        }
        return this;
    }

    /**
     * 设置播放区域拉伸类型
     */
    public IjkPlayerView setScaleType(int showType) {
        currentShowType = showType;
        ijkVideoView.setAspectRatio(currentShowType);
        return this;
    }

    /**
     * 是否禁止触摸
     */
    public IjkPlayerView forbidTouch(boolean forbidTouch) {
        this.isForbidTouch = forbidTouch;
        return this;
    }

    /**
     * 设置播放地址
     */
    public IjkPlayerView setPlaySource(Uri uri) {
        if (ObjectUtil.isNotNull(uri)) {
            ijkVideoView.setVideoURI(uri);
        }
        return this;
    }

    /**
     * 设置播放地址
     */
    public IjkPlayerView setPlaySource(String path) {
        if (StrUtil.isNotBlank(path)) {
            ijkVideoView.setVideoPath(path);
        }
        return this;
    }

    /**
     * 开始播放
     */
    public IjkPlayerView startPlay() {
        if (playerSupport) {
            ijkVideoView.start();
        } else {
            //TODO 播放错误
            //showStatus(mActivity.getResources().getString(R.string.not_support));
        }
        return this;
    }

    /**
     * 播放暂停
     * if (player != null) {
     * player.onPause();
     */
    public void onPlayerPause() {
        if (ObjectUtil.isNotNull(ijkVideoView)) {
            bgState = (ijkVideoView.isPlaying() ? 0 : 1);
            status = PlayStateParams.STATE_PAUSED;
            if (ijkVideoView.isPlaying()) {
                getCurrentPosition();
                ijkVideoView.onPause();
            }

        }
    }

    /**
     * 播放恢复
     * if (player != null) {
     * player.onResume();
     * }
     * }
     */
    public void onPlayerResume() {
        if (ObjectUtil.isNotNull(ijkVideoView)) {
            ijkVideoView.onResume();
            ijkVideoView.seekTo(currentPosition);
            if (bgState == 1) {
                onPlayerPause();
            }
        }
    }

    /**
     * 播放器释放
     *
     * @return
     */
    public void onPlayerRelease() {
        if (ObjectUtil.isNotNull(ijkVideoView)) {
            ijkVideoView.stopPlayback();
        }
    }

    /**
     * 获取当前播放位置
     */
    public int getCurrentPosition() {
        if (ObjectUtil.isNotNull(ijkVideoView)) {
            this.currentPosition = ijkVideoView.getCurrentPosition();
        } else {
            this.currentPosition = 0;
        }
        return currentPosition;
    }

    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == this.getId()) {
            if (this.isControllerViewShow) {
                hideControllerView();
            } else {
                showControllerView();
            }
        }
    }

    /**
     * 显示控制页面
     */
    private void showControllerView() {
        this.isControllerViewShow = true;
        EventBus.getDefault().post(EventBusCode.SHOW_VIEW);
        hideAfterTimeout();
    }

    /**
     * 隐藏控制页面
     */
    private void hideControllerView() {
        this.isControllerViewShow = false;
        EventBus.getDefault().post(EventBusCode.HIDE_VIEW);
    }

    /**
     * 显示时间超时隐藏
     */
    private void hideAfterTimeout() {
        if (ObjectUtil.isNotNull(this.countDownTimer)) {
            this.countDownTimer.cancel();
            this.countDownTimer = null;
        }
        this.countDownTimer = new CountDownTimer(SHOW_VIEW_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "onTick");
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "onFinish");
                hideControllerView();
            }
        };
        this.countDownTimer.start();
    }

}
