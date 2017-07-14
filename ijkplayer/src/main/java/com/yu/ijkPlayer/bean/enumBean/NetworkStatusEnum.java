package com.yu.ijkPlayer.bean.enumBean;

/**
 * Created by igreentree on 2017/7/10 0010.
 */

public enum NetworkStatusEnum {
    UN_KNOW(-1, "未知"),
    NO_INTERNET(0, "没有网络"),
    DISCONNECT(1, "网络断开"),
    INTERNET(2, "以太网"),
    WIFI(3,"WIFI"),
    MOVE_2G(4,"2G"),
    MOVE_3G(5,"3G"),
    MOVE_4G(6,"4G");
    private int code;
    private String msg;

    NetworkStatusEnum(int i, String msg) {
        this.code = i;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
