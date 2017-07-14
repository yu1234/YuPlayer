package com.yu.ijkPlayer.config;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkPlayer.R;
import com.yu.ijkPlayer.bean.Bean;
import com.yu.ijkPlayer.bean.daoBean.BaseBean;
import com.yu.ijkPlayer.dao.CommonDao;

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
    public static List<Bean> beans = new ArrayList<Bean>();
    public static Map<String, Bean> beanMap = new HashMap<String, Bean>();
    public static Map<String, CommonDao> daoMap = new HashMap<String, CommonDao>();
    /**
     * 获取dao配置bean
     */
    private static void getDaoBeansForResources(Context context) {
        beans.clear();
        beanMap.clear();
        XmlResourceParser xml = context.getResources().getXml(R.xml.dao_beans);
        try {
            xml.next();
            int eventType = xml.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                boolean inTitle = false;
                //到达title节点时标记一下
                if (ObjectUtil.isNotNull(xml.getName())) {
                    if (xml.getName().equals("bean") && XmlPullParser.START_TAG == eventType) {
                        Log.i("xml", "eventType:" + eventType);
                        inTitle = true;
                    }
                }
                //如过到达标记的节点则取出内容
                if (inTitle) {
                    Log.i("xml", "start");
                    Bean bean = new Bean();
                    String id = xml.getAttributeValue(null, "id");
                    String className = xml.getAttributeValue(null, "class");
                    bean.setId(id);
                    bean.setClassName(className);
                    beans.add(bean);
                    beanMap.put(id, bean);
                    Log.i("xml", "end" + beans.size());
                }
                xml.next();
                eventType = xml.getEventType();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Bean> getDaoBeans(Context context) {
        getDaoBeansForResources(context);
        return beans;

    }

    public static <T extends BaseBean> CommonDao<T> DaoFactory(Context context, String beanId) {
        Bean bean = beanMap.get(beanId);
        if (ObjectUtil.isNotNull(bean)) {
            if (ObjectUtil.isNotNull(daoMap.get(beanId))) {
                return daoMap.get(beanId);
            } else {
                try {
                    Class clazz = Class.forName(bean.getClassName());
                    CommonDao<T> commonDao = new CommonDao<T>(context, clazz);
                    if (ObjectUtil.isNotNull(commonDao)) {
                        daoMap.put(beanId, commonDao);
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
