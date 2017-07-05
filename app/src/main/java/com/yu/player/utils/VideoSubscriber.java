package com.yu.player.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.player.Impl.ScanFileCompletedImpl;
import com.yu.player.Impl.SubscriberCompletedImpl;
import com.yu.player.Impl.SubscriberErrorImpl;
import com.yu.player.Impl.SubscriberNextImpl;
import com.yu.player.bean.VideoFile;
import com.yu.player.dao.CommonDao;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;

/**
 * Created by igreentree on 2017/7/4 0004.
 */

public class VideoSubscriber<T extends File>  implements MediaScannerConnection.MediaScannerConnectionClient {
    private final static String TAG = VideoSubscriber.class.getSimpleName();
    private static volatile VideoSubscriber instance;
    private  List<T> ts = new ArrayList<T>();
    private List<VideoFile> videoFiles = new ArrayList<VideoFile>();
    //回调接口
    private SubscriberCompletedImpl<T> subscriberCompleted;
    private SubscriberErrorImpl subscriberError;
    private SubscriberNextImpl<T> subscriberNext;
    private ScanFileCompletedImpl<VideoFile> scanFileCompleted;
    //普通参数
    private Context context;
    private MediaScannerConnection mediaScannerConnection;
    private int index;
    private Subscriber subscriber;
    //完成标志
    private static boolean flag;

    //实例化对象
    public static VideoSubscriber getInstance(Context context) {
        if (instance == null) {                         //Single Checked
            synchronized (VideoSubscriber.class) {
                if (instance == null) {                 //Double Checked
                    instance = new VideoSubscriber(context);
                }
            }
        }
        return instance;
    }

    //构造函数，私有，单例
    private VideoSubscriber(Context context) {
        if (ObjectUtil.isNull(context)) {
            throw new RuntimeException("context not null");
        }
        this.context = context;
        //实例化一个MediaScannerConnection
        mediaScannerConnection = new MediaScannerConnection(this.context, this);
        flag = true;
    }



    @Override
    public void onMediaScannerConnected() {
        Log.i(TAG, "VideoSubscriber.onMediaScannerConnected");
        //执行更新媒体库
        index = ts.size();
        for (T t : ts) {
            if (mediaScannerConnection.isConnected()) {
                mediaScannerConnection.scanFile(t.getAbsolutePath(), null);
            }
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        index--;
        ContentResolver contentResolver = this.context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        File file = new File(path);
        getVideoFile(cursor, file, uri);
        if (index <= 0) {
            mediaScannerConnection.disconnect();
            scanCompleted();
        }
    }

    /**
     * 获取媒体文件对象
     *
     * @return
     */
    private void getVideoFile(Cursor cursor, File file, Uri uri) {
        if (ObjectUtil.isNotNull(cursor) && cursor.moveToFirst() && ObjectUtil.isNotNull(file) && file.exists()) {
            //获取id
            long mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
            //获取名称
            String name = FileUtils.getFileNameNoEx(file.getName());
            //获取大小
            String formatSize = FileUtils.formatFileSize(file.length());
            //获取时长
            String formatDuration = FileUtils.formatDuration(this.context, file);
            //获取mime_type
            String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE));

            VideoFile videoFile = new VideoFile();
            videoFile.setCreateTime(new Date());
            videoFile.setPath(file.getAbsolutePath());
            videoFile.setmId(mId);
            videoFile.setName(name);
            videoFile.setFormatSize(formatSize);
            videoFile.setFormatDuration(formatDuration);
            videoFile.setMimeType(mimeType);
            videoFile.setUriStr(uri.toString());
            this.videoFiles.add(videoFile);
        }
    }

    /**
     * 文件扫描完成
     */
    private void scanCompleted() {
        Log.i(TAG, "VideoSubscriber.scanCompleted");
        //重置flag
        this.flag = true;
        //更新数据库
        updateVideoFileTable();
        //解除绑定
        if(ObjectUtil.isNotNull(subscriber)&&subscriber.isUnsubscribed()){
            subscriber.unsubscribe();
        }
        //完成回调
        if (ObjectUtil.isNotNull(this.scanFileCompleted)) {
            this.scanFileCompleted.onScanCompleted(CacheUtils.getVideoFileCache(this.context));
        }
    }

    /**
     * 更新VideoFile表
     *
     * @return
     */
    private void updateVideoFileTable() {
        //1.获取dao
        CommonDao<VideoFile> videoFileDao = DaoUtils.DaoFactory(this.context, "VideoFile");

        if (CollectionUtil.isNotEmpty(this.videoFiles) && ObjectUtil.isNotNull(videoFileDao)) {
            //获取新增
            for (VideoFile videoFile : this.videoFiles) {
                Map<String, Object> fieldValues = new HashMap<String, Object>();
                fieldValues.put("path", videoFile.getPath());
                try {
                    List<VideoFile> existFile = videoFileDao.getDao().queryForFieldValuesArgs(fieldValues);
                    if (CollectionUtil.isNotEmpty(existFile) && existFile.get(0).isWatch()) {
                        videoFile.setNew(false);
                    } else {
                        videoFile.setNew(true);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            //删除旧数据
            boolean flag = videoFileDao.batchDelete(videoFileDao.queryAll());
            //插入新数据
            if (flag) {
                videoFileDao.batchAdd(this.videoFiles);
            }

        }

    }

    /**
     * 开始扫描
     *
     * @return
     */
    public void startScan() {
        Log.i(TAG, "VideoSubscriber.startScan");
        Log.i(TAG, "VideoSubscriber.startScan:" + this.flag);
        if (this.flag) {
            //初始化缓存变量
            ts.clear();
            videoFiles.clear();
            this.flag = false;
            //开始扫描
            ReadFileUtil readFileUtil = ReadFileUtil.getInstance();
            this.subscriber=new MySubscriber();
            readFileUtil.getFiles(ReadFileUtil.FileType.VOIDEO,this.subscriber);
        }
    }

    class MySubscriber extends Subscriber<T>{
        @Override
        public void onCompleted() {
            Log.i(TAG, "VideoSubscriber.onCompleted");
            if (CollectionUtil.isNotEmpty(ts)) {
                //发出更新媒体库连接
                mediaScannerConnection.connect();
            } else {
                scanCompleted();
            }
            if (ObjectUtil.isNotNull(subscriberCompleted)) {
                subscriberCompleted.onCompleted(ts);
            }
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "VideoSubscriber.onError", e);
            if (ObjectUtil.isNotNull(subscriberError)) {
                subscriberError.onError(e);
            }

        }

        @Override
        public void onNext(T t) {
            Log.i(TAG, "VideoSubscriber.onNext");
            if (ObjectUtil.isNotNull(t)) {
                if (ObjectUtil.isNotNull(ts)) {
                    ts.add(t);
                }
            }
            if (ObjectUtil.isNotNull(subscriberNext)) {
                subscriberNext.onNext(t);
            }
        }
    }

    //============================================回调设置========================================
    public SubscriberCompletedImpl getSubscriberCompleted() {
        return subscriberCompleted;
    }

    public void setSubscriberCompleted(SubscriberCompletedImpl subscriberCompleted) {
        this.subscriberCompleted = subscriberCompleted;
    }

    public SubscriberErrorImpl getSubscriberError() {
        return subscriberError;
    }

    public void setSubscriberError(SubscriberErrorImpl subscriberError) {
        this.subscriberError = subscriberError;
    }

    public SubscriberNextImpl getSubscriberNext() {
        return subscriberNext;
    }

    public void setSubscriberNext(SubscriberNextImpl subscriberNext) {
        this.subscriberNext = subscriberNext;
    }

    public ScanFileCompletedImpl<VideoFile> getScanFileCompleted() {
        return scanFileCompleted;
    }

    public void setScanFileCompleted(ScanFileCompletedImpl<VideoFile> scanFileCompleted) {
        this.scanFileCompleted = scanFileCompleted;
    }
}
