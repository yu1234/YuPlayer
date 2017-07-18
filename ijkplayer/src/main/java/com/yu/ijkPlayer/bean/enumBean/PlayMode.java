package com.yu.ijkPlayer.bean.enumBean;

import com.xiaoleilu.hutool.util.ArrayUtil;

/**
 * Created by igreentree on 2017/7/11 0011.
 * 播放模式
 */

public enum PlayMode {
    ALL_CYCLE(1,"列表循环"),//全部循环
    ONE_CYCLE(2,"单个循环"),//单一循环
    RANDOM(3,"列表随机"),//随机
    STOP(4,"播完退出");//停止
    private int id;
    private String name;
    PlayMode(int id,String name) {
        this.id = id;
        this.name=name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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
