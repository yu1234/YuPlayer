package com.yu.ijkPlayer.dao;

import android.content.Context;

import com.j256.ormlite.stmt.QueryBuilder;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.yu.ijkPlayer.bean.daoBean.PlaySetting;

import java.util.List;

/**
 * Created by igreentree on 2017/7/17 0017.
 */

public class PlaySettingDao extends CommonDao<PlaySetting> {

    public PlaySettingDao(Context context, Class<PlaySetting> clazz) {
        super(context, clazz);
    }

    /**
     * 获取最新的一条信息
     *
     * @return
     */
    public PlaySetting getLatest() {
        QueryBuilder queryBuilder = this.getDao().queryBuilder();
        queryBuilder.limit(1L);
        queryBuilder.orderBy("createTime", false);
        List<PlaySetting> playSettings = this.queryByBuilder(queryBuilder);
        if (CollectionUtil.isNotEmpty(playSettings)) {
            return playSettings.get(0);
        } else {
            return null;
        }
    }

}
