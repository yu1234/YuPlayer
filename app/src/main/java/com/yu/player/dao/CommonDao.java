package com.yu.player.dao;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

/**
 * Created by igreentree on 2017/6/5 0005.
 */

public class CommonDao<T> {
    private Context context;
    private Dao dao;
    private MyDbHelper helper;

    public CommonDao(Context context,Class<T> clazz) {
        this.context = context;
        try {
            helper = MyDbHelper.getHelper(context);
            dao = helper.getDao(clazz);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加一条信息
     *
     * @param t
     */
    public void add(T t) {
        try {
            dao.create(t);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 更新一条信息
     *
     * @param t
     */
    public void update(T t) {
        try {
            dao.update(t);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }

}
