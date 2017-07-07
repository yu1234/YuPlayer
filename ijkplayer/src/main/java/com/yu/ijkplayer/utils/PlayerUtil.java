package com.yu.ijkplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkplayer.MainActivity;
import com.yu.ijkplayer.bean.VideoijkBean;

/**
 * Created by igreentree on 2017/7/7 0007.
 */

public class PlayerUtil {
    public static void playVideo(Context context, Uri uri) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static VideoijkBean getVideoInfo(Context context, Uri uri) {
        String[] filePathColumn = {MediaStore.MediaColumns.TITLE, MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        VideoijkBean bean = new VideoijkBean();
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
            cursor.close();
        }
        return bean;
    }
}