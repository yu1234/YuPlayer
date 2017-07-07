package com.yu.ijkplayer.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkplayer.R;
import com.yu.ijkplayer.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/7/7 0007.
 */

public class IjkPlayerControllerBottom extends LinearLayout {

    public IjkPlayerControllerBottom(Context context) {
        this(context, null);
    }

    public IjkPlayerControllerBottom(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkPlayerControllerBottom(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //填充页面
        View view = LayoutInflater.from(context).inflate(R.layout.ijk_player_controller_bottom, this);
        ButterKnife.bind(this, view);
    }

}
