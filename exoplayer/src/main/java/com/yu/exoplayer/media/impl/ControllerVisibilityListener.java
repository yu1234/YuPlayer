package com.yu.exoplayer.media.impl;

import android.view.View;

/**
 * Created by igreentree on 2017/6/22 0022.
 */

public class ControllerVisibilityListener {
    /**
     * Called when the visibility changes.
     *
     * @param visibility The new visibility. Either {@link View#VISIBLE} or {@link View#GONE}.
     */
    private int visibility;

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }
}
