package com.yu.ijkPlayer.bean.enumBean;

import com.xiaoleilu.hutool.util.ArrayUtil;
import com.yu.ijkPlayer.view.playerView.PlayStateParams;

/**
 * Created by igreentree on 2017/7/18 0018.
 */

public enum PlayScreenSize {
    /**
     * 可能会剪裁,保持原视频的大小，显示在中心,当原视频的大小超过view的大小超过部分裁剪处理
     */
    FIT_PARENT(PlayStateParams.fitparent, "默认"),
    /**
     * 可能会剪裁,等比例放大视频，直到填满View为止,超过View的部分作裁剪处理
     */
    FILL_PARENT(PlayStateParams.fillparent, "等比满屏"),
    /**
     * 将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
     */
    WRAP_CONTENT(PlayStateParams.wrapcontent, "居中显示"),
    /**
     * 不剪裁,非等比例拉伸画面填满整个View
     */
    FIT_XY(PlayStateParams.fitxy, "非等比满屏"),
    /**
     * 不剪裁,非等比例拉伸画面到16:9,并完全显示在View中
     */
    FIT_16_9(PlayStateParams.f16_9, "16:9"),
    /**
     * 不剪裁,非等比例拉伸画面到4:3,并完全显示在View中
     */
    FIT_4_3(PlayStateParams.f4_3, "4:3");

    private int id;
    private String name;

    PlayScreenSize(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }



    public String getName() {
        return name;
    }

    public static PlayScreenSize getPlayScreenSize(int id) {
        PlayScreenSize[] playScreenSizes = PlayScreenSize.values();
        if (ArrayUtil.isNotEmpty(playScreenSizes)) {
            for (PlayScreenSize playScreenSize : playScreenSizes) {
                if (playScreenSize.getId() == id) {
                    return playScreenSize;
                }
            }
        }
        return FIT_PARENT;
    }

}
