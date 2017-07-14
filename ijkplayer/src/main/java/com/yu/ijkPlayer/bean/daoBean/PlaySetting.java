package com.yu.ijkPlayer.bean.daoBean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by yu on 2017/7/14 0014.
 * 播放设置dao bean
 */
@DatabaseTable
public class PlaySetting extends BaseBean {
    @DatabaseField
    private int playMode;//播放模式，默认为全部循环，对应PlayMode枚举对象
}
