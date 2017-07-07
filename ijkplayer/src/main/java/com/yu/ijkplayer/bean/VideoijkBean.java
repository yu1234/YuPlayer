package com.yu.ijkplayer.bean;

import android.graphics.Bitmap;

/**
 * ========================================
 * <p>
 * 版 权：dou361.com 版权所有 （C） 2015
 * <p>
 * 作 者：陈冠明
 * <p>
 * 个人网站：http://www.dou361.com
 * <p>
 * 版 本：1.0
 * <p>
 * 创建日期：2016/7/7
 * <p>
 * 描 述：使用默认的视频地址对象
 * <p>
 * <p>
 * 修订历史：
 * <p>
 * ========================================
 */
public class VideoijkBean {
    /**
     * id
     */
    private int id;
    /**
     * 分辨率名称
     */
    private String stream;
    /**
     * 分辨率对应视频地址
     */
    private String url;
    /**
     * 备注备用
     */
    private String remarks;
    /**
     * 当前选中的
     */
    private boolean select;

    /**
     * 缩略图
     *
     * @return
     */
    private Bitmap thumbnails;
    /**
     * 标题
     */
    private String title;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public Bitmap getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Bitmap thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoijkBean that = (VideoijkBean) o;

        if (id != that.id) return false;
        if (stream != null ? !stream.equals(that.stream) : that.stream != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return remarks != null ? remarks.equals(that.remarks) : that.remarks == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (stream != null ? stream.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        return result;
    }


}
