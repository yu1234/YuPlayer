package com.yu.player.adapter;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.player.Impl.SubscriberCompletedImpl;
import com.yu.player.Impl.SubscriberErrorImpl;
import com.yu.player.Impl.SubscriberNextImpl;
import com.yu.player.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

/**
 * Created by igreentree on 2017/7/4 0004.
 */

public class VideoSubscriberAdapter<T extends File> extends Subscriber<T> implements MediaScannerConnection.MediaScannerConnectionClient {
    private final static String TAG = VideoSubscriberAdapter.class.getSimpleName();
    private List<T> ts = new ArrayList<T>();
    //回调接口
    private SubscriberCompletedImpl<T> subscriberCompleted;
    private SubscriberErrorImpl subscriberError;
    private SubscriberNextImpl<T> subscriberNext;
    //普通参数
    private Context context;
    private MediaScannerConnection mediaScannerConnection;
    private int index;

    public VideoSubscriberAdapter(Context context) {
        if (ObjectUtil.isNull(context)) {
            throw new RuntimeException("context not null");
        }
        this.context = context;
        //实例化一个MediaScannerConnection
        mediaScannerConnection = new MediaScannerConnection(this.context, this);
    }

    @Override
    public void onCompleted() {
        Log.i(TAG, "VideoSubscriberAdapter.onCompleted");
        if (CollectionUtil.isNotEmpty(ts)) {
            //发出更新媒体库连接
            mediaScannerConnection.connect();
        } else {
//TODO 媒体文件扫描完成回调
        }
        if (ObjectUtil.isNotNull(subscriberCompleted)) {
            subscriberCompleted.onCompleted(ts);
        }
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "VideoSubscriberAdapter.onError", e);
        if (ObjectUtil.isNotNull(subscriberError)) {
            subscriberError.onError(e);
        }

    }

    @Override
    public void onNext(T t) {
        Log.i(TAG, "VideoSubscriberAdapter.onNext");
        if (ObjectUtil.isNotNull(t)) {
            if (ObjectUtil.isNotNull(ts)) {
                ts.add(t);
            }
        }
        if (ObjectUtil.isNotNull(subscriberNext)) {
            subscriberNext.onNext(t);
        }
    }

    @Override
    public void onMediaScannerConnected() {
        Log.i(TAG, "VideoSubscriberAdapter.onMediaScannerConnected");
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
        if (index <= 0) {
            //TODO 媒体文件扫描完成回调
        }
    }

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


}
