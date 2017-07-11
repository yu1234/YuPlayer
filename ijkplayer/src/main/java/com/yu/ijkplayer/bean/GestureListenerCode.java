package com.yu.ijkplayer.bean;

/**
 * Created by igreentree on 2017/7/11 0011.
 */

public enum GestureListenerCode {
    PROGRESS_SLIDE,//进度条滑动
    VOLUME_SLIDE,//声音滑动
    BRIGHTNESS_SLIDE,//亮度滑动
    END_GESTURE;//手势结束 ;

    float percent;
    GestureListenerCode endCode;

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public GestureListenerCode getEndCode() {
        return endCode;
    }

    public void setEndCode(GestureListenerCode endCode) {
        this.endCode = endCode;
    }
}
