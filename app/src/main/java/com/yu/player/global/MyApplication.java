package com.yu.player.global;

import android.app.Application;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.player.R;
import com.yu.player.bean.Bean;
import com.yu.player.dao.CommonDao;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by igreentree on 2017/6/28 0028.
 */

public class MyApplication extends Application {
    public static List<Bean> beans = new ArrayList<Bean>();
    public static Map<String, Bean> beanMap = new HashMap<String, Bean>();
    public static Map<String, CommonDao> daoMap = new HashMap<String, CommonDao>();

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        //读取dao配置文件
        getDaoBeans();
    }

    /**
     * 获取dao配置bean
     */
    private void getDaoBeans() {
        XmlResourceParser xml = this.getResources().getXml(R.xml.dao_beans);
        try {
            xml.next();
            int eventType = xml.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                boolean inTitle = false;
                //到达title节点时标记一下
                if (ObjectUtil.isNotNull(xml.getName())) {
                    if (xml.getName().equals("bean")&&XmlPullParser.START_TAG==eventType) {
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
}
