package com.yu.ijkPlayer.dao;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkPlayer.bean.daoBean.BaseBean;


import java.sql.SQLException;
import java.util.List;

/**
 * Created by igreentree on 2017/6/5 0005.
 */

public class CommonDao<T extends BaseBean> {
    private Context context;
    private Dao dao;
    private MyDbHelper helper;

    public CommonDao(Context context, Class<T> clazz) throws SQLException {
        this.context = context;
        helper = MyDbHelper.getHelper(context);
        dao = helper.getDao(clazz);
    }

    /**
     * 增加一条信息
     *
     * @param t
     */
    public boolean add(T t) {
        boolean flag = false;
        try {
            if (t.getId() == 0) {
                t.setId(System.currentTimeMillis());
            }
            int rows = dao.create(t);
            if (rows > 0) {
                flag = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 批量增加信息
     *
     * @param ts
     */
    public boolean batchAdd(List<T> ts) {
        boolean flag = false;
        if (ObjectUtil.isNotNull(ts)) {
            for (T t : ts) {
                flag= this.add(t);
            }
        }
        return flag;
    }

    /**
     * 更新一条信息
     *
     * @param t
     */
    public boolean update(T t) {
        boolean flag = false;
        try {
            int rows = dao.update(t);
            if (rows > 0) {
                flag = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 批量删除信息
     *
     * @return
     */
    public boolean batchDelete(List<T> ts) {
        boolean flag = true;
        try {
            if (ObjectUtil.isNotNull(ts)) {
                for (T t : ts) {
                    dao.delete(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 根据条件查询
     *
     * @param queryBuilder
     * @param <M>
     * @return
     * @throws SQLException
     */
    public <M> List<M> query(QueryBuilder queryBuilder) {

        if (ObjectUtil.isNotNull(queryBuilder)) {
            List<M> list = null;
            try {
                list = queryBuilder.query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }
        return null;
    }

    /**
     * 查询全部
     *
     * @return
     * @throws SQLException
     */
    public List<T> queryAll() {

        if (ObjectUtil.isNotNull(this.dao)) {
            List<T> list = null;
            try {
                list = this.dao.queryForAll();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }
        return null;
    }

    /**
     * 根据id查询对象
     *
     * @return
     * @throws SQLException
     */
    public T queryById(Object id) throws SQLException {
        if (ObjectUtil.isNotNull(this.dao) && ObjectUtil.isNotNull(id)) {

            T o = (T) dao.queryForId(id);
            return o;
        }
        return null;
    }

    /**
     * 获取dao
     *
     * @return
     */
    public Dao getDao() {
        return dao;
    }
}
