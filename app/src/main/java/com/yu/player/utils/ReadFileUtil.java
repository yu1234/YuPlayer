package com.yu.player.utils;

import android.os.Environment;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.player.Impl.FileFilter;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by igreentree on 2017/6/28 0028.
 */

public class ReadFileUtil {
    private long videoFileMinSize = 1024 * 1024 * 10;
    //单例模式
    private static volatile ReadFileUtil instance;
    //自定义文件过滤器
    private FileFilter fileFilter;
    //默认文件过滤器
    private FileFilter defaultFileFilter = new FileFilter() {
        @Override
        public boolean doFilter(File file, ReadFileUtil.FileType fileType) {
            if (ObjectUtil.isNotNull(file) && file.exists() && file.canRead() && file.isFile()) {
                if (FileType.VOIDEO == fileType) {
                    if (FileUtils.isVideo(file)) {
                        if (file.length() > videoFileMinSize) {
                            return true;
                        }
                    }

                } else if (FileType.MUSIC == fileType) {
                    return FileUtils.isAudio(file);
                } else if (FileType.PICTURE == fileType) {
                    return FileUtils.isPicture(file);
                }
            }
            return false;
        }
    };

    public enum FileType {
        VOIDEO, MUSIC, PICTURE
    }

    private ReadFileUtil() {
    }

    public static ReadFileUtil getInstance() {
        if (instance == null) {                         //Single Checked
            synchronized (ReadFileUtil.class) {
                if (instance == null) {                 //Double Checked
                    instance = new ReadFileUtil();
                }
            }
        }
        return instance;
    }

    //获取指定类型文件
    public void getFiles(final FileType fileType, Subscriber<File> subscriber) {
        if (ObjectUtil.isNotNull(fileType) && ObjectUtil.isNotNull(subscriber)) {
            File rootFile = Environment.getExternalStorageDirectory();
            if (ObjectUtil.isNotNull(rootFile)) {
                Observable.just(rootFile)
                        .flatMap(new Func1<File, Observable<File>>() {
                            @Override
                            public Observable<File> call(File file) {
                                Observable<File> fileObservable = listFiles(fileType, file);
                                return fileObservable;
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(subscriber);
            }
        }
    }

    /**
     * rxjava递归查询内存中的视频文件
     *
     * @param f
     * @return
     */
    private Observable<File> listFiles(final FileType fileType, File f) {
        if (ObjectUtil.isNotNull(f)) {
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                if (ObjectUtil.isNotNull(files)) {
                    return Observable.from(files)
                            .flatMap(new Func1<File, Observable<File>>() {
                                @Override
                                public Observable<File> call(File file) {
                                    /**如果是文件夹就递归**/
                                    return listFiles(fileType, file);
                                }
                            });
                } else {
                    return null;
                }
            } else {
                /**filter操作符过滤视频文件,是视频文件就通知观察者**/
                return Observable.just(f)
                        .filter(new Func1<File, Boolean>() {
                            @Override
                            public Boolean call(File file) {
                                return ObjectUtil.isNotNull(fileFilter) ? fileFilter.doFilter(file, fileType) : defaultFileFilter.doFilter(file, fileType);
                            }
                        });
            }
        } else {
            return null;
        }
    }

    public FileFilter getFileFilter() {
        if (ObjectUtil.isNull(fileFilter)) {
            return defaultFileFilter;
        }
        return fileFilter;
    }

    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }
}
