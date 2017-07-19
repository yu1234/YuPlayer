package com.yu.ijkPlayer.bean.enumBean;

import com.xiaoleilu.hutool.util.ArrayUtil;

/**
 * Created by yu on 2017/7/19 0019.
 * 播放编码
 */

public enum PlayCode {
    SOFT_CODING(0,"软解码"),//软解码
    HARD_CODE(1,"硬解码");//硬解码
    private int id;
    private String name;
    PlayCode(int id, String name) {
        this.id=id;
        this.name=name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static PlayCode getPlayCodec(int id){
        PlayCode[] playCodes = PlayCode.values();
            if (ArrayUtil.isNotEmpty(playCodes)) {
                for (PlayCode playCode : playCodes) {
                    if (playCode.getId() == id) {
                        return playCode;
                    }
                }
            }
            return SOFT_CODING;
        }

}
