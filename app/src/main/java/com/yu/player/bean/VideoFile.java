package com.yu.player.bean;

import android.graphics.Bitmap;

/**
 * Created by igreentree on 2017/6/29 0029.
 */

public class VideoFile extends File {
    private Bitmap thumbnails;
    private String formatDuration;

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
}
