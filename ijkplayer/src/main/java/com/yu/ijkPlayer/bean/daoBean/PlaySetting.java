package com.yu.ijkPlayer.bean.daoBean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.yu.ijkPlayer.bean.enumBean.PlayCode;
import com.yu.ijkPlayer.bean.enumBean.PlayMode;
import com.yu.ijkPlayer.bean.enumBean.PlayScreenSize;
import com.yu.ijkPlayer.dao.PlaySettingDao;
import com.yu.ijkPlayer.global.IjkPlayerLib;

/**
 * Created by yu on 2017/7/14 0014.
 * 播放设置dao bean
 */
@DatabaseTable
public class PlaySetting extends BaseBean {
    public static PlaySettingDao dao = new PlaySettingDao(IjkPlayerLib.getInstance().getContext(), PlaySetting.class);
    @DatabaseField
    private int playMode;//播放模式，默认为全部循环，对应PlayMode枚举对象

    @DatabaseField
    private int  playScreenSize;//播放页面尺寸
    @DatabaseField
    private int  playCode;//解码器

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    public int getPlayScreenSize() {
        return playScreenSize;
    }

    public void setPlayScreenSize(int playScreenSize) {
        this.playScreenSize = playScreenSize;
    }

    public int getPlayCode() {
        return playCode;
    }

    public void setPlayCode(int playCode) {
        this.playCode = playCode;
    }

    public static PlaySetting getDefaultPlaySetting() {
        PlaySetting playSetting = new PlaySetting();
        playSetting.setPlayMode(PlayMode.ALL_CYCLE.getId());
        playSetting.setPlayScreenSize(PlayScreenSize.FIT_PARENT.getId());
        playSetting.setPlayCode(PlayCode.SOFT_CODING.getId());
        return playSetting;
    }
}
