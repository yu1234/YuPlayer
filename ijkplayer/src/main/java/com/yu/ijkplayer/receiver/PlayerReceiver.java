package com.yu.ijkplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.BatteryManager;

import com.yu.ijkplayer.bean.EventBusCode;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by igreentree on 2017/7/10 0010.
 */

public class PlayerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {//时间改变
            EventBus.getDefault().post(EventBusCode.TIME_CHANGE);
        } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {//电量改变
            EventBusCode battery = EventBusCode.BATTERY_CHANGE;
            //获取当前充电状态
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            battery.setCharging(isCharging);
            //获取当前电量
            int level = intent.getIntExtra("level", 0);
            battery.setCurrentBattery(level);
            EventBus.getDefault().post(battery);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {//网络变化
            EventBus.getDefault().post(EventBusCode.NETWORK_CHANGE);
        }else if(Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())){//断电
            EventBus.getDefault().post(EventBusCode.POWER_DISCONNECTED);
        } else if(Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())){//充电
            EventBus.getDefault().post(EventBusCode.POWER_CONNECTED);
        }

    }
}
