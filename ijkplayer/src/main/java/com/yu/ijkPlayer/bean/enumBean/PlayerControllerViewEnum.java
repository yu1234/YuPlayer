package com.yu.ijkPlayer.bean.enumBean;

/**
 * Created by igreentree on 2017/7/13 0013.
 * 播放控制器界面隐藏/出现（配合EventBus使用）
 */

public enum PlayerControllerViewEnum {
    IN_SHOW, // (子控制-->主控制：：显示)
    IN_HIDE,//  (子控制-->主控制：：隐藏)
    OUT_SHOW,// (主控制-->子控制：：显示)
    OUT_HIDE;// (主控制-->子控制：：隐藏)
}
