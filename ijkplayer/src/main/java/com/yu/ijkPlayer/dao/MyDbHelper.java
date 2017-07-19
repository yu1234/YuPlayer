package com.yu.ijkPlayer.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.yu.ijkPlayer.bean.Bean;
import com.yu.ijkPlayer.config.DaoConfig;


import java.sql.SQLException;
import java.util.List;

/**
 * Created by igreentree on 2017/6/5 0005.
 */

public class MyDbHelper extends OrmLiteSqliteOpenHelper {
    private static final String CDB_DB_NAME = "ijk_player_media_db";
    private static final int DB_VERSION = 3;
    private static MyDbHelper instance;
    private Context context;

    public MyDbHelper(Context context) {
        /**
         * 参数说明：
         * context：上下文。
         * databaseName： 数据库名。
         * factory： 游标实例，多数时候设置成NULL。
         * databaseVersion：数据库版本，当数据库版本升高时，会调用onUpgrade（）方法。
         */
        super(context, CDB_DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            List<Bean> beans = DaoConfig.getDaoBeans();
            if (CollectionUtil.isNotEmpty(beans)) {
                for (Bean bean : beans) {
                    Class clazz = Class.forName(bean.getClassName());
                    TableUtils.createTable(connectionSource, clazz);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            List<Bean> beans = DaoConfig.getDaoBeans();
            if (CollectionUtil.isNotEmpty(beans)) {
                for (Bean bean : beans) {
                    Class clazz = Class.forName(bean.getClassName());
                    TableUtils.createTable(connectionSource, clazz);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单例获取该Helper
     *
     * @param context
     * @return
     */
    public static synchronized MyDbHelper getHelper(Context context) {
        context = context.getApplicationContext();
        if (instance == null) {
            synchronized (MyDbHelper.class) {
                if (instance == null)
                    instance = new MyDbHelper(context);
            }
        }

        return instance;
    }

    public synchronized Dao getDao(Class clazz) throws SQLException {
        Dao dao =  super.getDao(clazz);
        return dao;
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        super.close();
    }
}
