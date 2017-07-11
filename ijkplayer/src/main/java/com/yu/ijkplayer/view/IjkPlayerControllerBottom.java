package com.yu.ijkplayer.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkplayer.R;
import com.yu.ijkplayer.R2;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.bean.GestureListenerCode;
import com.yu.ijkplayer.bean.NetWorkStatus;
import com.yu.ijkplayer.bean.PlayMode;
import com.yu.ijkplayer.bean.PlayerListenerCode;
import com.yu.ijkplayer.impl.PlayerCompletion;
import com.yu.ijkplayer.utils.NetworkUtils;
import com.yu.ijkplayer.utils.PlayerUtil;
import com.yu.ijkplayer.utils.SrceenUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import tv.danmaku.ijk.media.player.IMediaPlayer;

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
     * 控件注入
     */
    @BindView(R2.id.ijk_player_seekBar)
    SeekBar playerSeekBar;
    @BindView(R2.id.video_currentTime)
    TextView videoCurrentTime;
    @BindView(R2.id.video_endTime)
    TextView videoEndTime;
    @BindView(R2.id.video_play)
    ImageView videpPlay;
    @BindView(R2.id.player_play_c)
    ImageView playerPlayC;


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

    }

    /**
     * 事件注册
     */
    private void registerListener() {
        //播放\暂停按钮事件注册
        if (ObjectUtil.isNotNull(this.playerPlayC)) {
            this.playerPlayC.setOnClickListener(this);
        }
        if (ObjectUtil.isNotNull(this.videpPlay)) {
            this.videpPlay.setOnClickListener(this);
        }
    }

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
        }
    }

    /**
     * 显示上层控制页面
     */
    private void showView() {
        int navBarHeight = SrceenUtils.getNavigationBarSize(this.activity).x;
        int realW = SrceenUtils.getRealScreenSize(this.activity).x;
        int w = realW - navBarHeight;
        if (w > 0) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
            params.width = w;
            this.setLayoutParams(params);
        }
        this.setVisibility(VISIBLE);
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
    private void syncProgress() {
        if (ObjectUtil.isNotNull(this.player)) {
            long position = this.player.getCurrentPosition();
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
                EventBus.getDefault().post(EventBusCode.PROGRESS_CHANGE);
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
        if (ObjectUtil.isNotNull(this.player)) {
            this.bgState = (this.player.isPlaying() ? 0 : 1);
            status = PlayStateParams.STATE_PAUSED;
            if (this.player.isPlaying()) {
                this.currentPosition = this.player.getCurrentPosition();
                this.player.pause();
                if (ObjectUtil.isNotNull(this.videpPlay)) {
                    this.videpPlay.setImageResource(R.drawable.bili_player_play_can_play);
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
        Log.i(TAG, "播放进度:" + currentPosition);
        status = PlayStateParams.STATE_PLAYING;
        if (ObjectUtil.isNotNull(this.player)) {
            this.player.start();
            this.player.seekTo(currentPosition);
            if (ObjectUtil.isNotNull(this.playerPlayC)) {
                this.playerPlayC.setImageResource(R.drawable.ic_tv_stop);
            }
            if (ObjectUtil.isNotNull(this.videpPlay)) {
                this.videpPlay.setImageResource(R.drawable.bili_player_play_can_pause);
            }
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
        if (ObjectUtil.isNotNull(this.player)) {
            this.onPlayerRelease();
        }
    }

    /**
     * 开始播放
     */
    public void onPlayerStart() {
        status = PlayStateParams.STATE_PLAYING;
        if (ObjectUtil.isNotNull(this.playerPlayC)) {
            this.playerPlayC.setImageResource(R.drawable.ic_tv_stop);
        }
        if (ObjectUtil.isNotNull(this.videpPlay)) {
            this.videpPlay.setImageResource(R.drawable.bili_player_play_can_pause);
        }
    }

    /**
     * 开始播放
     */
    public void onPlayerRestart() {
        EventBus.getDefault().post(PlayerListenerCode.RESTART);
    }

/**
 * ==========================================================消息监听函数====================================
 */
    /**
     * eventBus 消息监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusCode eventBusCode) {
        if (EventBusCode.SHOW_VIEW == eventBusCode) {//显示view
            if (ObjectUtil.isNotNull(this.activity)) {
                this.showView();
            }

        } else if (EventBusCode.HIDE_VIEW == eventBusCode) {//隐藏view
            if (ObjectUtil.isNotNull(this.activity)) {
                this.hideView();
            }

        } else if (EventBusCode.ACTIVITY_FINISH == eventBusCode) {//activity退出
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
    public void onMessageEvent(PlayerListenerCode code) {
        if (PlayerListenerCode.START == code) {  //播放开始
            this.onPlayerStart();
        } else if (PlayerListenerCode.PAUSE == code) {//播放暂停
            this.onPlayerPause();
        } else if (PlayerListenerCode.RESUME == code) {//播放恢复
            this.onPlayerResume();
        } else if (PlayerListenerCode.STOP == code) {//播放停止
            this.onPlayerStop();
        } else if (PlayerListenerCode.RELEASE == code) {//播放释放
            this.onPlayerRelease();
        }
    }
    /**
     * 手势监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GestureListenerCode code) {
       if(GestureListenerCode.PROGRESS_SLIDE==code){//进度条滑动监听

       }else if(GestureListenerCode.VOLUME_SLIDE==code){//声音滑动监听

       }else if(GestureListenerCode.BRIGHTNESS_SLIDE==code){//亮度滑动监听

       }else if(GestureListenerCode.END_GESTURE==code){//手势结束

       }
    }
}
