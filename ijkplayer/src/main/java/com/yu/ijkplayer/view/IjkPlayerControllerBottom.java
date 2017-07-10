package com.yu.ijkplayer.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.ijkplayer.R;
import com.yu.ijkplayer.R2;
import com.yu.ijkplayer.bean.EventBusCode;
import com.yu.ijkplayer.utils.SrceenUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/7/7 0007.
 */

public class IjkPlayerControllerBottom extends LinearLayout {
    /**
     * 依附的activity
     **/
    private Activity activity;

    /**
     * 控件注入
     */
    public IjkPlayerControllerBottom(Context context) {
        this(context, null);
    }

    public IjkPlayerControllerBottom(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkPlayerControllerBottom(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.activity = (Activity) context;
        //填充页面
        View view = LayoutInflater.from(context).inflate(R.layout.ijk_player_controller_bottom, this);
        ButterKnife.bind(this, view);
        //EventBus注册
        EventBus.getDefault().register(this);
    }

    /**
     * 显示上层控制页面
     */
    private void showView() {
        int navBarHeight = SrceenUtils.getNavigationBarSize(this.activity).x;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
        int cW=this.getWidth();
        if( params.width>0){
            cW= params.width;
        }
        int w =cW- navBarHeight;
        if (w > 0) {
            params.width = w;
            this.setLayoutParams(params);
        }

        this.setVisibility(VISIBLE);
    }

    /**
     * 隐藏上层控制页面
     */
    private void hideView() {
        int navBarHeight = SrceenUtils.getNavigationBarSize(this.activity).x;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
        int cW=this.getWidth();
        if( params.width>0){
            cW= params.width;
        }
        int w = cW+ navBarHeight;
        if (w > navBarHeight) {
            params.width = w;
            this.setLayoutParams(params);
        }

        this.setVisibility(GONE);

    }

    /**
     * eventBus 消息监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusCode eventBusCode) {
        if (EventBusCode.SHOW_VIEW == eventBusCode) {//显示view
            if(ObjectUtil.isNotNull(this.activity)){
                this.showView();
            }

        } else if (EventBusCode.HIDE_VIEW == eventBusCode) {//隐藏view
            if(ObjectUtil.isNotNull(this.activity)){
                this.hideView();
            }

        } else if (EventBusCode.ACTIVITY_FINISH == eventBusCode) {//activity退出
            EventBus.getDefault().unregister(this);
        }
    }

}
