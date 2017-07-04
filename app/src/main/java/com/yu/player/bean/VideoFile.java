package com.yu.player.bean;

import android.graphics.Bitmap;

/**
 * Created by igreentree on 2017/6/29 0029.
 */

public class VideoFile extends File {
    private Bitmap thumbnails;
    private String formatDuration;
    private boolean isNew;//是否是新添加


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
}
