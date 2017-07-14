package com.yu.ijkPlayer.bean.enumBean;

import com.xiaoleilu.hutool.util.ArrayUtil;

/**
 * Created by igreentree on 2017/7/11 0011.
 * 播放模式
 */

public enum PlayMode {
    ALL_CYCLE(1),//全部循环
    ONE_CYCLE(2),//单一循环
    RANDOM(3),//随机
    STOP(4);//停止
    private int id;

    PlayMode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PlayMode getPlayMode(int id) {
        PlayMode[] playModes = PlayMode.values();
        if (ArrayUtil.isNotEmpty(playModes)) {
            for (PlayMode playMode : playModes) {
                if (playMode.getId() == id) {
                    return playMode;
                }
            }
        }
        return ALL_CYCLE;
    }
}
