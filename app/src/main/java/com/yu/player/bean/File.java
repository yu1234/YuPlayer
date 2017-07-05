package com.yu.player.bean;

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by igreentree on 2017/6/29 0029.
 */
@DatabaseTable
public class File extends BaseBean{
    @DatabaseField
    private long mId;
    @DatabaseField
    private String formatSize;
    @DatabaseField
    private String name;
    @DatabaseField
    private String path;
    @DatabaseField
    private String mimeType;
    @DatabaseField
    private String uriStr;

    private Uri uri;

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

    public String getUriStr() {
        return uriStr;
    }

    public void setUriStr(String uriStr) {
        this.uriStr = uriStr;
    }
}
