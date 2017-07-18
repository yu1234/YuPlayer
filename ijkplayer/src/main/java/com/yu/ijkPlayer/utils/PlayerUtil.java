package com.yu.ijkPlayer.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkPlayer.MainActivity;
import com.yu.ijkPlayer.bean.VideoIjkBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by igreentree on 2017/7/7 0007.
 */

public class PlayerUtil {
    private static final String TAG = PlayerUtil.class.getSimpleName();

    public static void playVideo(Context context, Uri uri) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void playVideo(Context context, Uri uri, List<Uri> uriList) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setData(uri);
        if (CollectionUtil.isNotEmpty(uriList)) {
            List<String> uris = new ArrayList<String>();
            for (Uri uri1 : uriList) {
                uris.add(uri1.toString());
            }
            Bundle bundle = new Bundle();
            bundle.putString("uriList", com.alibaba.fastjson.JSON.toJSONString(uris));
            intent.putExtras(bundle);
        }


        context.startActivity(intent);
    }


    public static VideoIjkBean getVideoInfo(Context context, Uri uri) {
        String[] filePathColumn = {MediaStore.MediaColumns.TITLE, MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        VideoIjkBean bean = new VideoIjkBean();
        if (ObjectUtil.isNotNull(cursor) && cursor.moveToFirst()) {
            cursor.moveToFirst();
            int titleColumnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String title = cursor.getString(titleColumnIndex);
            bean.setTitle(title);
            int pathColumnIndex = cursor.getColumnIndex(filePathColumn[1]);
            String path = cursor.getString(pathColumnIndex);
            bean.setUrl(path);
            //获取缩略图
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
            bean.setThumbnails(thumbnail);
            //获取分辨率
            MediaMetadataRetriever retr = new MediaMetadataRetriever();
            retr.setDataSource(path);
            Bitmap bm = retr.getFrameAtTime();
            bean.setStream(bm.getHeight() + "");
            bean.setWidth(bm.getWidth());
            bean.setHeight(bm.getHeight());
            cursor.close();
        }
        return bean;
    }

    /**
     * 时长格式化显示
     */
    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 生成随机数
     * 在一定范围内生成随机数.
     * 比如此处要求在[0 - n)内生成随机数.
     * 注意:包含0不包含n
     */
    public static int getRandom(int n) {
        Random random = new Random();
        return random.nextInt(n);
    }
}
