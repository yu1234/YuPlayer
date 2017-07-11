package com.yu.ijkplayer.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkplayer.R;
import com.yu.ijkplayer.R2;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.bean.NetWorkStatus;
import com.yu.ijkplayer.receiver.PlayerReceiver;
import com.yu.ijkplayer.utils.NetworkUtils;
import com.yu.ijkplayer.utils.SrceenUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/7/7 0007.
 */

public class IjkPlayerControllerTop extends LinearLayout implements View.OnClickListener {
    private static final String TAG = IjkPlayerControllerTop.class.getSimpleName();
    /**
     * 依附的activity
     **/
    private Activity activity;

    /**
     * 广播监听
     */
    private PlayerReceiver receiver;
    /**
     * 控件注入
     */
    @BindView(R2.id.video_title)
    TextView videoTitle;
    @BindView(R2.id.player_exit)
    ImageView playerExit;
    @BindView(R2.id.toolbar)
    Toolbar toolbar;
    @BindView((R2.id.orientation_overlay_button))
    LinearLayout orientationOverlayButton;
    @BindView((R2.id.current_time))
    TextView currentTime;
    @BindView((R2.id.network_status))
    TextView networkStatus;
    @BindView((R2.id.battery))
    TextView battery;
    @BindView((R2.id.battery_image))
    ImageView batteryImage;

    public IjkPlayerControllerTop(Context context) {
        this(context, null);
    }

    public IjkPlayerControllerTop(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkPlayerControllerTop(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.activity = (Activity) context;
        //填充页面
        View view = LayoutInflater.from(context).inflate(R.layout.ijk_player_controller_top, this);
        ButterKnife.bind(this, view);
        //参数初始化
        initParams();
        //事件注册
        registerListener();
        //EventBus注册
        EventBus.getDefault().register(this);
        //隐藏导航栏
        ImmersionBar.with(this.activity).hideBar(BarHide.FLAG_HIDE_BAR).init();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.player_exit) {
            if (ObjectUtil.isNotNull(this.activity)) {
                this.activity.finish();
            }
        } else if (v.getId() == R.id.orientation_overlay_button) {
            if (ObjectUtil.isNotNull(this.activity)) {
                Configuration cf = this.activity.getResources().getConfiguration(); //获取设置的配置信息
                int ori = cf.orientation; //获取屏幕方向
                if (ori == cf.ORIENTATION_LANDSCAPE) {
                    this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                } else if (ori == cf.ORIENTATION_PORTRAIT) {
                    this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        }
    }

    /**
     * 参数初始化
     */
    private void initParams() {
        //网络状态初始化
        NetWorkStatus networkStatus = NetworkUtils.getNetworkStatus(this.activity);
        if (ObjectUtil.isNotNull(this.networkStatus)) {
            this.networkStatus.setText(networkStatus.getMsg());
        }
        //当前时间
        if (ObjectUtil.isNotNull(this.currentTime)) {
            this.currentTime.setText(DateUtil.format(new Date(), "HH:mm"));
        }
        //初始化电量
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.activity.registerReceiver(null, ifilter);
        int currentBattery = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        batteryChange(currentBattery, isCharging);
    }

    /**
     * 设置video title
     */
    public void setTitle(String title) {
        if (ObjectUtil.isNotNull(videoTitle)) {
            videoTitle.setText(title);
        }
    }

    /**
     * 事件注册
     */
    private void registerListener() {
        //返回按键点击事件注册
        if (ObjectUtil.isNotNull(this.playerExit)) {
            this.playerExit.setOnClickListener(this);
        }
        //屏幕旋转
        if (ObjectUtil.isNotNull(this.orientationOverlayButton)) {
            this.orientationOverlayButton.setOnClickListener(this);
        }
        //注册广播
        if (ObjectUtil.isNotNull(this.activity)) {
            receiver = new PlayerReceiver();
            this.activity.registerReceiver(receiver, new IntentFilter(Intent.ACTION_TIME_TICK));
            this.activity.registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            this.activity.registerReceiver(receiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
            this.activity.registerReceiver(receiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
            this.activity.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        }
    }

    /**
     * 显示上层控制页面
     */
    private void showView() {
        if (ObjectUtil.isNotNull(this.activity)) {
            ImmersionBar.with(this.activity).transparentNavigationBar().navigationBarColor(R.color.c_light_black).hideBar(BarHide.FLAG_SHOW_BAR).init();
            ImmersionBar.with(this.activity).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).init();
            int navBarHeight = SrceenUtils.getNavigationBarSize(this.activity).x;
            int realW = SrceenUtils.getRealScreenSize(this.activity).x;
            int w =realW- navBarHeight;
            if (w > 0) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
                params.width = w;
                this.setLayoutParams(params);
            }
        }
        this.setVisibility(VISIBLE);
    }

    /**
     * 隐藏上层控制页面
     */
    private void hideView() {
        if (ObjectUtil.isNotNull(this.activity)) {
            ImmersionBar.with(this.activity).hideBar(BarHide.FLAG_HIDE_BAR).fullScreen(false).init();
        }
        this.setVisibility(GONE);
    }

    /**
     * 电量改变
     */
    private void batteryChange(int currentBattery, boolean isCharging) {

        if (ObjectUtil.isNotNull(this.batteryImage)) {
            if (isCharging) {
                this.batteryImage.setImageResource(R.drawable.player_battery_charging_icon);
                if (ObjectUtil.isNotNull(this.battery)) {
                    this.battery.setText("");
                }
            } else {
                if (ObjectUtil.isNotNull(this.battery)) {
                    this.battery.setText(currentBattery + "");
                }
                if (currentBattery < 10) {
                    this.batteryImage.setImageResource(R.drawable.player_battery_red_icon);
                } else if (currentBattery > 90) {
                    this.batteryImage.setImageResource(R.drawable.player_battery_green_icon);
                } else {
                    this.batteryImage.setImageResource(R.drawable.player_battery_icon);
                }
            }

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
        } else if (EventBusCode.ACTIVITY_FINISH == eventBusCode) {//activity退出
            EventBus.getDefault().unregister(this);
            this.activity.unregisterReceiver(receiver);
        } else if (EventBusCode.TIME_CHANGE == eventBusCode) {//时间改变
            if (ObjectUtil.isNotNull(this.currentTime)) {
                this.currentTime.setText(DateUtil.format(new Date(), "HH:mm"));
            }
        } else if (EventBusCode.BATTERY_CHANGE == eventBusCode) {//电量改变
            batteryChange(eventBusCode.getCurrentBattery(), eventBusCode.isCharging());
        } else if (EventBusCode.NETWORK_CHANGE == eventBusCode) {//网络变化
            if (ObjectUtil.isNotNull(this.networkStatus)) {
                NetWorkStatus networkStatus = NetworkUtils.getNetworkStatus(this.activity);
                this.networkStatus.setText(networkStatus.getMsg());
            }
        } else if (EventBusCode.POWER_DISCONNECTED == eventBusCode) {//断电
            Log.i(TAG, "断电");
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = this.activity.registerReceiver(null, ifilter);
            int currentBattery = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            batteryChange(currentBattery, false);
        } else if (EventBusCode.POWER_CONNECTED == eventBusCode) {//充电
            Log.i(TAG, "充电");
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = this.activity.registerReceiver(null, ifilter);
            int currentBattery = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            batteryChange(currentBattery, true);
        }
    }


}
