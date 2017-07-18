package com.yu.ijkPlayer.view.controller;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.xiaoleilu.hutool.util.PageUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yu.ijkPlayer.R;
import com.yu.ijkPlayer.R2;
import com.yu.ijkPlayer.bean.VideoIjkBean;
import com.yu.ijkPlayer.bean.daoBean.PlaySetting;
import com.yu.ijkPlayer.bean.enumBean.GestureListenerCode;
import com.yu.ijkPlayer.bean.enumBean.MediaQuality;
import com.yu.ijkPlayer.bean.enumBean.PlayMode;
import com.yu.ijkPlayer.bean.enumBean.PlayerControllerViewEnum;
import com.yu.ijkPlayer.bean.enumBean.PlayerListenerEnum;
import com.yu.ijkPlayer.bean.enumBean.ScreenLock;
import com.yu.ijkPlayer.impl.PlayerCompletion;
import com.yu.ijkPlayer.utils.PlayerUtil;
import com.yu.ijkPlayer.utils.ScreenRotateUtil;
import com.yu.ijkPlayer.view.playerView.IjkVideoView;
import com.yu.ijkPlayer.view.playerView.PlayStateParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

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
     * 播放列表
     */
    private List<Uri> playList = new ArrayList<>();
    /**
     * 当前播放的索引值
     */
    private int currentPlayIndex = 0;
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
    /**
     * 当前手势类型
     */
    private static Uri currentPlayUri = null;

    /**
     * 当前状态
     */
    private int status = PlayStateParams.STATE_IDLE;

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
            currentPlayUri = uri;
            if (CollectionUtil.isNotEmpty(this.playList)) {
                int index = this.playList.indexOf(currentPlayUri);
                if (index >= 0) {
                    this.currentPlayIndex = index;
                } else {
                    this.currentPlayIndex = 0;
                    this.playList.add(0, currentPlayUri);
                }
            } else {
                this.currentPlayIndex = 0;
                this.playList = new ArrayList<Uri>();
                this.playList.add(0, currentPlayUri);
            }
            ijkVideoView.setVideoURI(currentPlayUri);
        }
        return this;
    }

    /**
     * 设置播放地址
     */
    public IjkPlayerView setPlaySource(String path) {
        if (StrUtil.isNotBlank(path)) {
            this.setPlaySource(Uri.parse(path));
        }
        return this;
    }

    /**
     * 设置播放列表
     */
    public IjkPlayerView setPlayList(List<Uri> playList) {
        if (CollectionUtil.isNotEmpty(playList)) {
            this.playList = playList;
        }
        return this;
    }

    /**
     * 设置播放器设置
     */
    public IjkPlayerView setPlayerSetting(PlaySetting playerSetting) {
        if (ObjectUtil.isNotNull(playerSetting)) {
            EventBus.getDefault().post(playerSetting);
        }
        return this;
    }

    /**
     * 开始播放
     */
    public IjkPlayerView startPlay() {
        if (playerSupport && ObjectUtil.isNotNull(ijkVideoView)) {
            //换源之后声音可播，画面卡住，主要是渲染问题，目前只是提供了软解方式，后期提供设置方式
            ijkVideoView.setRender(ijkVideoView.RENDER_TEXTURE_VIEW);
            ijkVideoView.setOnCompletionListener(this);
            ijkVideoView.start(0);
            controllerBottom.setPlayer(ijkVideoView);
            controllerCenter.setPlayer(ijkVideoView);
            EventBus.getDefault().post(PlayerListenerEnum.IN_START);
        } else {
            //TODO 播放错误
            //showStatus(mActivity.getResources().getString(R.string.not_support));
        }
        return this;
    }

    /**
     * 循环播放
     */
    public void cyclePlay() {
        if (CollectionUtil.isNotEmpty(this.playList) && this.playList.size() > 1) {
            if (this.currentPlayIndex == (this.playList.size() - 1)) {
                Uri uri = this.playList.get(0);
                if (ObjectUtil.isNotNull(uri)) {
                    VideoIjkBean bean = PlayerUtil.getVideoInfo(this.context, uri);
                    this.setTitle(bean.getTitle()).setPlaySource(uri).startPlay();
                }
            } else {
                Uri uri = this.playList.get(this.currentPlayIndex + 1);
                if (ObjectUtil.isNotNull(uri)) {
                    VideoIjkBean bean = PlayerUtil.getVideoInfo(this.context, uri);
                    this.setTitle(bean.getTitle()).setPlaySource(uri)
                            .startPlay();
                }
            }
        } else {
            startPlay();
        }

    }

    /**
     * 播放上一首
     */
    public void playPrevious() {
        if (CollectionUtil.isNotEmpty(this.playList) && this.playList.size() > 1) {
            if(this.currentPlayIndex>0){
                Uri uri = this.playList.get(this.currentPlayIndex - 1);
                if (ObjectUtil.isNotNull(uri)) {
                    VideoIjkBean bean = PlayerUtil.getVideoInfo(this.context, uri);
                    this.setTitle(bean.getTitle()).setPlaySource(uri)
                            .startPlay();
                }
            }else {
                Uri uri = this.playList.get(this.playList.size()- 1);
                if (ObjectUtil.isNotNull(uri)) {
                    VideoIjkBean bean = PlayerUtil.getVideoInfo(this.context, uri);
                    this.setTitle(bean.getTitle()).setPlaySource(uri)
                            .startPlay();
                }
            }
        } else {
            startPlay();
        }

    }


    /**
     * 随机播放
     */
    public void randomPlay() {
        if (CollectionUtil.isNotEmpty(this.playList) && this.playList.size() > 1) {
            int random = PlayerUtil.getRandom(this.playList.size());
            if (this.currentPlayIndex != random) {
                Uri uri = this.playList.get(random);
                if (ObjectUtil.isNotNull(uri)) {
                    VideoIjkBean bean = PlayerUtil.getVideoInfo(this.context, uri);
                    this.setTitle(bean.getTitle()).setPlaySource(uri).startPlay();
                }
            } else {
                randomPlay();
            }
        } else {
            startPlay();
        }
    }

    /**
     * 设置分辨率
     */
    public IjkPlayerView setResolution(int height) {
        MediaQuality mediaQuality = MediaQuality.getMediaQuality(height);
        EventBus.getDefault().post(mediaQuality);
        return this;
    }

    /**
     * 设置重力感应
     */
    public IjkPlayerView setGravitySensor(boolean isSupportGravitySensor) {
        if (isSupportGravitySensor) {
            ScreenRotateUtil.getInstance(this.activity).toggleRotate();
        }
        return this;
    }


    /**
     * 显示控制页面
     */
    private void showControllerView() {
        this.isControllerViewShow = true;
        EventBus.getDefault().post(PlayerControllerViewEnum.OUT_SHOW);
        hideAfterTimeout();
    }

    /**
     * 隐藏控制页面
     */
    private void hideControllerView() {
        this.isControllerViewShow = false;
        EventBus.getDefault().post(PlayerControllerViewEnum.OUT_HIDE);
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
                if (ObjectUtil.isNotNull(ijkVideoView) && ijkVideoView.isPlaying()) {
                    hideControllerView();
                } else {
                    hideAfterTimeout();
                }
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
                    EventBus.getDefault().post(PlayerListenerEnum.IN_PAUSE);
                } else {
                    EventBus.getDefault().post(PlayerListenerEnum.IN_RESUME);
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
                    isVolume = mOldX > screenWidthPixels * 0.5f;
                    isDownTouch = false;
                }
                if (isLandscape) {
                    /**进度设置*/
                    gestureListenerCode = GestureListenerCode.PROGRESS_SLIDE;
                    gestureListenerCode.setPercent(-deltaX / ijkVideoView.getWidth());
                    EventBus.getDefault().post(gestureListenerCode);
                } else {
                    int h = ijkVideoView.getHeight();
                    float percent = deltaY / h;
                    if (isVolume) {
                        /**声音设置*/
                        gestureListenerCode = GestureListenerCode.VOLUME_SLIDE;
                        gestureListenerCode.setPercent(percent);
                        EventBus.getDefault().post(gestureListenerCode);
                    } else {
                        /**亮度设置*/
                        gestureListenerCode = GestureListenerCode.BRIGHTNESS_SLIDE;
                        gestureListenerCode.setPercent(percent);
                        EventBus.getDefault().post(gestureListenerCode);
                    }
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
/**
 * ==========================================================消息监听函数====================================
 */
    /**
     * 播放过程监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlayerListenerEnum code) {
        if (PlayerListenerEnum.OUT_RESTART == code) {//重新播放
            this.startPlay();
        }
    }

    /**
     * 锁屏消息监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScreenLock screenLock) {
        if (ScreenLock.UNLOCK == screenLock) {
            showControllerView();
        } else if (ScreenLock.LOCK == screenLock) {
            hideControllerView();
        }
    }

    /**
     * 控制界面显示/隐藏监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlayerControllerViewEnum controllerViewEnum) {
        if (PlayerControllerViewEnum.IN_SHOW == controllerViewEnum) {
            showControllerView();
        } else if (PlayerControllerViewEnum.IN_HIDE == controllerViewEnum) {
            hideControllerView();
        }
    }

    /**
     * 监听播放播放切换方式
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlayMode mode) {
        if (PlayMode.ALL_CYCLE == mode) {//全部循环
            if (mode.isPrevious()) {//播放上一首
                playPrevious();
            } else {
                this.cyclePlay();//播放下一首
            }
        } else if (PlayMode.ONE_CYCLE == mode) {//单一循环
            this.startPlay();
        } else if (PlayMode.STOP == mode) {//停止播放
            if (ObjectUtil.isNotNull(this.activity)) {
                this.activity.finish();
            }
        } else if (PlayMode.RANDOM == mode) {//随机播放
            randomPlay();
        }
    }

    /**
     * 播放器设置监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlaySetting playSetting) {
        PlaySetting.dao.updateOrAdd(playSetting);
    }
}
