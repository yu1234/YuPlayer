package com.yu.ijkPlayer.bean.enumBean;

import com.yu.ijkPlayer.R;

/**
 * Created by igreentree on 2017/7/12 0012.
 */

public enum MediaQuality {
    P_1080("1080P",1080,R.drawable.ic_player_media_quality_bd),//1080p
    P_720("超清",720,R.drawable.ic_player_media_quality_super),//超清
    P_480("高清",480,R.drawable.ic_player_media_quality_high),//高清
    P_360("标清",360,R.drawable.ic_player_media_quality_medium),//标清
    P_240("流畅",240,R.drawable.ic_player_media_quality_smooth);//流畅

    int iconResource;


    private String name;
    private int code;
    MediaQuality(String name,int code,int iconResource) {
        this.name=name;
        this.code=code;
        this.iconResource=iconResource;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static MediaQuality getMediaQuality(int height) {
        if (height < 350) {
            return P_240;
        } else if (height >= 350 && height < 470) {
            return P_360;
        } else if (height >= 470 && height < 710) {
            return P_480;
        } else if (height >= 710 && height < 1000) {
            return P_720;
        } else if (height >= 1000) {
            return P_1080;
        } else {
            return P_240;
        }
    }
}
