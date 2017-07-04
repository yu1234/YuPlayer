package com.yu.player.Impl;

import java.util.List;

/**
 * Created by igreentree on 2017/7/4 0004.
 */

public interface SubscriberCompletedImpl<T> {
    void onCompleted(List<T> ts);
}
