package com.yu.ijkplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkplayer.MainActivity;
import com.yu.ijkplayer.bean.VideoijkBean;
import com.yu.ijkplayer.view.IjkPlayerControllerBottom;

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
}
