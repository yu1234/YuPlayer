package com.yu.ijkPlayer.dao;

import com.j256.ormlite.stmt.QueryBuilder;
import com.yu.ijkPlayer.bean.daoBean.BaseBean;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by igreentree on 2017/7/17 0017.
 */
public interface IBaseDao<T extends BaseBean>  {
    /**
     * 增加一条信息
     *
     * @param t
     */
     boolean add(T t);
    /**
     * 批量增加信息
     *
     * @param ts
     */
     boolean batchAdd(List<T> ts);

    /**
     * 更新一条信息
     *
     * @param t
     */
     boolean update(T t);
    /**
     * 更新或插入信息
     *
     * @param t
     */
    boolean updateOrAdd(T t);
    /**
     * 批量删除信息
     *
     * @return
     */
     boolean batchDelete(List<T> ts);
    /**
     * 查询全部
     *
     * @return
     * @throws SQLException
     */
     List<T> queryAll();
    /**
     * 根据id查询对象
     *
     * @return
     * @throws SQLException
     */
     T queryById(Object id) throws SQLException;
    /**
     * 根据条件查询
     *
     * @param queryBuilder
     * @return
     * @throws SQLException
     */
      List<T> queryByBuilder(QueryBuilder queryBuilder);
}
