package com.yu.ijkplayer.bean;

/**
 * Created by igreentree on 2017/7/10 0010.
 */

public enum EventBusCode {
    HIDE_VIEW//隐藏控制器页面
    , SHOW_VIEW//显示
    , ACTIVITY_FINISH//播放器页面关闭
    , TIME_CHANGE//时间改变
    , BATTERY_CHANGE//电量改变
    , NETWORK_CHANGE//网络状态变化
    , POWER_DISCONNECTED//断电
    , POWER_CONNECTED//充电
    , PROGRESS_CHANGE;//进度条变化


    int CurrentBattery = 0;
    boolean isCharging;

    public int getCurrentBattery() {
        return CurrentBattery;
    }

    public void setCurrentBattery(int currentBattery) {
        CurrentBattery = currentBattery;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
    }
}
