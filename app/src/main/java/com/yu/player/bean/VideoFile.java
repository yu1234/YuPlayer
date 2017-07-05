package com.yu.player.bean;

import android.graphics.Bitmap;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by igreentree on 2017/6/29 0029.
 */
@DatabaseTable
public class VideoFile extends File {
    private Bitmap thumbnails;
    @DatabaseField
    private String formatDuration;
    @DatabaseField
    private boolean isNew;//是否是新添加
    @DatabaseField
    private boolean isWatch;//是否观看过


    public String getFormatDuration() {
        return formatDuration;
    }

    public void setFormatDuration(String formatDuration) {
        this.formatDuration = formatDuration;
    }

    public Bitmap getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Bitmap thumbnails) {
        this.thumbnails = thumbnails;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isWatch() {
        return isWatch;
    }

    public void setWatch(boolean watch) {
        isWatch = watch;
    }
}
