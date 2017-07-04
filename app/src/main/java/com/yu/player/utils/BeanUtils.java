package com.yu.player.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.yu.player.R;
import com.yu.player.bean.Bean;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igreentree on 2017/7/4 0004.
 */

public class BeanUtils {
    public static List<Bean> getDaoBeans(Context context) throws IOException, XmlPullParserException {
        List<Bean> beans = new ArrayList<Bean>();
        XmlResourceParser xml = context.getResources().getXml(R.xml.dao_beans);
        xml.next();
        int eventType = xml.getEventType();
        boolean inTitle = false;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            //到达title节点时标记一下
            if (eventType == XmlPullParser.START_TAG) {
                if (xml.getName().equals("bean")) {
                    inTitle = true;
                }
            }
            //如过到达标记的节点则取出内容
            if (eventType == XmlPullParser.TEXT && inTitle) {
                Bean bean = new Bean();
                String id = xml.getAttributeValue(null, "id");
                String className = xml.getAttributeValue(null, "class");
                bean.setId(id);
                bean.setClassName(className);
                beans.add(bean);
            }

            xml.next();
            eventType = xml.getEventType();
        }
        return beans;
    }
}
