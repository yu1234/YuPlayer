package com.yu.player.Impl;


import com.yu.player.bean.File;

import java.util.List;

/**
 * Created by igreentree on 2017/7/5 0005.
 */

public interface ScanFileCompletedImpl<T extends File> {
    void onScanCompleted(List<T> ts);
}
