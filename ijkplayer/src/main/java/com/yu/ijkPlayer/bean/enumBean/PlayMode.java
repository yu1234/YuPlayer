package com.yu.ijkPlayer.bean.enumBean;

import com.xiaoleilu.hutool.util.ArrayUtil;

/**
 * Created by igreentree on 2017/7/11 0011.
 * 播放模式
 */

public enum PlayMode {
    ALL_CYCLE(1,"列表循环",false),//全部循环
    ONE_CYCLE(2,"单个循环",false),//单一循环
    RANDOM(3,"列表随机",false),//随机
    STOP(4,"播完退出",false);//停止
    private int id;
    private String name;
    private boolean isPrevious;
    PlayMode(int id,String name,boolean isPrevious) {
        this.id = id;
        this.name=name;
        this.isPrevious=isPrevious;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPrevious() {
        return isPrevious;
    }

    public void setPrevious(boolean previous) {
        isPrevious = previous;
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
