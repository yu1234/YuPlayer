package com.yu.player.utils;

import android.content.Context;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.player.bean.BaseBean;
import com.yu.player.bean.Bean;
import com.yu.player.dao.CommonDao;
import com.yu.player.global.MyApplication;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by igreentree on 2017/7/4 0004.
 */

public class DaoUtils {
    public static List<Bean> getDaoBeans(Context context) throws IOException, XmlPullParserException {
        return MyApplication.beans;
    }

    public static <T extends BaseBean> CommonDao<T> DaoFactory(Context context, String beanId) {
        Bean bean = MyApplication.beanMap.get(beanId);
        if (ObjectUtil.isNotNull(bean)) {
            if (ObjectUtil.isNotNull(MyApplication.daoMap.get(beanId))) {
                return MyApplication.daoMap.get(beanId);
            } else {
                try {
                    Class clazz = Class.forName(bean.getClassName());
                    CommonDao<T> commonDao = new CommonDao<T>(context, clazz);
                    if (ObjectUtil.isNotNull(commonDao)) {
                        MyApplication.daoMap.put(beanId, commonDao);
                    }
                    return commonDao;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
