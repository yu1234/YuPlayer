package com.yu.player.global;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by igreentree on 2017/6/28 0028.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
