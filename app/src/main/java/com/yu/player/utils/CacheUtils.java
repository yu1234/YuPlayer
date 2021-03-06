package com.yu.player.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.j256.ormlite.stmt.QueryBuilder;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yu.player.bean.VideoFile;
import com.yu.player.dao.CommonDao;

import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by igreentree on 2017/7/5 0005.
 */

public class CacheUtils {

    public static List<VideoFile> getVideoFileCache(Context context) {
        List<VideoFile> videoFiles = null;
        //1.获取dao
        CommonDao<VideoFile> videoFileDao = DaoUtils.DaoFactory(context, "VideoFile");
        if (ObjectUtil.isNotNull(videoFileDao)) {
            QueryBuilder queryBuilder = videoFileDao.getDao().queryBuilder();
            queryBuilder.orderBy("name", false);
            videoFiles = videoFileDao.query(queryBuilder);
            if (CollectionUtil.isNotEmpty(videoFiles)) {
                Iterator<VideoFile> iterator = videoFiles.iterator();
                while (iterator.hasNext()) {
                    VideoFile videoFile = iterator.next();
                    if (StrUtil.isNotBlank(videoFile.getPath())) {
                        File file = new File(videoFile.getPath());
                        if(file.exists()){
                            //获取缩略图
                            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFile.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                            videoFile.setThumbnails(thumbnail);
                            //获取Uri
                            if (StrUtil.isNotBlank(videoFile.getUriStr())) {
                                Uri uri = Uri.parse(videoFile.getUriStr());
                                videoFile.setUri(uri);


                            }
                        }else {
                            try {
                                videoFileDao.getDao().delete(videoFile);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            iterator.remove();

                        }
                    } else {
                        try {
                            videoFileDao.getDao().delete(videoFile);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        iterator.remove();
                    }
                }
            }
        }
        return videoFiles;
    }
}
