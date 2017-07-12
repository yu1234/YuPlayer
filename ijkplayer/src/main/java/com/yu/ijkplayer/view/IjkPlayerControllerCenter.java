package com.yu.ijkplayer.view;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkplayer.R;
import com.yu.ijkplayer.R2;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.bean.GestureListenerCode;
import com.yu.ijkplayer.bean.ScreenLock;
import com.yu.ijkplayer.utils.PlayerUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/7/7 0007.
 */

public class IjkPlayerControllerCenter extends LinearLayout implements View.OnClickListener {
    private static final String TAG = IjkPlayerControllerCenter.class.getSimpleName();
    /**
     * 依附的activity
     **/
    private Activity activity;
    /**
     * 播放器对象
     */
    private IjkVideoView player;
    /**
     * 当前声音大小
     */
    private int volume;
    /**
     * 设备最大音量
     */
    private int mMaxVolume;
    /**
     * 音频管理器
     */
    private AudioManager audioManager;
    /**
     * 当前亮度大小
     */
    private float brightness;
    /**
     * 当前屏幕是否锁屏 默认为没有锁屏
     */
    private ScreenLock screenLock = ScreenLock.UNLOCK;
    /**
     * 控件注入
     */
    @BindView(R2.id.scroll_process_text_view)
    LinearLayout scrollProcessTextView;
    @BindView(R2.id.current_process_text)
    TextView currentProcessText;
    @BindView(R2.id.change_process_text)
    TextView changeProcessText;
    //声音
    @BindView(R2.id.ijk_player_volume_box)
    LinearLayout ijkPlayerVolumeBox;
    @BindView(R2.id.ijk_player_volume_icon)
    ImageView ijkPlayerVolumeIcon;
    @BindView(R2.id.ijk_player_volume_txt)
    TextView ijkPlayerVolumeTxt;
    //亮度
    @BindView(R2.id.ijk_player_brightness_box)
    LinearLayout ijkPlayerBrightnessBox;
    @BindView(R2.id.ijk_player_brightness_icon)
    ImageView ijkPlayerBrightnessIcon;
    @BindView(R2.id.ijk_player_brightness_txt)
    TextView ijkPlayerBrightnessTxt;
    //锁屏图标
    @BindView(R2.id.ijk_player_lock_icon)
    ImageView ijkPlayerLockIcon;


    public IjkPlayerControllerCenter(Context context) {
        this(context, null);
    }

    public IjkPlayerControllerCenter(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkPlayerControllerCenter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.activity = (Activity) context;
        //填充页面
        View view = LayoutInflater.from(context).inflate(R.layout.ijk_player_controller_center, this);
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
        //声音初始化
        volume = -1;
        if (ObjectUtil.isNotNull(this.activity)) {
            audioManager = (AudioManager) this.activity.getSystemService(Context.AUDIO_SERVICE);
            mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        //亮度初始化
        brightness = -1f;


    }

    /**
     * 事件注册
     */
    private void registerListener() {
        //锁屏点击事件注册
        if (ObjectUtil.isNotNull(this.ijkPlayerLockIcon)) {
            this.ijkPlayerLockIcon.setOnClickListener(this);
        }
    }

    /**
     * 点击事件回调
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ijk_player_lock_icon) {
            if (this.screenLock == ScreenLock.UNLOCK) {
                this.screenLock = ScreenLock.LOCK;

            } else if (this.screenLock == ScreenLock.LOCK) {
                this.screenLock = ScreenLock.UNLOCK;
            }
            EventBus.getDefault().post(this.screenLock);
        }
    }

    /**
     * 设置播放器
     *
     * @param player
     */
    public void setPlayer(IjkVideoView player) {
        this.player = player;
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
            if (ObjectUtil.isNotNull(this.currentProcessText)) {
                this.currentProcessText.setText(PlayerUtil.generateTime(newPosition));
            }
            if (ObjectUtil.isNotNull(this.changeProcessText)) {
                String symbol = "";
                if (delta > 0) {
                    symbol = "+";
                } else {
                    symbol = "-";
                    delta = -delta;
                }
                this.changeProcessText.setText("[" + symbol + PlayerUtil.generateTime(+delta) + "]");
            }
        }
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }
        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        int i = (int) (index * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }
        // 显示
        if (ObjectUtil.isNotNull(this.ijkPlayerVolumeIcon)) {
            this.ijkPlayerVolumeIcon.setImageResource(i == 0 ? R.drawable.simple_player_volume_off_white_36dp : R.drawable.simple_player_volume_up_white_36dp);
        }
        if (ObjectUtil.isNotNull(this.ijkPlayerVolumeTxt)) {
            ijkPlayerVolumeTxt.setText(s);
        }
    }

    /**
     * 亮度滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {

        if (ObjectUtil.isNotNull(this.activity)) {
            if (brightness < 0) {
                brightness = this.activity.getWindow().getAttributes().screenBrightness;
                if (brightness <= 0.00f) {
                    brightness = 0.50f;
                } else if (brightness < 0.01f) {
                    brightness = 0.01f;
                }
            }
            Log.i(this.getClass().getSimpleName(), "brightness:" + brightness + ",percent:" + percent);
            WindowManager.LayoutParams lpa = this.activity.getWindow().getAttributes();
            lpa.screenBrightness = brightness + percent;
            if (lpa.screenBrightness > 1.0f) {
                lpa.screenBrightness = 1.0f;
            } else if (lpa.screenBrightness < 0.01f) {
                lpa.screenBrightness = 0.01f;
            }
            if (ObjectUtil.isNotNull(this.ijkPlayerBrightnessTxt)) {
                this.ijkPlayerBrightnessTxt.setText(((int) (lpa.screenBrightness * 100)) + "%");
            }
            this.activity.getWindow().setAttributes(lpa);
        }

    }

    /**
     * 显示上层控制页面
     */
    private void showView() {
        if (ScreenLock.LOCK == this.screenLock) {
            if (ObjectUtil.isNotNull(this.ijkPlayerLockIcon)) {
                this.ijkPlayerLockIcon.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 隐藏上层控制页面
     */
    private void hideView() {
        if (ObjectUtil.isNotNull(this.ijkPlayerLockIcon)) {
            this.ijkPlayerLockIcon.setVisibility(GONE);
        }
    }
/**
 * ==========================================================消息监听函数====================================
 */
    /**
     * 手势监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GestureListenerCode code) {
        if (ScreenLock.UNLOCK == this.screenLock) {//当解屏状态时才有执行权限
            if (GestureListenerCode.PROGRESS_SLIDE == code) {//进度条滑动监听
                Log.i(TAG, "进度条滑动百分比：" + code.getPercent());
                if (ObjectUtil.isNotNull(scrollProcessTextView)) {
                    scrollProcessTextView.setVisibility(VISIBLE);
                    onProgressSlide(code.getPercent());
                }

            } else if (GestureListenerCode.VOLUME_SLIDE == code) {//声音滑动监听
                Log.i(TAG, "声音滑动百分比：" + code.getPercent());
                if (ObjectUtil.isNotNull(this.ijkPlayerVolumeBox)) {
                    ijkPlayerVolumeBox.setVisibility(VISIBLE);
                    onVolumeSlide(code.getPercent());
                }

            } else if (GestureListenerCode.BRIGHTNESS_SLIDE == code) {//亮度滑动监听
                Log.i(TAG, "亮度滑动百分比：" + code.getPercent());
                if (ObjectUtil.isNotNull(this.ijkPlayerBrightnessBox)) {
                    ijkPlayerBrightnessBox.setVisibility(VISIBLE);
                    onBrightnessSlide(code.getPercent());
                }

            } else if (GestureListenerCode.END_GESTURE == code) {//手势结束
                if (code.getEndCode() == GestureListenerCode.PROGRESS_SLIDE) {
                    Log.i(TAG, "进度条滑动手势结束");
                    if (ObjectUtil.isNotNull(scrollProcessTextView)) {
                        scrollProcessTextView.setVisibility(GONE);
                    }
                } else if (code.getEndCode() == GestureListenerCode.VOLUME_SLIDE) {
                    Log.i(TAG, "声音滑动手势结束");
                    volume = -1;
                    if (ObjectUtil.isNotNull(this.ijkPlayerVolumeBox)) {
                        ijkPlayerVolumeBox.setVisibility(GONE);
                    }
                } else if (code.getEndCode() == GestureListenerCode.BRIGHTNESS_SLIDE) {
                    Log.i(TAG, "亮度滑动手势结束");
                    brightness = -1f;
                    if (ObjectUtil.isNotNull(this.ijkPlayerBrightnessBox)) {
                        ijkPlayerBrightnessBox.setVisibility(GONE);
                    }
                }

            }
        }
    }


    /**
     * 锁屏消息监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScreenLock screenLock) {
        this.screenLock = screenLock;
        if (ScreenLock.UNLOCK == screenLock) {
            this.hideView();
        } else if (ScreenLock.LOCK == screenLock) {
            this.showView();
        }
    }

    /**
     * eventBus 消息监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusCode eventBusCode) {
        if (EventBusCode.SHOW_VIEW == eventBusCode) {//显示view
            this.showView();
        } else if (EventBusCode.HIDE_VIEW == eventBusCode) {//隐藏view
            this.hideView();
        }
    }
}
