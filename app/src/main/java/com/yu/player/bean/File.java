package com.yu.player.bean;

import android.net.Uri;

/**
 * Created by igreentree on 2017/6/29 0029.
 */

public class File {
    private long id;
    private long mId;
    private String formatSize;
    private String name;
    private String path;
    private String mimeType;
    private Uri uri;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getmId() {
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormatSize() {
        return formatSize;
    }

    public void setFormatSize(String formatSize) {
        this.formatSize = formatSize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
