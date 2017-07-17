package com.yu.ijkPlayer.config;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkPlayer.R;
import com.yu.ijkPlayer.bean.Bean;
import com.yu.ijkPlayer.bean.daoBean.BaseBean;
import com.yu.ijkPlayer.dao.CommonDao;
import com.yu.ijkPlayer.global.IjkPlayerLib;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by igreentree on 2017/7/14 0014.
 */

public class DaoConfig {
    /**
     * 获取dao配置bean
     */
    public static List<Bean> getDaoBeans() {
        return IjkPlayerLib.beans;

    }

    public static <T extends BaseBean> CommonDao<T> DaoFactory(Context context, String beanId) {
        Bean bean = IjkPlayerLib.beanMap.get(beanId);
        if (ObjectUtil.isNotNull(bean)) {
            if (ObjectUtil.isNotNull(IjkPlayerLib.daoMap.get(beanId))) {
                return IjkPlayerLib.daoMap.get(beanId);
            } else {
                try {
                    Class clazz = Class.forName(bean.getClassName());
                    CommonDao<T> commonDao = new CommonDao<T>(context, clazz);
                    if (ObjectUtil.isNotNull(commonDao)) {
                        IjkPlayerLib.daoMap.put(beanId, commonDao);
                    }
                    return commonDao;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
