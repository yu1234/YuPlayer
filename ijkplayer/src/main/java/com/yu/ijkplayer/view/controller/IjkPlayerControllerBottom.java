package com.yu.ijkplayer.view.controller;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkplayer.R;
import com.yu.ijkplayer.R2;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.bean.GestureListenerCode;
import com.yu.ijkplayer.bean.MediaQuality;
import com.yu.ijkplayer.bean.PlayMode;
import com.yu.ijkplayer.bean.PlayerControllerViewEnum;
import com.yu.ijkplayer.bean.PlayerListenerEnum;
import com.yu.ijkplayer.bean.ScreenLock;
import com.yu.ijkplayer.impl.PlayerCompletion;
import com.yu.ijkplayer.utils.PlayerUtil;
import com.yu.ijkplayer.view.playerView.IjkVideoView;
import com.yu.ijkplayer.view.playerView.PlayStateParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/7/7 0007.
 */

public class IjkPlayerControllerBottom extends LinearLayout implements View.OnClickListener {
    private static final String TAG = IjkPlayerControllerBottom.class.getSimpleName();
    /**
     * 记录进行后台时的播放状态0为播放，1为暂停
     */
    private int bgState;
    /**
     * 当前状态
     */
    private int status = PlayStateParams.STATE_IDLE;
    /**
     * 当前播放位置
     */
    private int currentPosition;
    /**
     * 播放器对象
     */
    private IjkVideoView player;
    /**
     * 依附的activity
     **/
    private Activity activity;
    /**
     * 进度条定时器
     */
    private Timer progressTimer;
    /**
     * 播放模式 默认为循环
     */
    private static final PlayMode PLAY_MODE = PlayMode.CYCLE;
    /**
     * 当前屏幕是否锁屏 默认为没有锁屏
     */
    private ScreenLock screenLock = ScreenLock.UNLOCK;
    /**
     * 控件注入
     */
    @BindView(R2.id.ijk_player_seekBar)
    SeekBar playerSeekBar;
    @BindView(R2.id.video_currentTime)
    TextView videoCurrentTime;
    @BindView(R2.id.video_endTime)
    TextView videoEndTime;
    @BindView(R2.id.video_play)
    ImageView videoPlay;
    @BindView(R2.id.player_play_c)
    ImageView playerPlayC;
    @BindView(R2.id.ijk_player_media_quality_txt)
    TextView ijkPlayerMediaQualityTxt;
    @BindView(R2.id.ijk_player_media_quality_icon)
    ImageView ijkPlayerMediaQualityIcon;
    @BindView(R2.id.ijk_player_lock_box)
    LinearLayout ijkPlayerLockBox;


    public IjkPlayerControllerBottom(Context context) {
        this(context, null);
    }

    public IjkPlayerControllerBottom(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkPlayerControllerBottom(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.activity = (Activity) context;
        //填充页面
        View view = LayoutInflater.from(context).inflate(R.layout.ijk_player_controller_bottom, this);
        ButterKnife.bind(this, view);
        //参数初始化
        initParams();
        //事件注册
        registerListener();
        //EventBus注册
        EventBus.getDefault().register(this);
    }

    /**
     * 参数初始化
     */
    private void initParams() {
        //进度条初始化
        if (ObjectUtil.isNotNull(this.playerSeekBar)) {
            this.playerSeekBar.setMax(1000);
        }
        if (ObjectUtil.isNotNull(this.playerPlayC)) {
            this.playerPlayC.setImageResource(R.drawable.ic_tv_stop);
        }
        //锁屏参数初始化
        screenLock = ScreenLock.UNLOCK;
    }

    /**
     * 事件注册
     */
    private void registerListener() {
        //播放\暂停按钮事件注册
        if (ObjectUtil.isNotNull(this.playerPlayC)) {
            this.playerPlayC.setOnClickListener(this);
        }
        if (ObjectUtil.isNotNull(this.videoPlay)) {
            this.videoPlay.setOnClickListener(this);
        }
        //进度条注册
        if (ObjectUtil.isNotNull(this.playerSeekBar)) {
            this.playerSeekBar.setOnSeekBarChangeListener(mSeekListener);
        }
        //锁屏点击事件注册
        if (ObjectUtil.isNotNull(this.ijkPlayerLockBox)) {
            this.ijkPlayerLockBox.setOnClickListener(this);
        }
    }

    /**
     * 点击事件回调
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.player_play_c) {
            if (ObjectUtil.isNotNull(this.player)) {
                if (this.player.isPlaying()) {
                    this.onPlayerPause();

                } else {
                    this.onPlayerResume();
                }
            }
        } else if (v.getId() == R.id.video_play) {
            if (ObjectUtil.isNotNull(this.player)) {
                if (this.player.isPlaying()) {
                    this.onPlayerPause();
                } else {
                    this.onPlayerResume();
                }
            }
        } else if (v.getId() == R.id.ijk_player_lock_box) {
            if (this.screenLock == ScreenLock.UNLOCK) {
                this.screenLock = ScreenLock.LOCK;
            } else if (this.screenLock == ScreenLock.LOCK) {
                this.screenLock = ScreenLock.UNLOCK;
            }
            EventBus.getDefault().post(this.screenLock);
        }
    }

    /**
     * 显示上层控制页面
     */
    private void showView() {
        if (ScreenLock.UNLOCK == this.screenLock) {
            this.setVisibility(VISIBLE);
        }

    }

    /**
     * 隐藏上层控制页面
     */
    private void hideView() {
        this.setVisibility(GONE);

    }


    /**
     * 同步进度
     */
    private void syncProgress(long position) {
        if (ObjectUtil.isNotNull(this.player)) {
            this.currentPosition = (int) position;
            long duration = this.player.getDuration();
            if (this.playerSeekBar != null) {
                if (duration > 0) {
                    long pos = 1000L * position / duration;
                    this.playerSeekBar.setProgress((int) pos);
                }
                int percent = this.player.getBufferPercentage();
                this.playerSeekBar.setSecondaryProgress(percent * 10);
            }
            if (ObjectUtil.isNotNull(this.videoCurrentTime)) {
                this.videoCurrentTime.setText(PlayerUtil.generateTime(position));
            }
            if (ObjectUtil.isNotNull(this.videoEndTime)) {
                this.videoEndTime.setText(PlayerUtil.generateTime(duration));
            }

        }
    }

    private void syncProgress() {
        if (ObjectUtil.isNotNull(this.player)) {
            this.syncProgress(this.player.getCurrentPosition());
        }
    }

    /**
     * 开启进度条定时任务
     */
    private void openProgressTimer() {
        if (ObjectUtil.isNotNull(this.progressTimer)) {
            this.progressTimer.cancel();
            this.progressTimer = null;
        }
        //设置定时任务
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (status == PlayStateParams.STATE_PLAYING) {
                    EventBus.getDefault().post(EventBusCode.PROGRESS_CHANGE);
                }
            }
        };
        //开启定时器
        this.progressTimer = new Timer();
        this.progressTimer.schedule(task, 0, 1000);
    }

    /**
     * 设置播放器
     *
     * @param player
     */
    public void setPlayer(IjkVideoView player) {
        this.player = player;
        openProgressTimer();
    }

    /**
     * 播放暂停
     * if (player != null) {
     * player.onPause();
     */
    public void onPlayerPause() {
        Log.i(TAG, "onPlayerPause");
        if (ObjectUtil.isNotNull(this.player)) {
            this.bgState = (this.player.isPlaying() ? 0 : 1);
            status = PlayStateParams.STATE_PAUSED;
            if (this.player.isPlaying()) {
                this.player.pause();
                this.currentPosition = this.player.getCurrentPosition();
                Log.i(TAG, "onPlayerPause：" + this.currentPosition);
                if (ObjectUtil.isNotNull(this.videoPlay)) {
                    this.videoPlay.setImageResource(R.drawable.bili_player_play_can_play);
                }
                if (ObjectUtil.isNotNull(this.playerPlayC)) {
                    this.playerPlayC.setImageResource(R.drawable.ic_tv_play);
                }
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
        Log.i(TAG, "onPlayerResume");

        if (ObjectUtil.isNotNull(this.player)) {
            Log.i(TAG, "onPlayerResume：" + this.currentPosition);
            if (this.currentPosition == this.player.getCurrentPosition()) {
                this.currentPosition = this.player.getCurrentPosition();
                this.player.start();
            } else {
                this.player.start(this.currentPosition);
            }

            if (ObjectUtil.isNotNull(this.playerPlayC)) {
                this.playerPlayC.setImageResource(R.drawable.ic_tv_stop);
            }
            if (ObjectUtil.isNotNull(this.videoPlay)) {
                this.videoPlay.setImageResource(R.drawable.bili_player_play_can_pause);
            }
            status = PlayStateParams.STATE_PLAYING;
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
        Log.i(TAG, "onPlayerRelease");
        if (ObjectUtil.isNotNull(this.player)) {
            this.player.stopPlayback();
            status = PlayStateParams.STATE_IDLE;
        }
    }

    /**
     * 停止视频
     *
     * @return
     */
    public void onPlayerStop() {
        Log.i(TAG, "onPlayerStop");
        if (ObjectUtil.isNotNull(this.player)) {
            this.onPlayerRelease();
        }
    }

    /**
     * 开始播放
     */
    public void onPlayerStart() {
        Log.i(TAG, "onPlayerStart");
        status = PlayStateParams.STATE_PLAYING;
        if (ObjectUtil.isNotNull(this.playerPlayC)) {
            this.playerPlayC.setImageResource(R.drawable.ic_tv_stop);
        }
        if (ObjectUtil.isNotNull(this.videoPlay)) {
            this.videoPlay.setImageResource(R.drawable.bili_player_play_can_pause);
        }
    }

    /**
     * 开始播放
     */
    public void onPlayerRestart() {
        EventBus.getDefault().post(PlayerListenerEnum.OUT_RESTART);
    }

    /**
     * 快进或者快退滑动改变进度
     *
     * @param percent
     */
    private void onProgressSlide(float percent) {
        if (ObjectUtil.isNotNull(this.player)) {
            int position = this.player.getCurrentPosition();
            long duration = this.player.getDuration();
            long deltaMax = Math.min(100 * 1000, duration - position);
            long delta = (long) (deltaMax * percent);
            long newPosition = delta + position;
            if (newPosition > duration) {
                newPosition = duration;
            } else if (newPosition <= 0) {
                newPosition = 0;
            }
            syncProgress(newPosition);
        }

    }

/**
 * ==========================================================消息监听函数====================================
 */
    /**
     * eventBus 消息监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusCode eventBusCode) {
       if (EventBusCode.ACTIVITY_FINISH == eventBusCode) {//activity退出
            EventBus.getDefault().unregister(this);
        } else if (EventBusCode.PROGRESS_CHANGE == eventBusCode) {
            syncProgress();
        }
    }

    /**
     * 播放结束监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlayerCompletion playerCompletion) {
        Log.i(TAG, "播放结束");
        status = PlayStateParams.STATE_COMPLETED;
        //释放进度条定时器
        if (ObjectUtil.isNotNull(this.progressTimer)) {
            this.progressTimer.cancel();
            this.progressTimer = null;
        }
        if (this.PLAY_MODE == PlayMode.CYCLE) {
            this.onPlayerRestart();
        } else if (this.PLAY_MODE == PlayMode.STOP) {
            if (ObjectUtil.isNotNull(this.activity)) {
                this.activity.finish();
            }

        }
    }

    /**
     * 播放过程监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlayerListenerEnum code) {
        if (PlayerListenerEnum.IN_START == code) {  //播放开始
            this.onPlayerStart();
        } else if (PlayerListenerEnum.IN_PAUSE == code) {//播放暂停
            this.onPlayerPause();
        } else if (PlayerListenerEnum.IN_RESUME == code) {//播放恢复
            this.onPlayerResume();
        } else if (PlayerListenerEnum.IN_STOP == code) {//播放停止
            this.onPlayerStop();
        } else if (PlayerListenerEnum.IN_RELEASE == code) {//播放释放
            this.onPlayerRelease();
        }
    }

    /**
     * 手势监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GestureListenerCode code) {
        if (ScreenLock.UNLOCK == this.screenLock) {//当解屏状态时才有执行权限
            if (GestureListenerCode.PROGRESS_SLIDE == code) {//进度条滑动监听
                Log.i(TAG, "进度条滑动百分比：" + code.getPercent());
                if (ObjectUtil.isNotNull(this.player) && this.player.isPlaying()) {
                    onPlayerPause();
                }
                onProgressSlide(code.getPercent());
                this.setVisibility(VISIBLE);
            } else if (GestureListenerCode.END_GESTURE == code) {//手势结束
                if (code.getEndCode() == GestureListenerCode.PROGRESS_SLIDE) {
                    Log.i(TAG, "进度条滑动手势结束");
                    onPlayerResume();
                    this.setVisibility(GONE);
                }
            }
        }
    }

    /**
     * 进度条滑动监听
     */
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        /**数值的改变*/
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (ObjectUtil.isNotNull(player) && fromUser) {
                long duration = player.getDuration();
                int position = (int) ((duration * progress * 1.0) / 1000);
                currentPosition = position;
                syncProgress(currentPosition);
            }
        }

        /**开始拖动*/
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (ObjectUtil.isNotNull(player)) {
                onPlayerPause();
            }
        }

        /**停止拖动*/
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (ObjectUtil.isNotNull(player)) {
                Log.i(TAG, "onStopTrackingTouch：" + currentPosition);
                onPlayerResume();
            }
        }
    };

    /**
     * eventBus 分辨率设置
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MediaQuality mediaQuality) {
        if (ObjectUtil.isNotNull(ijkPlayerMediaQualityIcon)) {
            ijkPlayerMediaQualityIcon.setImageResource(mediaQuality.getIconResource());
        }
        if (ObjectUtil.isNotNull(ijkPlayerMediaQualityTxt)) {
            ijkPlayerMediaQualityTxt.setText(mediaQuality.getName());
        }
    }

    /**
     * 锁屏消息监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScreenLock screenLock) {
        this.screenLock = screenLock;
    }
    /**
     * 控制界面显示/隐藏监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlayerControllerViewEnum controllerViewEnum) {
        if (PlayerControllerViewEnum.OUT_SHOW == controllerViewEnum) {//显示view
            this.showView();
        } else if (PlayerControllerViewEnum.OUT_HIDE == controllerViewEnum) {//隐藏view
            this.hideView();
        }
    }
}
