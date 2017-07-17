package com.yu.ijkPlayer.bean.daoBean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.yu.ijkPlayer.bean.enumBean.PlayMode;
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

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    public static PlaySetting getDefaultPlaySetting() {
        PlaySetting playSetting = new PlaySetting();
        playSetting.setPlayMode(PlayMode.ALL_CYCLE.getId());
        return playSetting;
    }
}
