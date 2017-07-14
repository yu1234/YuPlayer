package com.yu.ijkPlayer.bean.enumBean;

/**
 * Created by igreentree on 2017/7/11 0011.
 * 播放状态监听
 */

public enum PlayerListenerEnum {
    //子-->主
    IN_PAUSE,//暂停
    IN_START,//开始
    IN_RESTART,//重新开始
    IN_RESUME,//恢复
    IN_STOP,//停止
    IN_RELEASE,//释放
    //主-->子
    OUT_PAUSE,//暂停
    OUT_START,//开始
    OUT_RESTART,//重新开始
    OUT_RESUME,//恢复
    OUT_STOP,//停止
    OUT_RELEASE;//释放
}
