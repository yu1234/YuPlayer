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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yu.ijkplayer.R;
import com.yu.ijkplayer.R2;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.bean.GestureListenerCode;
import com.yu.ijkplayer.bean.PlayerListenerCode;
import com.yu.ijkplayer.bean.VideoijkBean;
import com.yu.ijkplayer.impl.PlayerCompletion;
import com.yu.ijkplayer.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by igreentree on 2017/7/7 0007.
 * <p>
 * 作 者：余流庆
 * <p>
 */

public class IjkPlayerView extends FrameLayout implements IMediaPlayer.OnCompletionListener, View.OnTouchListener {
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
    /**
     * 手势
     */
    private GestureDetector gestureDetector;

    /**
     * 当前手势类型
     */
    private GestureListenerCode gestureListenerCode;

    public IjkPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public IjkPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkPlayerView(@NonNull Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
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
        //EventBus注册
        EventBus.getDefault().register(this);
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
        this.setOnTouchListener(this);
        gestureDetector = new GestureDetector(this.context, new PlayerGestureListener());

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
        if (playerSupport && ObjectUtil.isNotNull(ijkVideoView)) {
            ijkVideoView.setOnCompletionListener(this);
            ijkVideoView.start();
            controllerBottom.setPlayer(ijkVideoView);
            EventBus.getDefault().post(PlayerListenerCode.START);
        } else {
            //TODO 播放错误
            //showStatus(mActivity.getResources().getString(R.string.not_support));
        }
        return this;
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

            }

            @Override
            public void onFinish() {
                hideControllerView();
            }
        };
        this.countDownTimer.start();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        PlayerCompletion playerCompletion = new PlayerCompletion();
        playerCompletion.setMediaPlayer(iMediaPlayer);
        EventBus.getDefault().post(playerCompletion);
    }


    /**
     * 播放过程监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlayerListenerCode code) {
        if (PlayerListenerCode.RESTART == code) {//重新播放
            this.startPlay();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (ObjectUtil.isNotNull(this.gestureDetector)) {
            this.gestureDetector.onTouchEvent(event);
        }
        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                //TODO 手势结束
                if (ObjectUtil.isNotNull(gestureListenerCode)) {
                    GestureListenerCode code = GestureListenerCode.END_GESTURE;
                    code.setEndCode(gestureListenerCode);
                    EventBus.getDefault().post(code);
                    gestureListenerCode = null;
                }
                break;
        }
        return true;
    }

    /**
     * 播放器的手势监听
     */
    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * 是否是按下的标识，默认为其他动作，true为按下标识，false为其他动作
         */
        private boolean isDownTouch;
        /**
         * 是否声音控制,默认为亮度控制，true为声音控制，false为亮度控制
         */
        private boolean isVolume;
        /**
         * 是否横向滑动，默认为纵向滑动，true为横向滑动，false为纵向滑动
         */
        private boolean isLandscape;

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            /**双击暂停\开始事件*/
            if (ObjectUtil.isNotNull(ijkVideoView)) {
                if (ijkVideoView.isPlaying()) {
                    EventBus.getDefault().post(PlayerListenerCode.PAUSE);
                } else {
                    EventBus.getDefault().post(PlayerListenerCode.RESUME);
                }
            }
            return true;
        }

        /**
         * 按下
         */
        @Override
        public boolean onDown(MotionEvent e) {
            isDownTouch = true;
            return super.onDown(e);
        }


        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isForbidTouch) {
                float mOldX = e1.getX(), mOldY = e1.getY();
                float deltaY = mOldY - e2.getY();
                float deltaX = mOldX - e2.getX();
                if (isDownTouch) {
                    isLandscape = Math.abs(distanceX) >= Math.abs(distanceY);
                }

                if (isLandscape) {
                    /**进度设置*/
                    gestureListenerCode = GestureListenerCode.PROGRESS_SLIDE;
                    gestureListenerCode.setPercent(-deltaX / ijkVideoView.getWidth());
                    EventBus.getDefault().post(gestureListenerCode);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        /**
         * 单击
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            /**视频视窗单击事件*/
            if (isControllerViewShow) {
                hideControllerView();
            } else {
                showControllerView();
            }
            return true;
        }
    }
}
